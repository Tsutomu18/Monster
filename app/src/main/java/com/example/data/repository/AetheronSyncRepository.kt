package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.local.AetheronLocalDatabase
import com.example.data.local.LocalMonsterDao
import com.example.data.local.LocalMonsterEntity
import com.example.data.local.LocalPlayerProfileDao
import com.example.data.local.LocalPlayerProfileEntity
import com.example.data.remote.Monster
import com.example.data.remote.PlayerCloudProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Summary report of a bidirectional synchronization pass between the local Room database
 * and Firebase Firestore.
 */
data class SyncReport(
    val isCloudAvailable: Boolean,
    val pushedProfilesCount: Int = 0,
    val pushedMonstersCount: Int = 0,
    val deletedRemoteMonstersCount: Int = 0,
    val pulledMonstersCount: Int = 0,
    val pulledProfileUpdated: Boolean = false
)

/**
 * Hybrid Repository Wrapper that coordinates persistence between the local Room database
 * ([AetheronLocalDatabase]) and Firebase Firestore ([AetheronRepository]).
 *
 * - When the user is unauthenticated (playing without Google) or offline, all reads and writes
 *   operate seamlessly against the local Room SQLite database and mark modified entities with
 *   `pendingSync = true`.
 * - When the user is authenticated with Google and has internet connectivity, local changes are
 *   synchronized with Firestore and remote changes are cached locally in Room.
 */
