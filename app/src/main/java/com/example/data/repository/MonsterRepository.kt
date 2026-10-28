package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.remote.Monster
import com.example.data.remote.OperationType
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

/**
 * Repository providing full Firestore CRUD operations and real-time observation for the
 * 'Monster' entity under `/users/{userId}/monsters/{monsterId}`.
 */
class MonsterRepository(private val db: FirebaseFirestore) {

    // Secondary constructor resolving the provisioned custom database ID from resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    /**
     * CREATE: Creates a new Monster document in `/users/{uid}/monsters/{monsterId}`.
     */
    suspend fun createMonster(monster: Monster): Result<String> {
        val uid = requireUserId()
        val monsterId = monster.id.takeIf { it.isNotBlank() }
            ?: "mon_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("monsters").document(monsterId)
        val payload = buildMonsterPayload(uid, monster, includeCreatedAt = true)

        return try {
            docRef.set(payload).await()
            Result.success(monsterId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * READ (Single): Fetches a single Monster by document ID for a given userId.
     */
    suspend fun getMonsterById(monsterId: String, ownerUserId: String? = null): Result<Monster?> {
        val targetUid = ownerUserId ?: requireUserId()
        val docRef = db.collection("users").document(targetUid).collection("monsters").document(monsterId)
        return try {
            val snapshot = docRef.get().await()
            val monster = if (snapshot.exists()) {
                snapshot.toObject(
                    Monster::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
            Result.success(monster)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * READ (List): Queries all Monsters owned by the authenticated trainer, aligned with security rules.
     */
    suspend fun getUserMonsters(ownerUserId: String? = null): Result<List<Monster>> {
        val targetUid = ownerUserId ?: requireUserId()
        val collectionPath = "users/$targetUid/monsters"
        return try {
            val snapshot = db.collection("users")
                .document(targetUid)
                .collection("monsters")
                .whereEqualTo("userId", targetUid)
                .get()
                .await()
            val monsters = snapshot.toObjects(
                Monster::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(monsters)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, collectionPath)
            Result.failure(e)
        }
    }

    /**
     * READ (Realtime Flow): Observes all Monsters owned by `userId` in real time using `snapshots()`.
     */
    fun observeUserMonsters(userId: String): Flow<List<Monster>> {
        val collectionPath = "users/$userId/monsters"
        return db.collection("users")
            .document(userId)
            .collection("monsters")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    Monster::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, collectionPath)
                }
                throw error
            }
    }

    /**
     * UPDATE: Updates an existing Monster's stats, level, genetics, or evolution progress.
     */
    suspend fun updateMonster(monster: Monster): Result<Unit> {
        val uid = requireUserId()
        require(monster.id.isNotBlank()) { "Monster ID must not be blank for update" }
        val docRef = db.collection("users").document(uid).collection("monsters").document(monster.id)
        val updatePayload = buildMonsterUpdatePayload(monster)

        return try {
            docRef.update(updatePayload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * DELETE: Deletes a Monster document by ID from `/users/{uid}/monsters/{monsterId}`.
     */
    suspend fun deleteMonster(monsterId: String, ownerUserId: String? = null): Result<Unit> {
        val targetUid = ownerUserId ?: requireUserId()
        require(monsterId.isNotBlank()) { "Monster ID must not be blank for delete" }
        val docRef = db.collection("users").document(targetUid).collection("monsters").document(monsterId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    private fun buildMonsterPayload(
        uid: String,
        monster: Monster,
        includeCreatedAt: Boolean
    ): Map<String, Any> {
        val base = mutableMapOf<String, Any>(
            "userId" to uid,
            "speciesId" to monster.speciesId.coerceIn(1L, 240L),
            "name" to monster.name.take(50).ifBlank { "Monster" },
            "primaryElement" to monster.primaryElement.take(20).ifBlank { "TERRA" },
            "currentLevel" to monster.currentLevel.coerceIn(1L, 100L),
            "xp" to monster.xp.coerceIn(0L, 9999999L),
            "attack" to monster.attack.coerceIn(1L, 9999L),
            "defense" to monster.defense.coerceIn(1L, 9999L),
            "speed" to monster.speed.coerceIn(1L, 9999L),
            "elementalPower" to monster.elementalPower.coerceIn(1L, 9999L),
            "maxHp" to monster.maxHp.coerceIn(1L, 99999L),
            "trainedAttack" to monster.trainedAttack.coerceIn(0L, 250L),
            "trainedDefense" to monster.trainedDefense.coerceIn(0L, 250L),
            "trainedSpeed" to monster.trainedSpeed.coerceIn(0L, 250L),
            "geneticIvRank" to if (monster.geneticIvRank in listOf("C", "B", "A", "S", "S+")) monster.geneticIvRank else "A",
            "geneticTraitId" to monster.geneticTraitId.take(60).ifBlank { "trait_solar_vein" },
            "geneticTraitName" to monster.geneticTraitName.take(60).ifBlank { "Solar-Ader" },
            "generation" to monster.generation.coerceIn(1L, 100L),
            "evolutionStage" to monster.evolutionStage.coerceIn(1L, 3L),
            "evolutionProgressPercent" to monster.evolutionProgressPercent.coerceIn(0L, 100L),
            "targetEvolutionSpeciesId" to monster.targetEvolutionSpeciesId.coerceIn(0L, 240L),
            "requiredArtifactId" to monster.requiredArtifactId.take(60),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (includeCreatedAt) {
            base["createdAt"] = FieldValue.serverTimestamp()
        }
        return base
    }

    private fun buildMonsterUpdatePayload(monster: Monster): Map<String, Any> {
        return mapOf(
            "speciesId" to monster.speciesId.coerceIn(1L, 240L),
            "name" to monster.name.take(50).ifBlank { "Monster" },
            "primaryElement" to monster.primaryElement.take(20).ifBlank { "TERRA" },
            "currentLevel" to monster.currentLevel.coerceIn(1L, 100L),
            "xp" to monster.xp.coerceIn(0L, 9999999L),
            "attack" to monster.attack.coerceIn(1L, 9999L),
            "defense" to monster.defense.coerceIn(1L, 9999L),
            "speed" to monster.speed.coerceIn(1L, 9999L),
            "elementalPower" to monster.elementalPower.coerceIn(1L, 9999L),
            "maxHp" to monster.maxHp.coerceIn(1L, 99999L),
            "trainedAttack" to monster.trainedAttack.coerceIn(0L, 250L),
            "trainedDefense" to monster.trainedDefense.coerceIn(0L, 250L),
            "trainedSpeed" to monster.trainedSpeed.coerceIn(0L, 250L),
            "geneticIvRank" to if (monster.geneticIvRank in listOf("C", "B", "A", "S", "S+")) monster.geneticIvRank else "A",
            "geneticTraitId" to monster.geneticTraitId.take(60).ifBlank { "trait_solar_vein" },
            "geneticTraitName" to monster.geneticTraitName.take(60).ifBlank { "Solar-Ader" },
            "generation" to monster.generation.coerceIn(1L, 100L),
            "evolutionStage" to monster.evolutionStage.coerceIn(1L, 3L),
            "evolutionProgressPercent" to monster.evolutionProgressPercent.coerceIn(0L, 100L),
            "targetEvolutionSpeciesId" to monster.targetEvolutionSpeciesId.coerceIn(0L, 240L),
            "requiredArtifactId" to monster.requiredArtifactId.take(60),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}
