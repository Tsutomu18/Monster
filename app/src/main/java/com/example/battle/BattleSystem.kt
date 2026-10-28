package com.example.battle

import com.example.domain.model.TimeOfDay
import com.example.domain.model.WeatherType
import com.example.model.BaseStats
import com.example.model.ElementType
import com.example.model.Monster
import kotlin.math.roundToInt

/**
 * Represents the active battlefield environment (weather, terrain, and celestial phase)
 * that modifies monster stats and elemental damage during turn-based combat.
 */
enum class BattleEnvironment(
    val displayNameDe: String,
    val descriptionDe: String,
    val boostedElements: Set<ElementType>,
    val weakenedElements: Set<ElementType>,
    val speedBoostedElements: Set<ElementType>,
    val defenseBoostedElements: Set<ElementType>,
    val staminaRegenPerTurn: Int = 8
) {
    NEUTRAL(
        displayNameDe = "Ausgeglichene Ebene",
        descriptionDe = "Stabile Aether-Bedingungen ohne elementare Verzerrung.",
        boostedElements = emptySet(),
        weakenedElements = emptySet(),
        speedBoostedElements = emptySet(),
        defenseBoostedElements = emptySet(),
        staminaRegenPerTurn = 10
    ),
    SOLAR_FLARE(
        displayNameDe = "Sonnenflut-Krater",
        descriptionDe = "Verstärkt Pyro- & Lux-Angriff (+25%) sowie Terra-Initiative (+15%); schwächt Cryo (-20%).",
        boostedElements = setOf(ElementType.PYRO, ElementType.LUX),
        weakenedElements = setOf(ElementType.CRYO, ElementType.AQUA),
        speedBoostedElements = setOf(ElementType.PYRO, ElementType.TERRA, ElementType.LUX),
        defenseBoostedElements = setOf(ElementType.TERRA),
        staminaRegenPerTurn = 12
    ),
    TROPICAL_RAIN(
        displayNameDe = "Tropischer Monsun",
        descriptionDe = "Verstärkt Aqua- & Terra-Angriff (+25%) und Aqua-Initiative (+20%); dämpft Pyro (-25%).",
        boostedElements = setOf(ElementType.AQUA, ElementType.TERRA),
        weakenedElements = setOf(ElementType.PYRO),
        speedBoostedElements = setOf(ElementType.AQUA),
        defenseBoostedElements = setOf(ElementType.AQUA, ElementType.TERRA),
        staminaRegenPerTurn = 12
    ),
    THUNDER_TEMPEST(
        displayNameDe = "Magnetischer Gewittersturm",
        descriptionDe = "Verstärkt Volt- & Aether-Angriff (+30%) und Volt-Geschwindigkeit (+25%); schwächt Terra (-15%).",
        boostedElements = setOf(ElementType.VOLT, ElementType.AETHER),
        weakenedElements = setOf(ElementType.TERRA),
        speedBoostedElements = setOf(ElementType.VOLT, ElementType.AETHER),
        defenseBoostedElements = setOf(ElementType.VOLT),
        staminaRegenPerTurn = 10
    ),
    GLACIER_BLIZZARD(
        displayNameDe = "Subpolarer Eissturm",
        descriptionDe = "Verstärkt Cryo-Angriff (+25%) und Cryo-Verteidigung (+25%); verlangsamt Terra & Pyro (-15%).",
        boostedElements = setOf(ElementType.CRYO),
        weakenedElements = setOf(ElementType.TERRA),
        speedBoostedElements = setOf(ElementType.CRYO),
        defenseBoostedElements = setOf(ElementType.CRYO, ElementType.AQUA),
        staminaRegenPerTurn = 7
    ),
    AETHER_NEXUS(
        displayNameDe = "Astraler Aether-Nexus",
        descriptionDe = "Kosmische Resonanz: +25% Angriff & +20% Verteidigung für Aether-, Umbra- und Lux-Monster.",
        boostedElements = setOf(ElementType.AETHER, ElementType.UMBRA, ElementType.LUX),
        weakenedElements = emptySet(),
        speedBoostedElements = setOf(ElementType.AETHER, ElementType.UMBRA),
        defenseBoostedElements = setOf(ElementType.AETHER, ElementType.LUX),
        staminaRegenPerTurn = 15
    );

    companion object {
        /**
         * Maps the world's dynamic [WeatherType] and [TimeOfDay] to a [BattleEnvironment].
         */
        fun fromWorldState(weather: WeatherType, timeOfDay: TimeOfDay): BattleEnvironment {
            return when (weather) {
                WeatherType.SOLAR_FLARE -> SOLAR_FLARE
                WeatherType.TROPICAL_RAIN -> TROPICAL_RAIN
                WeatherType.THUNDER_TEMPEST -> THUNDER_TEMPEST
                WeatherType.AETHER_MIST -> AETHER_NEXUS
                WeatherType.OBSIDIAN_SANDSTORM -> if (timeOfDay == TimeOfDay.NIGHT) AETHER_NEXUS else SOLAR_FLARE
            }
        }
    }
}

