package com.example

import com.example.model.ElementType
import com.example.model.Monster
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
}
