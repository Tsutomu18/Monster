package com.example.battle

import com.example.model.ElementType
import com.example.model.Monster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleSystemTest {

    private val battleSystem = BattleSystem()

    @Test
    fun calculateEnvironmentModifiedStats_appliesWeatherAndGeneticTraitModifiers() {
        val pyroMonster = Monster(
            id = "pyro_1",
            name = "Ignisrex",
            elementType = ElementType.PYRO,
            attack = 80,
            defense = 60,
            speed = 70,
            stamina = 110,
            currentLevel = 20,
            genetics = mapOf(
                "ivRank" to "S",
                "Magma-Herz" to "+20% Pyro-Angriff bei Sonnenflut"
            )
        )

        val neutralStats = battleSystem.calculateEnvironmentModifiedStats(
            pyroMonster,
            BattleEnvironment.NEUTRAL
        )
        val solarFlareStats = battleSystem.calculateEnvironmentModifiedStats(
            pyroMonster,
            BattleEnvironment.SOLAR_FLARE
        )
        val rainStats = battleSystem.calculateEnvironmentModifiedStats(
            pyroMonster,
            BattleEnvironment.TROPICAL_RAIN
        )

        // In SOLAR_FLARE, PYRO attack and speed are boosted + Magma-Herz trait activates
        assertTrue(solarFlareStats.attack > neutralStats.attack)
        assertTrue(solarFlareStats.speed > neutralStats.speed)

        // In TROPICAL_RAIN, PYRO attack is weakened
        assertTrue(rainStats.attack < neutralStats.attack)
    }

    @Test
    fun calculateDamage_incorporatesElementalTypeAdvantageAndEnvironment() {
        val pyroAttacker = Monster(
            id = "atk_pyro",
            name = "Pyroclaw",
            elementType = ElementType.PYRO,
            attack = 75,
            defense = 55,
            speed = 68,
            stamina = 100,
            currentLevel = 15
        )
        val terraDefender = Monster(
            id = "def_terra",
            name = "Sylphling",
            elementType = ElementType.TERRA,
            attack = 55,
            defense = 55,
            speed = 55,
            stamina = 100,
            currentLevel = 15
        )
        val aquaDefender = Monster(
            id = "def_aqua",
            name = "Tidefin",
            elementType = ElementType.AQUA,
            attack = 55,
            defense = 55,
            speed = 55,
            stamina = 100,
            currentLevel = 15
        )

        val sessionVsTerra = battleSystem.startBattle(
            pyroAttacker,
            terraDefender,
            BattleEnvironment.SOLAR_FLARE
        )
        val sessionVsAqua = battleSystem.startBattle(
            pyroAttacker,
            aquaDefender,
            BattleEnvironment.TROPICAL_RAIN
        )

        val dmgVsTerra = battleSystem.calculateDamage(
            attacker = sessionVsTerra.combatantA,
            defender = sessionVsTerra.combatantB,
            action = BattleAction.ELEMENTAL_STRIKE,
            environment = BattleEnvironment.SOLAR_FLARE
        )
        val dmgVsAqua = battleSystem.calculateDamage(
            attacker = sessionVsAqua.combatantA,
            defender = sessionVsAqua.combatantB,
            action = BattleAction.ELEMENTAL_STRIKE,
            environment = BattleEnvironment.TROPICAL_RAIN
        )

        assertTrue(dmgVsTerra.isSuperEffective)
        assertTrue(dmgVsAqua.isNotVeryEffective)
        assertTrue(dmgVsTerra.finalDamage > dmgVsAqua.finalDamage)
    }

    @Test
    fun executeTurnAndSimulateBattle_resolvesTurnOrderDamageAndWinnerXp() {
        val voltMonster = Monster(
            id = "volt_fast",
            name = "Voltlynx",
            elementType = ElementType.VOLT,
            attack = 85,
            defense = 65,
            speed = 90,
            stamina = 120,
            currentLevel = 22,
            genetics = mapOf("ivRank" to "S+", "Blitz-Reflex" to "+15 Initiative")
        )
        val aquaMonster = Monster(
            id = "aqua_slow",
            name = "Aquashell",
            elementType = ElementType.AQUA,
            attack = 55,
            defense = 50,
            speed = 45,
            stamina = 100,
            currentLevel = 16,
            genetics = mapOf("ivRank" to "B")
        )

        val initialSession = battleSystem.startBattle(
            monsterA = voltMonster,
            monsterB = aquaMonster,
            environment = BattleEnvironment.THUNDER_TEMPEST
        )

        val firstTurn = battleSystem.executeTurn(
            session = initialSession,
            actionA = BattleAction.ELEMENTAL_STRIKE,
            actionB = BattleAction.ELEMENTAL_STRIKE
        )

        assertEquals("Voltlynx", firstTurn.firstMoverName)
        assertTrue(firstTurn.updatedSession.combatantB.currentHp < initialSession.combatantB.maxHp)

        val completedBattle = battleSystem.simulateFullBattle(
            monsterA = voltMonster,
            monsterB = aquaMonster,
            environment = BattleEnvironment.THUNDER_TEMPEST
        )

        assertTrue(completedBattle.isFinished)
        assertNotNull(completedBattle.winner)
        assertEquals("Voltlynx", completedBattle.winner?.name)
        assertTrue(completedBattle.xpAwardedToWinner > 0)
    }
}