class AetheronSyncRepository(
    private val cloudRepository: AetheronRepository,
    private val localMonsterDao: LocalMonsterDao,
    private val localProfileDao: LocalPlayerProfileDao,
    private val appContext: Context? = null,
    private val authUidProvider: () -> String? = {
        runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
    },
    private val networkAvailableProvider: () -> Boolean = {
        isNetworkConnected(appContext)
    }
) {

    constructor(
        context: Context,
        cloudRepository: AetheronRepository = AetheronRepository(context),
        localDatabase: AetheronLocalDatabase = AetheronLocalDatabase.getInstance(context)
    ) : this(
        cloudRepository = cloudRepository,
        localMonsterDao = localDatabase.localMonsterDao(),
        localProfileDao = localDatabase.localPlayerProfileDao(),
        appContext = context.applicationContext
    )

    /**
     * Returns true when both a Google-authenticated Firebase user and an active network
     * connection are available for Firestore operations.
     */
    fun canSyncWithCloud(): Boolean {
        val uid = authUidProvider()
        return !uid.isNullOrBlank() && networkAvailableProvider()
    }

    private fun resolveActiveUserId(fallbackUserId: String = LOCAL_DEFAULT_USER_ID): String {
        return authUidProvider()?.takeIf { it.isNotBlank() }
            ?: fallbackUserId.ifBlank { LOCAL_DEFAULT_USER_ID }
    }

    // =========================================================================
    // Monster CRUD (Offline-First via Room + Firestore Sync)
    // =========================================================================

    /**
     * Reactively observes all monsters for [userId] from the local Room database.
     */
    fun observeMonsters(userId: String = resolveActiveUserId()): Flow<List<Monster>> {
        val targetUserId = userId.ifBlank { resolveActiveUserId() }
        return localMonsterDao.observeMonstersByUser(targetUserId)
            .map { entities -> entities.map { it.toCloudMonster() } }
    }

    /**
     * Fetches all monsters for [userId]. When cloud sync is available, pulls fresh remote
     * monsters into Room first; otherwise returns the local Room dataset immediately.
     */
    suspend fun getMonsters(userId: String = resolveActiveUserId()): Result<List<Monster>> {
        val targetUserId = userId.ifBlank { resolveActiveUserId() }
        if (canSyncWithCloud() && authUidProvider() == targetUserId) {
            val remoteResult = cloudRepository.getUserMonsters(targetUserId)
            if (remoteResult.isSuccess) {
                val remoteMonsters = remoteResult.getOrNull().orEmpty()
                val localPendingIds = localMonsterDao.getPendingSyncMonsters().map { it.id }.toSet()
                val entitiesToCache = remoteMonsters
                    .filterNot { it.id in localPendingIds }
                    .map { LocalMonsterEntity.fromCloudMonster(it, pendingSync = false) }
                if (entitiesToCache.isNotEmpty()) {
                    localMonsterDao.upsertMonsters(entitiesToCache)
                }
            }
        }
        val localList = localMonsterDao.getMonstersByUser(targetUserId).map { it.toCloudMonster() }
        return Result.success(localList)
    }

    /**
     * Gets a single Monster by [monsterId], checking Room first and falling back to Firestore
     * if online and authenticated.
     */
    suspend fun getMonsterById(monsterId: String): Result<Monster?> {
        val local = localMonsterDao.getMonsterById(monsterId)
        if (local != null) {
            return Result.success(local.toCloudMonster())
        }
        if (canSyncWithCloud()) {
            val remoteResult = cloudRepository.getMonsterById(monsterId)
            val remoteMonster = remoteResult.getOrNull()
            if (remoteMonster != null) {
                localMonsterDao.upsertMonster(
                    LocalMonsterEntity.fromCloudMonster(remoteMonster, pendingSync = false)
                )
                return Result.success(remoteMonster)
            }
        }
        return Result.success(null)
    }

    /**
     * Saves (creates or updates) a [Monster] in the local Room database immediately,
     * and synchronizes it with Firestore if Google authentication & internet are available.
     */
    suspend fun saveMonster(monster: Monster): Result<String> {
        val activeUserId = authUidProvider() ?: monster.userId.ifBlank { LOCAL_DEFAULT_USER_ID }
        val monsterId = monster.id.takeIf { it.isNotBlank() }
            ?: "mon_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val normalizedMonster = monster.copy(id = monsterId, userId = activeUserId)

        // 1. Always persist to local Room database first (with pendingSync = true)
        val localEntity = LocalMonsterEntity.fromCloudMonster(
            monster = normalizedMonster,
            pendingSync = true,
            isDeletedLocally = false
        )
        localMonsterDao.upsertMonster(localEntity)

        // 2. If authenticated & online, push to Firestore and clear pendingSync on success
        if (canSyncWithCloud()) {
            try {
                val existingRemote = cloudRepository.getMonsterById(monsterId, activeUserId).getOrNull()
                val cloudResult = if (existingRemote == null) {
                    cloudRepository.createMonster(normalizedMonster)
                } else {
                    cloudRepository.updateMonster(normalizedMonster).map { monsterId }
                }
                if (cloudResult.isSuccess) {
                    localMonsterDao.upsertMonster(localEntity.copy(pendingSync = false))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Cloud monster sync deferred to Room queue: ${e.message}")
            }
        }

        return Result.success(monsterId)
    }

    /**
     * Deletes a monster by [monsterId]. If offline or unauthenticated, marks it as a tombstone
     * in Room (`isDeletedLocally = true, pendingSync = true`) so it will be removed from Firestore
     * on the next synchronization pass.
     */
    suspend fun deleteMonster(monsterId: String): Result<Unit> {
        if (canSyncWithCloud()) {
            val remoteDelete = cloudRepository.deleteMonster(monsterId)
            if (remoteDelete.isSuccess) {
                localMonsterDao.hardDeleteMonsterById(monsterId)
                return Result.success(Unit)
            }
        }
        // Offline or unauthenticated fallback: soft-delete in Room for later sync
        localMonsterDao.markMonsterDeleted(monsterId)
        return Result.success(Unit)
    }

    // =========================================================================
    // Player Profile CRUD (Offline-First via Room + Firestore Sync)
    // =========================================================================

    fun observePlayerProfile(userId: String = resolveActiveUserId()): Flow<PlayerCloudProfile?> {
        val targetUserId = userId.ifBlank { resolveActiveUserId() }
        return localProfileDao.observeProfile(targetUserId)
            .map { it?.toCloudProfile() }
    }

    /**
     * Loads the player's profile, preferring the newest state between Room and Firestore
     * when authenticated, or falling back directly to Room when offline / without Google.
     */
    suspend fun getPlayerProfile(userId: String = resolveActiveUserId()): Result<PlayerCloudProfile?> {
        val targetUserId = userId.ifBlank { resolveActiveUserId() }
        val localEntity = localProfileDao.getProfile(targetUserId)
            ?: localProfileDao.getProfile(LOCAL_DEFAULT_USER_ID)

        if (canSyncWithCloud() && authUidProvider() == targetUserId) {
            // If local profile has unsynced changes, push it to Firestore first
            if (localEntity != null && localEntity.pendingSync) {
                val migrated = localEntity.copy(userId = targetUserId)
                val pushRes = cloudRepository.savePlayerProfile(migrated.toCloudProfile())
                if (pushRes.isSuccess) {
                    localProfileDao.upsertProfile(migrated.copy(pendingSync = false))
                    return Result.success(migrated.toCloudProfile())
                }
            }

            // Otherwise fetch latest profile from Firestore and cache in Room
            val remoteResult = cloudRepository.getPlayerProfile(targetUserId)
            val remoteProfile = remoteResult.getOrNull()
            if (remoteProfile != null) {
                localProfileDao.upsertProfile(
                    LocalPlayerProfileEntity.fromCloudProfile(remoteProfile, pendingSync = false)
                )
                return Result.success(remoteProfile)
            }
        }

        return Result.success(localEntity?.toCloudProfile())
    }

    /**
     * Saves the player's profile to Room immediately and synchronizes with Firestore
     * if Google authentication and internet connection are available.
     */
    suspend fun savePlayerProfile(profile: PlayerCloudProfile): Result<Unit> {
        val activeUserId = authUidProvider() ?: profile.userId.ifBlank { LOCAL_DEFAULT_USER_ID }
        val normalizedProfile = profile.copy(id = activeUserId, userId = activeUserId)

        // 1. Save to Room with pendingSync = true
        val localEntity = LocalPlayerProfileEntity.fromCloudProfile(
            profile = normalizedProfile,
            pendingSync = true
        )
        localProfileDao.upsertProfile(localEntity)

        // 2. Attempt immediate Firestore sync if authenticated & online
        if (canSyncWithCloud()) {
            val remoteRes = cloudRepository.savePlayerProfile(normalizedProfile)
            if (remoteRes.isSuccess) {
                localProfileDao.upsertProfile(localEntity.copy(pendingSync = false))
            }
        }

        return Result.success(Unit)
    }

    // =========================================================================
    // Bidirectional Synchronization (Room <-> Firestore)
    // =========================================================================

    /**
     * Synchronizes all pending local Room records (monsters, deletions, and player profiles)
     * with Firebase Firestore and caches the latest remote state into Room.
     */
    suspend fun synchronizeWithFirestore(): Result<SyncReport> {
        val uid = authUidProvider()
        if (uid.isNullOrBlank() || !networkAvailableProvider()) {
            return Result.success(SyncReport(isCloudAvailable = false))
        }

        return try {
            var pushedProfiles = 0
            var pushedMonsters = 0
            var deletedRemoteMonsters = 0
            var pulledMonsters = 0
            var pulledProfileUpdated = false

            // Step 1: Migrate any offline guest ("local_trainer") data in Room to the authenticated uid
            if (uid != LOCAL_DEFAULT_USER_ID) {
                localMonsterDao.reassignUserMonsters(
                    oldUserId = LOCAL_DEFAULT_USER_ID,
                    newUserId = uid
                )
                val guestProfile = localProfileDao.getProfile(LOCAL_DEFAULT_USER_ID)
                val existingUserProfile = localProfileDao.getProfile(uid)
                if (guestProfile != null && existingUserProfile == null) {
                    localProfileDao.upsertProfile(
                        guestProfile.copy(userId = uid, pendingSync = true)
                    )
                    localProfileDao.deleteProfile(LOCAL_DEFAULT_USER_ID)
                }
            }

            // Step 2: Push pending local profile updates to Firestore
            val pendingProfiles = localProfileDao.getPendingSyncProfiles()
                .filter { it.userId == uid }
            for (pendingProfile in pendingProfiles) {
                val saveRes = cloudRepository.savePlayerProfile(pendingProfile.toCloudProfile())
                if (saveRes.isSuccess) {
                    localProfileDao.upsertProfile(pendingProfile.copy(pendingSync = false))
                    pushedProfiles++
                }
            }

            // Step 3: Push pending local monster creations/updates/deletions to Firestore
            val pendingMonsters = localMonsterDao.getPendingSyncMonsters()
                .filter { it.userId == uid }
            for (localMonster in pendingMonsters) {
                if (localMonster.isDeletedLocally) {
                    val delRes = cloudRepository.deleteMonster(localMonster.id, uid)
                    if (delRes.isSuccess) {
                        localMonsterDao.hardDeleteMonsterById(localMonster.id)
                        deletedRemoteMonsters++
                    }
                } else {
                    val cloudMonster = localMonster.toCloudMonster().copy(userId = uid)
                    val existingRemote = cloudRepository.getMonsterById(cloudMonster.id, uid).getOrNull()
                    val writeRes = if (existingRemote == null) {
                        cloudRepository.createMonster(cloudMonster).map { Unit }
                    } else {
                        cloudRepository.updateMonster(cloudMonster)
                    }
                    if (writeRes.isSuccess) {
                        localMonsterDao.upsertMonster(
                            localMonster.copy(userId = uid, pendingSync = false)
                        )
                        pushedMonsters++
                    }
                }
            }

            // Step 4: Pull remote Firestore monsters into Room cache
            val remoteMonstersRes = cloudRepository.getUserMonsters(uid)
            if (remoteMonstersRes.isSuccess) {
                val remoteMonsters = remoteMonstersRes.getOrNull().orEmpty()
                val stillPendingIds = localMonsterDao.getPendingSyncMonsters().map { it.id }.toSet()
                val toUpsert = remoteMonsters
                    .filterNot { it.id in stillPendingIds }
                    .map { LocalMonsterEntity.fromCloudMonster(it, pendingSync = false) }
                if (toUpsert.isNotEmpty()) {
                    localMonsterDao.upsertMonsters(toUpsert)
                    pulledMonsters = toUpsert.size
                }
            }

            // Step 5: Pull remote Firestore profile into Room if no local profile is pending
            val currentLocalProfile = localProfileDao.getProfile(uid)
            if (currentLocalProfile == null || !currentLocalProfile.pendingSync) {
                val remoteProfile = cloudRepository.getPlayerProfile(uid).getOrNull()
                if (remoteProfile != null) {
                    localProfileDao.upsertProfile(
                        LocalPlayerProfileEntity.fromCloudProfile(remoteProfile, pendingSync = false)
                    )
                    pulledProfileUpdated = true
                }
            }

            Result.success(
                SyncReport(
                    isCloudAvailable = true,
                    pushedProfilesCount = pushedProfiles,
                    pushedMonstersCount = pushedMonsters,
                    deletedRemoteMonstersCount = deletedRemoteMonsters,
                    pulledMonstersCount = pulledMonsters,
                    pulledProfileUpdated = pulledProfileUpdated
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Synchronization with Firestore encountered an error", e)
            Result.failure(e)
        }
    }

    companion object {
        const val LOCAL_DEFAULT_USER_ID = "local_trainer"
        private const val TAG = "AetheronSyncRepo"

        fun isNetworkConnected(context: Context?): Boolean {
            if (context == null) return true
            return try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return true
                val network = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(network) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } catch (e: Exception) {
                true
            }
        }
    }
}
