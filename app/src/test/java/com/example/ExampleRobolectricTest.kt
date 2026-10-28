package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.catalog.AetheronWorldCatalog
import com.example.domain.engine.DynamicQuestAndTrainingEngine
import com.example.domain.engine.TrainingExerciseType
import com.example.domain.model.MonsterInstance
import com.example.domain.model.TimeOfDay
import com.example.domain.model.WeatherType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context and verify 240 monsters and 24 badges`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aetheron", appName)

        // Verify 240 monster species (80 per continent) and 24 badges
        assertEquals(240, AetheronWorldCatalog.allSpecies.size)
        assertEquals(80, AetheronWorldCatalog.allSpecies.count { it.continentId == 1 })
        assertEquals(80, AetheronWorldCatalog.allSpecies.count { it.continentId == 2 })
        assertEquals(80, AetheronWorldCatalog.allSpecies.count { it.continentId == 3 })
        assertEquals(24, AetheronWorldCatalog.gymBadges.size)
    }

    @Test
    fun `verify interactive training minigame and dynamic quest generation`() {
        val mon = MonsterInstance(instanceId = "m1", speciesId = 1, nickname = "Sylphling", level = 12)
        val trainRes = DynamicQuestAndTrainingEngine.evaluateTrainingMiniGame(
            monster = mon,
            exercise = TrainingExerciseType.POWER_FORGE,
            scorePercent = 95,
            dojoSkillRank = 2,
            hasPowerBracer = true,
            factionBonusMultiplier = 1.2f
        )
        assertEquals("S", trainRes.gradeRank)
        assertTrue(trainRes.updatedMonster.trainedAtk > mon.trainedAtk)

        val quests = DynamicQuestAndTrainingEngine.generateDynamicQuests(
            continentId = 1,
            biome = AetheronWorldCatalog.biomes.first(),
            playerLevel = 15,
            completedQuestsCount = 4,
            factionStandings = mapOf("sylvaria_order" to 280),
            currentWeather = WeatherType.THUNDER_TEMPEST,
            currentTime = TimeOfDay.NIGHT
        )
        assertEquals(4, quests.size)
        assertTrue(quests.all { it.rewardGold > 0 && it.rewardXp > 0 })
    }
}