/**
 * Available combat actions in a turn-based battle.
 */
enum class BattleAction(
    val labelDe: String,
    val basePower: Int,
    val staminaCost: Int,
    val priorityBonus: Int = 0
) {
    QUICK_STRIKE("Schnellhieb", basePower = 45, staminaCost = 10, priorityBonus = 25),
    ELEMENTAL_STRIKE("Elementar-Salve", basePower = 75, staminaCost = 22, priorityBonus = 0),
    AETHER_OVERDRIVE("Aether-Overdrive", basePower = 110, staminaCost = 40, priorityBonus = -5),
    GUARD_RECOVER("Aegis-Fokus (Block & Erholung)", basePower = 0, staminaCost = 0, priorityBonus = 50)
}

/**
 * Holds the runtime combat state of a participating [Monster].
 */
data class BattleCombatant(
    val monster: Monster,
    val maxHp: Int,
    val currentHp: Int,
    val maxStamina: Int,
    val currentStamina: Int,
    val effectiveStats: BaseStats,
    val isGuarding: Boolean = false
) {
    val isFainted: Boolean
        get() = currentHp <= 0
}

/**
 * Detailed breakdown of a single attack's damage calculation.
 */
data class DamageCalculationResult(
    val rawDamage: Int,
    val finalDamage: Int,
    val typeEffectivenessMultiplier: Float,
    val environmentMultiplier: Float,
    val isSuperEffective: Boolean,
    val isNotVeryEffective: Boolean,
    val isCriticalHit: Boolean
)

/**
 * Immutable snapshot of an ongoing or completed turn-based battle between two [Monster] instances.
 */
data class BattleSession(
    val turnNumber: Int = 1,
    val environment: BattleEnvironment = BattleEnvironment.NEUTRAL,
    val combatantA: BattleCombatant,
    val combatantB: BattleCombatant,
    val combatLog: List<String> = emptyList(),
    val isFinished: Boolean = false,
    val winner: Monster? = null,
    val xpAwardedToWinner: Int = 0
)

/**
 * Result returned after executing a single turn in [BattleSystem].
 */
data class BattleTurnResult(
    val updatedSession: BattleSession,
    val firstMoverName: String,
    val turnEvents: List<String>
)

/**
 * Turn-based combat engine managing battles between two [Monster] instances,
 * incorporating elemental type advantages, genetic traits, and environmental stat modifiers.
 */
class BattleSystem {

    /**
     * Initializes a new [BattleSession] between [monsterA] and [monsterB] under [environment].
     */
    fun startBattle(
        monsterA: Monster,
        monsterB: Monster,
        environment: BattleEnvironment = BattleEnvironment.NEUTRAL
    ): BattleSession {
        val combatantA = createCombatant(monsterA, environment)
        val combatantB = createCombatant(monsterB, environment)
        val initialLog = listOf(
            "Kampf gestartet: ${monsterA.name} (Lv.${monsterA.currentLevel}, ${monsterA.elementType.displayName}) vs. ${monsterB.name} (Lv.${monsterB.currentLevel}, ${monsterB.elementType.displayName})!",
            "Umgebung: ${environment.displayNameDe} — ${environment.descriptionDe}"
        )
        return BattleSession(
            turnNumber = 1,
            environment = environment,
            combatantA = combatantA,
            combatantB = combatantB,
            combatLog = initialLog
        )
    }

