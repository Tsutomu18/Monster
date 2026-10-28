package com.example.training

import com.example.model.BaseStats
import com.example.model.ElementType
import com.example.model.Monster
import kotlin.math.roundToInt

/**
 * Enumerates the specific base stats of a [Monster] that a [TrainingActivity] can target.
 */
enum class TrainingTargetStat(val labelDe: String, val shortCode: String) {
    ATTACK("Angriff", "ATK"),
    DEFENSE("Verteidigung", "DEF"),
    SPEED("Geschwindigkeit", "SPD"),
    STAMINA("Ausdauer", "STA")
}

/**
 * Encapsulates the result of completing a [TrainingActivity] mini-game with a [Monster].
 */
data class TrainingOutcome(
    val activityId: String,
    val activityTitleDe: String,
    val targetStat: TrainingTargetStat,
    val secondaryStat: TrainingTargetStat,
    val performanceScore: Int,
    val gradeRank: String,
    val primaryStatBoost: Int,
    val secondaryStatBoost: Int,
    val xpEarned: Int,
    val updatedMonster: Monster,
    val evolutionReadyAfterTraining: Boolean
)

/**
 * Interface defining a mini-game training activity that boosts specific [Monster] stats
 * based on player performance (0..100) and monster genetics/elemental affinity.
 */
interface TrainingActivity {
    val id: String
    val titleDe: String
    val descriptionDe: String
    val targetStat: TrainingTargetStat
    val secondaryStat: TrainingTargetStat
    val staminaCost: Int
    val favoredElements: Set<ElementType>

    /**
     * Evaluates the performance score (0..100) into a rank tier ("S+", "S", "A", "B", "C").
     */
    fun evaluateGrade(performanceScore: Int): String {
        val clamped = performanceScore.coerceIn(0, 100)
        return when {
            clamped >= 96 -> "S+"
            clamped >= 88 -> "S"
            clamped >= 75 -> "A"
            clamped >= 55 -> "B"
            else -> "C"
        }
    }

    /**
     * Calculates the `(primaryStatBoost, secondaryStatBoost)` earned by [monster]
     * for a given [performanceScore] (0..100).
     */
    fun calculateStatBoost(monster: Monster, performanceScore: Int): Pair<Int, Int> {
        val grade = evaluateGrade(performanceScore)
        val basePrimary = when (grade) {
            "S+" -> 14
            "S" -> 11
            "A" -> 8
            "B" -> 5
            else -> 3
        }
        val baseSecondary = when (grade) {
            "S+" -> 8
            "S" -> 6
            "A" -> 4
            "B" -> 3
            else -> 1
        }

        // Elemental resonance bonus (+20% if the monster's element matches the training dojo)
        val elementBonus = if (monster.elementType in favoredElements) 1.20f else 1.0f

        // Genetic IV rank training efficiency bonus
        val geneticEfficiency = when (monster.genetics["ivRank"]) {
            "S+" -> 1.25f
            "S" -> 1.15f
            "A" -> 1.08f
            else -> 1.0f
        }

        val primaryBoost = (basePrimary * elementBonus * geneticEfficiency).roundToInt().coerceAtLeast(1)
        val secondaryBoost = (baseSecondary * elementBonus * geneticEfficiency).roundToInt().coerceAtLeast(1)
        return primaryBoost to secondaryBoost
    }

    /**
     * Calculates the experience points awarded for completing the mini-game.
     */
    fun calculateXpReward(monster: Monster, performanceScore: Int): Int {
        val clamped = performanceScore.coerceIn(0, 100)
        val affinityMult = if (monster.elementType in favoredElements) 1.15f else 1.0f
        return ((30 + clamped * 0.85f) * affinityMult).roundToInt()
    }

