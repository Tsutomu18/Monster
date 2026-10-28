package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.remote.Monster
import com.example.data.remote.PlayerCloudProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AetheronRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun monsterCrud_createReadUpdateObserveAndDelete_succeedsForOwner() = runBlocking {
        val aliceUid = signInTestUser("alice_mon@aetheron.test")
        val monsterRepository = MonsterRepository(firestore)
        val uniqueId = "mon_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val initialMonster = Monster(
            id = uniqueId,
            userId = aliceUid,
            speciesId = 1L,
            name = "Sylphling",
            primaryElement = "TERRA",
            currentLevel = 12L,
            xp = 180L,
            attack = 78L,
            defense = 68L,
            speed = 74L,
            elementalPower = 72L,
            maxHp = 155L,
            trainedAttack = 18L,
            trainedDefense = 10L,
            trainedSpeed = 14L,
            geneticIvRank = "S",
            geneticTraitId = "trait_solar_vein",
            geneticTraitName = "Solar-Ader",
            generation = 1L,
            evolutionStage = 1L,
            evolutionProgressPercent = 65L,
            targetEvolutionSpeciesId = 2L,
            requiredArtifactId = ""
        )

        // 1. CREATE
        val createdId = withTimeout(DEFAULT_TIMEOUT_MS) {
            monsterRepository.createMonster(initialMonster).getOrThrow()
        }
        assertEquals(uniqueId, createdId)

        // 2. READ (Single)
        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            monsterRepository.getMonsterById(createdId).getOrThrow()
        }
        assertNotNull(fetched)
        assertEquals("Sylphling", fetched?.name)
        assertEquals(78L, fetched?.attack)
        assertEquals("S", fetched?.geneticIvRank)
        assertEquals(65L, fetched?.evolutionProgressPercent)

        // 3. UPDATE (Stats, Level & Evolution Progress)
        val updatedMonster = fetched!!.copy(
            currentLevel = 16L,
            attack = 98L,
            defense = 82L,
            speed = 92L,
            trainedAttack = 32L,
            evolutionProgressPercent = 100L
        )
        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            monsterRepository.updateMonster(updatedMonster)
        }
        assertTrue(updateResult.isSuccess)

        // 4. OBSERVE (Realtime Flow)
        val observedList = withTimeout(FLOW_TIMEOUT_MS) {
            monsterRepository.observeUserMonsters(aliceUid).first { list ->
                list.any { it.id == createdId && it.currentLevel == 16L }
            }
        }
        val observedMonster = observedList.first { it.id == createdId }
        assertEquals(98L, observedMonster.attack)
        assertEquals(100L, observedMonster.evolutionProgressPercent)

        // 5. DELETE
        val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            monsterRepository.deleteMonster(createdId)
        }
        assertTrue(deleteResult.isSuccess)

        val afterDelete = withTimeout(DEFAULT_TIMEOUT_MS) {
            monsterRepository.getMonsterById(createdId).getOrThrow()
        }
        assertNull(afterDelete)
    }

    @Test
    fun monsterCrud_crossUserReadAndDelete_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_owner@aetheron.test")
        val monsterRepository = MonsterRepository(firestore)
        val monsterId = "mon_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            monsterRepository.createMonster(
                Monster(id = monsterId, userId = aliceUid, name = "AliceMonster")
            ).getOrThrow()
        }

        // Switch to Bob and attempt unauthorized cross-user access
        signInTestUser("bob_intruder@aetheron.test")
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            monsterRepository.getMonsterById(monsterId, ownerUserId = aliceUid)
        }
        assertTrue(crossReadResult.isFailure)
        val firestoreEx = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun saveAndGetPlayerProfile_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser("alice@aetheron.test")
        val repository = AetheronRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.savePlayerProfile(
                PlayerCloudProfile(
                    userId = aliceUid,
                    trainerName = "Alice",
                    level = 15L,
                    gold = 2400L,
                    continentId = 1L,
                    badgesCount = 4L,
                    capturedCount = 22L,
                    questsCompleted = 9L,
                    saveStateJson = "{\"team\":[\"Sylphox\"]}"
                )
            )
        }
        assertTrue(saveResult.isSuccess)

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getPlayerProfile(aliceUid).getOrThrow()
        }
        assertNotNull(fetched)
        assertEquals("Alice", fetched?.trainerName)
        assertEquals(4L, fetched?.badgesCount)
    }

    @Test
    fun getPlayerProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice2@aetheron.test")
        val repo = AetheronRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.savePlayerProfile(
                PlayerCloudProfile(
                    userId = aliceUid,
                    trainerName = "Alice",
                    saveStateJson = "{\"team\":[]}"
                )
            ).getOrThrow()
        }

        signInTestUser("bob2@aetheron.test")
        val bobResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getPlayerProfile(aliceUid)
        }
        assertTrue(bobResult.isFailure)
        val firestoreEx = bobResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun createAndFulfillTrade_validFlow_succeeds() = runBlocking {
        signInTestUser("trader_alice@aetheron.test")
        val repo = AetheronRepository(firestore)

        val tradeId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createTradeOffer(
                ownerName = "Alice",
                offeredSpeciesId = 3,
                offeredMonsterName = "Sylpharch",
                offeredLevel = 42,
                offeredIvRank = "S+",
                offeredTrait = "Aether-Krone",
                offeredTrainedStats = "ATK+60 / SPD+45",
                requestedSpeciesId = 83,
                requestedSpeciesName = "Ignisrex"
            ).getOrThrow()
        }

        val openTrades = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeOpenTrades().first { list -> list.any { it.id == tradeId } }
        }
        assertTrue(openTrades.any { it.id == tradeId })

        signInTestUser("trader_bob@aetheron.test")
        val fulfillResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.fulfillTradeOffer(tradeId)
        }
        assertTrue(fulfillResult.isSuccess)
    }

    @Test
    fun observeOpenTrades_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = AetheronRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeOpenTrades().first()
            }
        } catch (e: Throwable) {
            caught = e
        }
        assertNotNull(caught)
        val firestoreException = checkNotNull(caught?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $caught"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