    /**
     * Computes the environment-modified combat stats (`attack`, `defense`, `speed`, `stamina`)
     * for [monster] in the given [environment], factoring in elemental resonance and genetic traits.
     */
    fun calculateEnvironmentModifiedStats(
        monster: Monster,
        environment: BattleEnvironment
    ): BaseStats {
        var atkMult = 1.0f
        var defMult = 1.0f
        var spdMult = 1.0f
        var staMult = 1.0f

        // 1. Elemental environment modifiers
        if (monster.elementType in environment.boostedElements) {
            atkMult += 0.25f
        }
        if (monster.elementType in environment.weakenedElements) {
            atkMult -= 0.20f
        }
        if (monster.elementType in environment.defenseBoostedElements) {
            defMult += 0.20f
        }
        if (monster.elementType in environment.speedBoostedElements) {
            spdMult += 0.20f
        }

        // 2. Genetic trait synergies with environment
        val traitKeys = monster.genetics.keys
        if (environment == BattleEnvironment.SOLAR_FLARE) {
            if (traitKeys.any { it.contains("Solar", ignoreCase = true) }) {
                staMult += 0.15f
                spdMult += 0.10f
            }
            if (traitKeys.any { it.contains("Magma", ignoreCase = true) || it.contains("Glut", ignoreCase = true) }) {
                atkMult += 0.20f
            }
        }
        if (environment == BattleEnvironment.THUNDER_TEMPEST &&
            traitKeys.any { it.contains("Blitz", ignoreCase = true) || it.contains("Sturm", ignoreCase = true) }
        ) {
            spdMult += 0.20f
        }
        if (traitKeys.any { it.contains("Aether-Resonanz", ignoreCase = true) }) {
            atkMult += 0.15f
            defMult += 0.10f
        }

        return BaseStats(
            attack = (monster.effectiveAttack * atkMult).roundToInt().coerceAtLeast(5),
            defense = (monster.effectiveDefense * defMult).roundToInt().coerceAtLeast(5),
            speed = (monster.effectiveSpeed * spdMult).roundToInt().coerceAtLeast(5),
            stamina = (monster.effectiveStamina * staMult).roundToInt().coerceAtLeast(20)
        )
    }

    /**
     * Calculates the elemental type advantage multiplier when [attackerElement] hits [defenderElement].
     */
    fun calculateTypeAdvantage(
        attackerElement: ElementType,
        defenderElement: ElementType
    ): Float {
        return attackerElement.effectivenessAgainst(defenderElement)
    }

    /**
     * Calculates the exact damage dealt by [attacker] to [defender] using [action] in [environment].
     */
    fun calculateDamage(
        attacker: BattleCombatant,
        defender: BattleCombatant,
        action: BattleAction,
        environment: BattleEnvironment
    ): DamageCalculationResult {
        if (action == BattleAction.GUARD_RECOVER || action.basePower <= 0) {
            return DamageCalculationResult(
                rawDamage = 0,
                finalDamage = 0,
                typeEffectivenessMultiplier = 1.0f,
                environmentMultiplier = 1.0f,
                isSuperEffective = false,
                isNotVeryEffective = false,
                isCriticalHit = false
            )
        }

        val typeMult = calculateTypeAdvantage(attacker.monster.elementType, defender.monster.elementType)
        val envMult = when (attacker.monster.elementType) {
            in environment.boostedElements -> 1.25f
            in environment.weakenedElements -> 0.80f
            else -> 1.0f
        }

        val effectiveAtk = attacker.effectiveStats.attack.toFloat()
        val effectiveDef = (defender.effectiveStats.defense * (if (defender.isGuarding) 1.65f else 1.0f))
            .coerceAtLeast(8f)

        val levelFactor = 1.0f + (attacker.monster.currentLevel * 0.03f)
        val baseRaw = (action.basePower * (effectiveAtk / effectiveDef) * 0.55f * levelFactor) + 6f

        val isCrit = attacker.monster.genetics.keys.any {
            it.contains("Resonanz", ignoreCase = true) || it.contains("Krit", ignoreCase = true)
        }
        val critMult = if (isCrit) 1.25f else 1.0f

        val finalDmg = (baseRaw * typeMult * envMult * critMult).roundToInt().coerceAtLeast(4)

        return DamageCalculationResult(
            rawDamage = baseRaw.roundToInt(),
            finalDamage = finalDmg,
            typeEffectivenessMultiplier = typeMult,
            environmentMultiplier = envMult,
            isSuperEffective = typeMult > 1.05f,
            isNotVeryEffective = typeMult < 0.95f,
            isCriticalHit = isCrit
        )
    }