    /**
     * Executes the training activity on [monster] with [performanceScore] (0..100)
     * and returns the resulting [TrainingOutcome] containing the updated [Monster].
     */
    fun executeTraining(monster: Monster, performanceScore: Int): TrainingOutcome {
        val clampedScore = performanceScore.coerceIn(0, 100)
        val grade = evaluateGrade(clampedScore)
        val (primaryBoost, secondaryBoost) = calculateStatBoost(monster, clampedScore)
        val xpEarned = calculateXpReward(monster, clampedScore)

        var atkDelta = 0
        var defDelta = 0
        var spdDelta = 0
        var staDelta = 0

        fun addDelta(stat: TrainingTargetStat, amount: Int) {
            when (stat) {
                TrainingTargetStat.ATTACK -> atkDelta += amount
                TrainingTargetStat.DEFENSE -> defDelta += amount
                TrainingTargetStat.SPEED -> spdDelta += amount
                TrainingTargetStat.STAMINA -> staDelta += amount
            }
        }

        addDelta(targetStat, primaryBoost)
        addDelta(secondaryStat, secondaryBoost)

        val updatedMonster = monster.applyTrainingBoost(
            attackDelta = atkDelta,
            defenseDelta = defDelta,
            speedDelta = spdDelta,
            staminaDelta = staDelta,
            xpGained = xpEarned,
            completedActivityId = id
        )

        return TrainingOutcome(
            activityId = id,
            activityTitleDe = titleDe,
            targetStat = targetStat,
            secondaryStat = secondaryStat,
            performanceScore = clampedScore,
            gradeRank = grade,
            primaryStatBoost = primaryBoost,
            secondaryStatBoost = secondaryBoost,
            xpEarned = xpEarned,
            updatedMonster = updatedMonster,
            evolutionReadyAfterTraining = updatedMonster.isReadyToEvolve
        )
    }
}

/**
 * Mini-Game 1: Power Forge (Timing Pendulum Strike) — Boosts Attack & Stamina.
 */
object PowerForgeActivity : TrainingActivity {
    override val id: String = "power_forge"
    override val titleDe: String = "Kraft-Schmiede (Timing-Schlag)"
    override val descriptionDe: String =
        "Triff den Aether-Amboss im goldenen Fokus-Fenster (75%–95%), um Angriff und Ausdauer massiv zu steigern."
    override val targetStat: TrainingTargetStat = TrainingTargetStat.ATTACK
    override val secondaryStat: TrainingTargetStat = TrainingTargetStat.STAMINA
    override val staminaCost: Int = 15
    override val favoredElements: Set<ElementType> = setOf(ElementType.PYRO, ElementType.TERRA)
}

/**
 * Mini-Game 2: Aegis Reflex (4-Sector Shield Parry) — Boosts Defense & Stamina.
 */
object AegisReflexActivity : TrainingActivity {
    override val id: String = "aegis_reflex"
    override val titleDe: String = "Aegis-Reflex (Schild-Parade)"
    override val descriptionDe: String =
        "Blocke Elementar-Salven aus 4 Himmelsrichtungen, um Verteidigung und Standfestigkeit zu härten."
    override val targetStat: TrainingTargetStat = TrainingTargetStat.DEFENSE
    override val secondaryStat: TrainingTargetStat = TrainingTargetStat.STAMINA
    override val staminaCost: Int = 12
    override val favoredElements: Set<ElementType> = setOf(ElementType.TERRA, ElementType.CRYO, ElementType.AQUA)
}

/**
 * Mini-Game 3: Aether Sprint (Rune Sequence 1..6) — Boosts Speed & Attack.
 */
object AetherSprintActivity : TrainingActivity {
    override val id: String = "aether_sprint"
    override val titleDe: String = "Aether-Sprint (Runen-Sequenz)"
    override val descriptionDe: String =
        "Aktiviere die 6 Geschwindigkeits-Runen in aufsteigender Reihenfolge für maximale Initiative."
    override val targetStat: TrainingTargetStat = TrainingTargetStat.SPEED
    override val secondaryStat: TrainingTargetStat = TrainingTargetStat.ATTACK
    override val staminaCost: Int = 14
    override val favoredElements: Set<ElementType> = setOf(ElementType.VOLT, ElementType.AETHER, ElementType.LUX)
}

/**
 * Mini-Game 4: Astral Resonance Breathing — Boosts Stamina & Defense.
 */
object AstralEnduranceActivity : TrainingActivity {
    override val id: String = "astral_endurance"
    override val titleDe: String = "Astral-Atem (Ausdauer-Fokus)"
    override val descriptionDe: String =
        "Synchronisiere den Aether-Puls, um die maximale Ausdauer (Stamina) und Abwehrkraft dauerhaft zu erhöhen."
    override val targetStat: TrainingTargetStat = TrainingTargetStat.STAMINA
    override val secondaryStat: TrainingTargetStat = TrainingTargetStat.DEFENSE
    override val staminaCost: Int = 10
    override val favoredElements: Set<ElementType> = setOf(ElementType.AETHER, ElementType.AQUA, ElementType.UMBRA)
}

/**
 * Registry of all available [TrainingActivity] mini-games.
 */
object TrainingActivityCatalog {
    val allActivities: List<TrainingActivity> = listOf(
        PowerForgeActivity,
        AegisReflexActivity,
        AetherSprintActivity,
        AstralEnduranceActivity
    )

    fun findById(id: String): TrainingActivity =
        allActivities.find { it.id == id } ?: PowerForgeActivity
}
