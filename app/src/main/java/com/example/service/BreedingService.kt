package com.example.service

import com.example.domain.catalog.AetheronWorldCatalog
import com.example.domain.model.GeneticTrait
import com.example.domain.model.MonsterInstance
import com.example.model.BaseStats
import com.example.model.ElementType
import com.example.model.Monster
import java.util.UUID
import kotlin.random.Random

/**
 * Service responsible for monster genetics calculations and breeding mechanics.
 * Calculates offspring stats based on parent genetics and defines logic for combining
 * trait Maps from two Monster instances.
 */
class BreedingService(
    private val random: Random = Random.Default
) {

    /**
     * Breeds two [Monster] instances to produce an offspring monster with:
     * - Combined genetic traits and calculated IV rank
     * - Inherited and scaled base stats (attack, defense, speed, stamina)
     * - Primary element inherited from either parent or elemental affinity
     * - Incremented generation counter
     */
    fun breed(
        parentA: Monster,
        parentB: Monster,
        offspringName: String? = null
    ): Monster {
        val combinedGenetics = combineTraitMaps(parentA.genetics, parentB.genetics)
        val offspringStats = calculateOffspringStats(parentA, parentB, combinedGenetics)

        val offspringElement = if (random.nextBoolean()) {
            parentA.elementType
        } else {
            parentB.elementType
        }

        val parentGenA = parentA.genetics["generation"]?.toIntOrNull() ?: 1
        val parentGenB = parentB.genetics["generation"]?.toIntOrNull() ?: 1
        val offspringGen = maxOf(parentGenA, parentGenB) + 1

        val finalGenetics = combinedGenetics.toMutableMap().apply {
            put("generation", offspringGen.toString())
        }

        val childName = offspringName ?: "${parentA.name.take(4)}${parentB.name.takeLast(4)} Jr."
        val childEvolutionLevel = minOf(parentA.evolutionLevel, parentB.evolutionLevel).coerceAtLeast(16)

        return Monster(
            id = "bred_${UUID.randomUUID().toString().take(8)}",
            name = childName,
            elementType = offspringElement,
            attack = offspringStats.attack,
            defense = offspringStats.defense,
            speed = offspringStats.speed,
            stamina = offspringStats.stamina,
            currentLevel = 5,
            experiencePoints = 0,
            genetics = finalGenetics,
            evolutionLevel = childEvolutionLevel,
            baseStats = offspringStats
        )
    }

    /**
     * Combines trait maps from two [Monster] instances.
     *
     * Logic:
     * 1. IV Rank is determined from the two parents with a chance of upgrading (e.g. S -> S+).
     * 2. Traits shared by both parents (common traits) are dominant and always inherited.
     * 3. Traits unique to either parent have a chance (typically 70%) to be passed down.
     * 4. A limit of up to 4 combat/passive traits is enforced (excluding metadata like "ivRank" and "generation").
     * 5. If both parents have S or S+ rank, a potential genetic mutation trait can be unlocked.
     */
    fun combineTraitMaps(
        traitsA: Map<String, String>,
        traitsB: Map<String, String>
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // 1. Calculate IV Rank
        val rankA = traitsA[KEY_IV_RANK] ?: "A"
        val rankB = traitsB[KEY_IV_RANK] ?: "A"
        val childIvRank = calculateOffspringIvRank(rankA, rankB)
        result[KEY_IV_RANK] = childIvRank

        // Extract non-metadata traits
        val customTraitsA = traitsA.filterKeys { it != KEY_IV_RANK && it != KEY_GENERATION }
        val customTraitsB = traitsB.filterKeys { it != KEY_IV_RANK && it != KEY_GENERATION }

        // 2. Shared traits are guaranteed to be passed down (dominant inheritance)
        val sharedKeys = customTraitsA.keys.intersect(customTraitsB.keys)
        for (key in sharedKeys) {
            result[key] = customTraitsA[key] ?: customTraitsB[key] ?: ""
        }

        // 3. Unique traits from Parent A
        val uniqueA = customTraitsA.keys - sharedKeys
        for (key in uniqueA) {
            if (result.size < MAX_TRAIT_COUNT && random.nextFloat() < TRAIT_INHERIT_CHANCE) {
                result[key] = customTraitsA[key] ?: ""
            }
        }

        // 4. Unique traits from Parent B
        val uniqueB = customTraitsB.keys - sharedKeys
        for (key in uniqueB) {
            if (result.size < MAX_TRAIT_COUNT && random.nextFloat() < TRAIT_INHERIT_CHANCE) {
                result[key] = customTraitsB[key] ?: ""
            }
        }

        // 5. High IV Rank Mutation Chance (S or S+ parents can awaken latent traits)
        if ((childIvRank == "S" || childIvRank == "S+") && result.size < MAX_TRAIT_COUNT) {
            val mutation = getLatentMutationTrait(childIvRank)
            if (mutation != null) {
                result[mutation.first] = mutation.second
            }
        }

        return result
    }

    /**
     * Calculates the offspring base stats (attack, defense, speed, stamina) based on parent stats
     * and parent genetics.
     *
     * Logic:
     * - Takes the average of each parent's base stats.
     * - Applies an IV Rank modifier: higher IV rank provides a bonus multiplier (+0% to +15%).
     * - Grants a genetic vigor bonus if parents have different primary elements (+5%).
     */
    fun calculateOffspringStats(
        parentA: Monster,
        parentB: Monster,
        combinedGenetics: Map<String, String>
    ): BaseStats {
        val ivRank = combinedGenetics[KEY_IV_RANK] ?: "A"
        val ivMultiplier = when (ivRank) {
            "S+" -> 1.15f
            "S" -> 1.10f
            "A" -> 1.05f
            "B" -> 1.00f
            else -> 0.95f
        }

        // Genetic vigor bonus if parents have different elements (hybrid vigor)
        val vigorBonus = if (parentA.elementType != parentB.elementType) 1.05f else 1.00f

        val totalMultiplier = ivMultiplier * vigorBonus

        val avgAtk = (parentA.attack + parentB.attack) / 2.0f
        val avgDef = (parentA.defense + parentB.defense) / 2.0f
        val avgSpd = (parentA.speed + parentB.speed) / 2.0f
        val avgSta = (parentA.stamina + parentB.stamina) / 2.0f

        return BaseStats(
            attack = (avgAtk * totalMultiplier).toInt().coerceAtLeast(10),
            defense = (avgDef * totalMultiplier).toInt().coerceAtLeast(10),
            speed = (avgSpd * totalMultiplier).toInt().coerceAtLeast(10),
            stamina = (avgSta * totalMultiplier).toInt().coerceAtLeast(20)
        )
    }

    /**
     * Determines the offspring IV rank from parent IV ranks.
     * Can upgrade by 1 rank tier if genetics align, up to "S+".
     */
    fun calculateOffspringIvRank(rankA: String, rankB: String): String {
        val rankOrder = listOf("C", "B", "A", "S", "S+")
        val idxA = rankOrder.indexOf(rankA).coerceAtLeast(0)
        val idxB = rankOrder.indexOf(rankB).coerceAtLeast(0)
        val maxIdx = maxOf(idxA, idxB)

        // 50% chance to upgrade to next tier if parents are at least B rank
        val shouldUpgrade = maxIdx >= 1 && random.nextFloat() < 0.50f
        val targetIdx = if (shouldUpgrade) (maxIdx + 1).coerceAtMost(rankOrder.lastIndex) else maxIdx
        return rankOrder[targetIdx]
    }

    private fun getLatentMutationTrait(ivRank: String): Pair<String, String>? {
        return when (ivRank) {
            "S+" -> "Aether-Resonanz" to "+20% Schaden aller Elemente und erhöhte kritische Trefferchance"
            "S" -> "Astral-Schutz" to "Verringert erlittenen Schaden um 10% und schützt vor Statusleiden"
            else -> null
        }
    }

    /**
     * Helper to bridge with the domain [MonsterInstance] runtime objects if needed.
     */
    fun breedDomainInstances(
        parentA: MonsterInstance,
        parentB: MonsterInstance
    ): MonsterInstance {
        val speciesA = AetheronWorldCatalog.getSpecies(parentA.speciesId)
        val speciesB = AetheronWorldCatalog.getSpecies(parentB.speciesId)

        val childSpecies = when {
            speciesA.isLegendary -> speciesA
            speciesB.isLegendary -> speciesB
            else -> {
                val baseId = ((parentA.speciesId - 1) / 3) * 3 + 1
                AetheronWorldCatalog.getSpecies(baseId.coerceIn(1, 240))
            }
        }

        val childRank = calculateOffspringIvRank(parentA.ivRank, parentB.ivRank)
        val childGen = maxOf(parentA.generation, parentB.generation) + 1

        val traitsMapA = buildMap {
            put(KEY_IV_RANK, parentA.ivRank)
            parentA.inheritedTraits.forEach { put(it.nameDe, it.descriptionDe) }
        }
        val traitsMapB = buildMap {
            put(KEY_IV_RANK, parentB.ivRank)
            parentB.inheritedTraits.forEach { put(it.nameDe, it.descriptionDe) }
        }

        val combinedTraitsMap = combineTraitMaps(traitsMapA, traitsMapB)

        val combinedTraitsList: List<GeneticTrait> = (parentA.inheritedTraits + parentB.inheritedTraits + childSpecies.signatureTrait)
            .distinctBy { it.id }
            .take(3)

        val child = MonsterInstance(
            instanceId = "bred_${UUID.randomUUID().toString().take(8)}",
            speciesId = childSpecies.id,
            nickname = "${childSpecies.nameDe} Gen-$childGen",
            level = ((parentA.level + parentB.level) / 3).coerceIn(5, 50),
            ivRank = childRank,
            inheritedTraits = combinedTraitsList,
            trainedAtk = ((parentA.trainedAtk + parentB.trainedAtk) / 3).coerceAtMost(120),
            trainedDef = ((parentA.trainedDef + parentB.trainedDef) / 3).coerceAtMost(120),
            trainedSpd = ((parentA.trainedSpd + parentB.trainedSpd) / 3).coerceAtMost(120),
            trainedElem = ((parentA.trainedElem + parentB.trainedElem) / 3).coerceAtMost(120),
            trainedHp = ((parentA.trainedHp + parentB.trainedHp) / 3).coerceAtMost(120),
            generation = childGen
        )
        return child.copy(currentHp = child.maxHp(childSpecies))
    }

    companion object {
        const val KEY_IV_RANK = "ivRank"
        const val KEY_GENERATION = "generation"
        const val MAX_TRAIT_COUNT = 4
        const val TRAIT_INHERIT_CHANCE = 0.75f
    }
}