    /**
     * Executes a single turn of combat between `combatantA` and `combatantB`.
     * Turn order is determined by environment-modified speed plus action priority and genetic traits.
     */
    fun executeTurn(
        session: BattleSession,
        actionA: BattleAction = BattleAction.ELEMENTAL_STRIKE,
        actionB: BattleAction = selectTacticalAiAction(session.combatantB)
    ): BattleTurnResult {
        if (session.isFinished) {
            return BattleTurnResult(
                updatedSession = session,
                firstMoverName = session.combatantA.monster.name,
                turnEvents = emptyList()
            )
        }

        val events = mutableListOf<String>()
        val env = session.environment

        // Reset guard flags at start of turn and apply Guard stance if selected
        var stateA = session.combatantA.copy(
            effectiveStats = calculateEnvironmentModifiedStats(session.combatantA.monster, env),
            isGuarding = actionA == BattleAction.GUARD_RECOVER
        )
        var stateB = session.combatantB.copy(
            effectiveStats = calculateEnvironmentModifiedStats(session.combatantB.monster, env),
            isGuarding = actionB == BattleAction.GUARD_RECOVER
        )

        val initA = stateA.effectiveStats.speed + actionA.priorityBonus +
            (if (stateA.monster.genetics.containsKey("Blitz-Reflex")) 15 else 0)
        val initB = stateB.effectiveStats.speed + actionB.priorityBonus +
            (if (stateB.monster.genetics.containsKey("Blitz-Reflex")) 15 else 0)

        val aMovesFirst = initA >= initB
        val firstMoverName = if (aMovesFirst) stateA.monster.name else stateB.monster.name

        if (aMovesFirst) {
            val (nextA, nextB) = performSingleAction(stateA, stateB, actionA, env, events)
            stateA = nextA
            stateB = nextB
            if (!stateB.isFainted) {
                val (afterB, afterA) = performSingleAction(stateB, stateA, actionB, env, events)
                stateB = afterB
                stateA = afterA
            }
        } else {
            val (nextB, nextA) = performSingleAction(stateB, stateA, actionB, env, events)
            stateB = nextB
            stateA = nextA
            if (!stateA.isFainted) {
                val (afterA, afterB) = performSingleAction(stateA, stateB, actionA, env, events)
                stateA = afterA
                stateB = afterB
            }
        }

        // End-of-turn environment stamina regeneration
        if (!stateA.isFainted) {
            stateA = stateA.copy(
                currentStamina = (stateA.currentStamina + env.staminaRegenPerTurn).coerceAtMost(stateA.maxStamina)
            )
        }
        if (!stateB.isFainted) {
            stateB = stateB.copy(
                currentStamina = (stateB.currentStamina + env.staminaRegenPerTurn).coerceAtMost(stateB.maxStamina)
            )
        }

        val battleEnded = stateA.isFainted || stateB.isFainted
        var winnerMonster: Monster? = null
        var xpAwarded = 0

        if (battleEnded) {
            if (stateB.isFainted) {
                xpAwarded = 50 + (stateB.monster.currentLevel * 15)
                winnerMonster = stateA.monster.gainExperience(xpAwarded)
                stateA = stateA.copy(monster = winnerMonster)
                events.add("🏆 ${stateA.monster.name} gewinnt den Kampf und erhält +$xpAwarded EP!")
            } else {
                xpAwarded = 50 + (stateA.monster.currentLevel * 15)
                winnerMonster = stateB.monster.gainExperience(xpAwarded)
                stateB = stateB.copy(monster = winnerMonster)
                events.add("🏆 ${stateB.monster.name} gewinnt den Kampf und erhält +$xpAwarded EP!")
            }
        }

        val updatedSession = session.copy(
            turnNumber = session.turnNumber + 1,
            combatantA = stateA,
            combatantB = stateB,
            combatLog = (session.combatLog + events).takeLast(14),
            isFinished = battleEnded,
            winner = winnerMonster,
            xpAwardedToWinner = xpAwarded
        )

        return BattleTurnResult(
            updatedSession = updatedSession,
            firstMoverName = firstMoverName,
            turnEvents = events
        )
    }

