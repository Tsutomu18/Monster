package com.example.service

import com.example.model.ElementType
import com.example.model.Monster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BreedingServiceTest {

    private val service = BreedingService(random = Random(42))

    @Test
    fun combineTraitMaps_inheritsSharedTraitsAndCalculatesIvRank() {
        val traitsParentA = mapOf(
            "ivRank" to "A",
            "Solar-Ader" to "+15% Ausdauer-Regeneration bei Sonne",
            "Flammen-Panzer" to "+10% Verteidigung gegen Pyro"
        )
        val traitsParentB = mapOf(
            "ivRank" to "S",
            "Solar-Ader" to "+15% Ausdauer-Regeneration bei Sonne",
            "Sturm-Reflex" to "+12% Initiative"
        )

        val combined = service.combineTraitMaps(traitsParentA, traitsParentB)

        // Shared trait must be inherited
        assertTrue("Shared trait must be in combined genetics", combined.containsKey("Solar-Ader"))
        assertEquals("+15% Ausdauer-Regeneration bei Sonne", combined["Solar-Ader"])

        // IV rank must be at least the max of parent ranks (S or S+)
        val ivRank = combined["ivRank"]
        assertNotNull(ivRank)
        assertTrue(ivRank == "S" || ivRank == "S+")
    }

    @Test
    fun calculateOffspringStats_computesAveragesWithGeneticsMultipliers() {
        val parentA = Monster(
            id = "mon_1",
            name = "TerraMon",
            elementType = ElementType.TERRA,
            attack = 60,
            defense = 40,
            speed = 50,
            stamina = 100,
            genetics = mapOf("ivRank" to "A")
        )
        val parentB = Monster(
            id = "mon_2",
            name = "PyroMon",
            elementType = ElementType.PYRO,
            attack = 80,
            defense = 60,
            speed = 70,
            stamina = 120,
            genetics = mapOf("ivRank" to "S")
        )

        val combinedGenetics = mapOf("ivRank" to "S")
        val stats = service.calculateOffspringStats(parentA, parentB, combinedGenetics)

        // Average raw: atk=70, def=50, spd=60, sta=110
        // S rank multiplier = 1.10, hybrid vigor (different elements) = 1.05 -> total ~ 1.155
        assertTrue(stats.attack >= 70)
        assertTrue(stats.defense >= 50)
        assertTrue(stats.speed >= 60)
        assertTrue(stats.stamina >= 110)
    }

    @Test
    fun breed_createsValidOffspringWithCombinedGeneticsAndStats() {
        val parentA = Monster(
            id = "parent_1",
            name = "Sylphox",
            elementType = ElementType.TERRA,
            attack = 55,
            defense = 50,
            speed = 60,
            stamina = 100,
            genetics = mapOf("ivRank" to "A", "generation" to "1", "Natur-Schutz" to "+15% Terra-Resistenz"),
            evolutionLevel = 16
        )
        val parentB = Monster(
            id = "parent_2",
            name = "Pyroling",
            elementType = ElementType.PYRO,
            attack = 65,
            defense = 45,
            speed = 55,
            stamina = 110,
            genetics = mapOf("ivRank" to "A", "generation" to "1", "Glut-Herz" to "+10% Pyro-Schaden"),
            evolutionLevel = 18
        )

        val child = service.breed(parentA, parentB, offspringName = "Sylph-Pyro Junior")

        assertEquals("Sylph-Pyro Junior", child.name)
        assertEquals(5, child.currentLevel)
        assertEquals(0, child.experiencePoints)
        assertTrue(child.attack > 0)
        assertTrue(child.defense > 0)
        assertTrue(child.speed > 0)
        assertTrue(child.stamina > 0)
        assertEquals("2", child.genetics["generation"])
        assertNotNull(child.genetics["ivRank"])
    }
}
