package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.remote.ArenaRoster
import com.example.data.remote.Monster
import com.example.data.remote.OperationType
import com.example.data.remote.PlayerCloudProfile
import com.example.data.remote.TradeOffer
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AetheronRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    val monsterRepository = MonsterRepository(db)

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Trainer muss mit Google angemeldet sein, um Cloud-Funktionen zu nutzen.")
    }

    // --- Monster Entity CRUD Delegation ---
    suspend fun createMonster(monster: Monster): Result<String> =
        monsterRepository.createMonster(monster)

    suspend fun getMonsterById(monsterId: String, ownerUserId: String? = null): Result<Monster?> =
        monsterRepository.getMonsterById(monsterId, ownerUserId)

    suspend fun getUserMonsters(ownerUserId: String? = null): Result<List<Monster>> =
        monsterRepository.getUserMonsters(ownerUserId)

    fun observeUserMonsters(userId: String): Flow<List<Monster>> =
        monsterRepository.observeUserMonsters(userId)

    suspend fun updateMonster(monster: Monster): Result<Unit> =
        monsterRepository.updateMonster(monster)

    suspend fun deleteMonster(monsterId: String, ownerUserId: String? = null): Result<Unit> =
        monsterRepository.deleteMonster(monsterId, ownerUserId)

    // --- PlayerCloudProfile CRUD ---
    fun observePlayerProfile(userId: String): Flow<PlayerCloudProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        PlayerCloudProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    suspend fun getPlayerProfile(userId: String): Result<PlayerCloudProfile?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snap = docRef.get().await()
            val profile = if (snap.exists()) {
                snap.toObject(
                    PlayerCloudProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
            Result.success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun savePlayerProfile(profile: PlayerCloudProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "trainerName" to profile.trainerName.take(40).ifBlank { "Aether-Trainer" },
                    "level" to profile.level.coerceIn(1L, 100L),
                    "gold" to profile.gold.coerceIn(0L, 9999999L),
                    "continentId" to profile.continentId.coerceIn(1L, 3L),
                    "badgesCount" to profile.badgesCount.coerceIn(0L, 24L),
                    "capturedCount" to profile.capturedCount.coerceIn(0L, 240L),
                    "questsCompleted" to profile.questsCompleted.coerceIn(0L, 10000L),
                    "factionSylvariaRep" to profile.factionSylvariaRep.coerceIn(0L, 10000L),
                    "factionPyroclastRep" to profile.factionPyroclastRep.coerceIn(0L, 10000L),
                    "factionAetherisRep" to profile.factionAetherisRep.coerceIn(0L, 10000L),
                    "saveStateJson" to profile.saveStateJson.take(79000).ifBlank { "{}" },
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mapOf(
                    "trainerName" to profile.trainerName.take(40).ifBlank { "Aether-Trainer" },
                    "level" to profile.level.coerceIn(1L, 100L),
                    "gold" to profile.gold.coerceIn(0L, 9999999L),
                    "continentId" to profile.continentId.coerceIn(1L, 3L),
                    "badgesCount" to profile.badgesCount.coerceIn(0L, 24L),
                    "capturedCount" to profile.capturedCount.coerceIn(0L, 240L),
                    "questsCompleted" to profile.questsCompleted.coerceIn(0L, 10000L),
                    "factionSylvariaRep" to profile.factionSylvariaRep.coerceIn(0L, 10000L),
                    "factionPyroclastRep" to profile.factionPyroclastRep.coerceIn(0L, 10000L),
                    "factionAetherisRep" to profile.factionAetherisRep.coerceIn(0L, 10000L),
                    "saveStateJson" to profile.saveStateJson.take(79000).ifBlank { "{}" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeOpenTrades(): Flow<List<TradeOffer>> {
        val path = "trades"
        return db.collection(path)
            .whereEqualTo("status", "open")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    TradeOffer::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createTradeOffer(
        ownerName: String,
        offeredSpeciesId: Int,
        offeredMonsterName: String,
        offeredLevel: Int,
        offeredIvRank: String,
        offeredTrait: String,
        offeredTrainedStats: String,
        requestedSpeciesId: Int,
        requestedSpeciesName: String
    ): Result<String> {
        val uid = requireUserId()
        val tradeId = "trade_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("trades").document(tradeId)
        val payload = mapOf(
            "ownerId" to uid,
            "ownerName" to ownerName.take(40).ifBlank { "Trainer" },
            "offeredSpeciesId" to offeredSpeciesId.coerceIn(1, 240).toLong(),
            "offeredMonsterName" to offeredMonsterName.take(50).ifBlank { "Monster" },
            "offeredLevel" to offeredLevel.coerceIn(1, 100).toLong(),
            "offeredIvRank" to if (offeredIvRank in listOf("C", "B", "A", "S", "S+")) offeredIvRank else "A",
            "offeredTrait" to offeredTrait.take(50).ifBlank { "Aether-Gen" },
            "offeredTrainedStats" to offeredTrainedStats.take(120).ifBlank { "Standard" },
            "requestedSpeciesId" to requestedSpeciesId.coerceIn(1, 240).toLong(),
            "requestedSpeciesName" to requestedSpeciesName.take(50).ifBlank { "Monster" },
            "status" to "open",
            "completedByUid" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(tradeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun fulfillTradeOffer(tradeId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("trades").document(tradeId)
        return try {
            docRef.update(
                mapOf(
                    "status" to "completed",
                    "completedByUid" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeActiveArenaRosters(): Flow<List<ArenaRoster>> {
        val path = "arena_rosters"
        return db.collection(path)
            .whereEqualTo("status", "active")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ArenaRoster::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun publishArenaRoster(
        trainerName: String,
        combatPower: Int,
        badgesCount: Int,
        wins: Int,
        leadSpeciesId: Int,
        teamSummary: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("arena_rosters").document(uid)
        return try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "trainerName" to trainerName.take(40).ifBlank { "Trainer" },
                        "combatPower" to combatPower.coerceIn(1, 999999).toLong(),
                        "badgesCount" to badgesCount.coerceIn(0, 24).toLong(),
                        "wins" to wins.coerceIn(0, 999999).toLong(),
                        "leadSpeciesId" to leadSpeciesId.coerceIn(1, 240).toLong(),
                        "teamSummary" to teamSummary.take(500).ifBlank { "Team" },
                        "status" to "active",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "trainerName" to trainerName.take(40).ifBlank { "Trainer" },
                        "combatPower" to combatPower.coerceIn(1, 999999).toLong(),
                        "badgesCount" to badgesCount.coerceIn(0, 24).toLong(),
                        "wins" to wins.coerceIn(0, 999999).toLong(),
                        "leadSpeciesId" to leadSpeciesId.coerceIn(1, 240).toLong(),
                        "teamSummary" to teamSummary.take(500).ifBlank { "Team" },
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
