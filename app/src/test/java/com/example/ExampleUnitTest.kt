package com.example

import com.example.model.ElementType
import com.example.model.Monster
import com.example.training.AegisReflexActivity
import com.example.training.AetherSprintActivity
import com.example.training.AstralEnduranceActivity
import com.example.training.PowerForgeActivity
import com.example.training.TrainingTargetStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun monsterModel_fieldsGeneticsAndEvolution_workCorrectly() {
        val monster = Monster(
            id = "mon_pyro_01",
            name = "Pyrokit",
            elementType = ElementType.PYRO,
            attack = 64,
            defense = 45,
            speed = 58,
            stamina = 110,
            currentLevel = 14,
            experiencePoints = 600,
            genetics = mapOf(
                "ivRank" to "S",
                "Magma-Herz" to "+20% Pyro-Angriff bei Vulkanhitze",
                "Blitz-Reflex" to "+12% Initiative im ersten Zug"
            ),
            evolutionLevel = 16
        )

        assertEquals("mon_pyro_01", monster.id)
        assertEquals("Pyrokit", monster.name)
        assertEquals(ElementType.PYRO, monster.elementType)
        assertEquals(64, monster.attack)
        assertEquals(45, monster.defense)
        assertEquals(58, monster.speed)
        assertEquals(110, monster.stamina)
        assertEquals(14, monster.currentLevel)
        assertEquals(600, monster.experiencePoints)
        assertEquals(3, monster.genetics.size)
        assertEquals(16, monster.evolutionLevel)
        assertFalse(monster.isReadyToEvolve)

        // Gain XP to cross evolutionLevel (16)
        val leveledMonster = monster.gainExperience(1000)
        assertTrue(leveledMonster.currentLevel >= 16)
        assertTrue(leveledMonster.isReadyToEvolve)
        assertTrue(ElementType.PYRO.effectivenessAgainst(ElementType.TERRA) > 1.0f)
    }

    @Test
    fun trainingActivity_boostsSpecificStatsAndIntegratesWithMonster() {
        val initial = Monster(
            id = "mon_train_01",
            name = "Pyrokit",
            elementType = ElementType.PYRO,
            attack = 60,
            defense = 50,
            speed = 55,
            stamina = 100,
            currentLevel = 15,
            experiencePoints = 720,
            genetics = mapOf("ivRank" to "S+"),
            evolutionLevel = 16
        )

        // 1. Power Forge Activity (Attack + Stamina)
        val forgeOutcome = initial.performTraining(PowerForgeActivity, performanceScore = 97)
        assertEquals("S+", forgeOutcome.gradeRank)
        assertEquals(TrainingTargetStat.ATTACK, forgeOutcome.targetStat)
        assertTrue(forgeOutcome.primaryStatBoost > 0)
        assertTrue(forgeOutcome.updatedMonster.attack > initial.attack)
        assertTrue(forgeOutcome.updatedMonster.stamina > initial.stamina)
        assertTrue(forgeOutcome.evolutionReadyAfterTraining)
        assertEquals("power_forge", forgeOutcome.updatedMonster.genetics["lastTrainingActivity"])

        // 2. Aegis Reflex Activity (Defense + Stamina)
        val aegisOutcome = forgeOutcome.updatedMonster.performTraining(AegisReflexActivity, performanceScore = 90)
        assertEquals("S", aegisOutcome.gradeRank)
        assertTrue(aegisOutcome.updatedMonster.defense > forgeOutcome.updatedMonster.defense)

        // 3. Aether Sprint Activity (Speed + Attack)
        val sprintOutcome = aegisOutcome.updatedMonster.performTraining(AetherSprintActivity, performanceScore = 82)
        assertEquals("A", sprintOutcome.gradeRank)
        assertTrue(sprintOutcome.updatedMonster.speed > aegisOutcome.updatedMonster.speed)

        // 4. Astral Endurance Activity (Stamina + Defense)
        val enduranceOutcome = sprintOutcome.updatedMonster.performTraining(AstralEnduranceActivity, performanceScore = 92)
        assertTrue(enduranceOutcome.updatedMonster.stamina > sprintOutcome.updatedMonster.stamina)
    }
}