    /**
     * Changes the active [BattleEnvironment] mid-battle and recalculates both combatants' stats.
     */
    fun updateEnvironment(
        session: BattleSession,
        newEnvironment: BattleEnvironment
    ): BattleSession {
        val updatedA = session.combatantA.copy(
            effectiveStats = calculateEnvironmentModifiedStats(session.combatantA.monster, newEnvironment)
        )
        val updatedB = session.combatantB.copy(
            effectiveStats = calculateEnvironmentModifiedStats(session.combatantB.monster, newEnvironment)
        )
        return session.copy(
            environment = newEnvironment,
            combatantA = updatedA,
            combatantB = updatedB,
            combatLog = (session.combatLog + "Umgebungswechsel: ${newEnvironment.displayNameDe} aktiv!").takeLast(14)
        )
    }

    /**
     * Simulates a complete turn-based battle up to [maxTurns] and returns the final [BattleSession].
     */
    fun simulateFullBattle(
        monsterA: Monster,
        monsterB: Monster,
        environment: BattleEnvironment = BattleEnvironment.NEUTRAL,
        maxTurns: Int = 30
    ): BattleSession {
        var current = startBattle(monsterA, monsterB, environment)
        var turns = 0
        while (!current.isFinished && turns < maxTurns) {
            val actionA = selectTacticalAiAction(current.combatantA)
            val actionB = selectTacticalAiAction(current.combatantB)
            current = executeTurn(current, actionA, actionB).updatedSession
            turns++
        }
        return current
    }

    private fun performSingleAction(
        attacker: BattleCombatant,
        defender: BattleCombatant,
        requestedAction: BattleAction,
        environment: BattleEnvironment,
        events: MutableList<String>
    ): Pair<BattleCombatant, BattleCombatant> {
        // Fallback to GUARD_RECOVER if stamina is insufficient for the chosen attack
        val effectiveAction = if (attacker.currentStamina < requestedAction.staminaCost) {
            BattleAction.GUARD_RECOVER
        } else {
            requestedAction
        }

        if (effectiveAction == BattleAction.GUARD_RECOVER) {
            val recoveredStamina = (attacker.currentStamina + 35).coerceAtMost(attacker.maxStamina)
            val healedHp = (attacker.currentHp + (attacker.maxHp * 0.08f).roundToInt()).coerceAtMost(attacker.maxHp)
            events.add("${attacker.monster.name} nutzt ${effectiveAction.labelDe} (+35 Ausdauer, Verteidigung erhöht).")
            return attacker.copy(
                currentStamina = recoveredStamina,
                currentHp = healedHp,
                isGuarding = true
            ) to defender
        }

        val dmgResult = calculateDamage(attacker, defender, effectiveAction, environment)
        val newDefenderHp = (defender.currentHp - dmgResult.finalDamage).coerceAtLeast(0)
        val newAttackerStamina = (attacker.currentStamina - effectiveAction.staminaCost).coerceAtLeast(0)

        val effectivenessNote = when {
            dmgResult.isSuperEffective -> " (Sehr effektiv! ×${dmgResult.typeEffectivenessMultiplier})"
            dmgResult.isNotVeryEffective -> " (Wenig effektiv... ×${dmgResult.typeEffectivenessMultiplier})"
            else -> ""
        }
        events.add(
            "${attacker.monster.name} setzt ${effectiveAction.labelDe} ein → ${dmgResult.finalDamage} Schaden an ${defender.monster.name}$effectivenessNote!"
        )

        return attacker.copy(currentStamina = newAttackerStamina) to
            defender.copy(currentHp = newDefenderHp)
    }

    private fun selectTacticalAiAction(combatant: BattleCombatant): BattleAction {
        return when {
            combatant.currentStamina < BattleAction.QUICK_STRIKE.staminaCost -> BattleAction.GUARD_RECOVER
            combatant.currentStamina >= BattleAction.AETHER_OVERDRIVE.staminaCost -> BattleAction.AETHER_OVERDRIVE
            combatant.currentStamina >= BattleAction.ELEMENTAL_STRIKE.staminaCost -> BattleAction.ELEMENTAL_STRIKE
            else -> BattleAction.QUICK_STRIKE
        }
    }

    private fun createCombatant(
        monster: Monster,
        environment: BattleEnvironment
    ): BattleCombatant {
        val modifiedStats = calculateEnvironmentModifiedStats(monster, environment)
        val computedMaxHp = 90 + (monster.currentLevel * 8) + (modifiedStats.defense / 2)
        return BattleCombatant(
            monster = monster,
            maxHp = computedMaxHp,
            currentHp = computedMaxHp,
            maxStamina = modifiedStats.stamina,
            currentStamina = modifiedStats.stamina,
            effectiveStats = modifiedStats
        )
    }
}
