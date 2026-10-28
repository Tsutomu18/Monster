package com.example.model

import com.example.domain.catalog.AetheronWorldCatalog
import com.example.domain.model.MonsterInstance
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Enum representing elemental affinities for monsters in Aetheron.
 */
enum class ElementType(
    val displayName: String,
    val colorHex: Long,
    val description: String
) {
    PYRO("Pyro", 0xFFFF6B35, "Vulkanische Flammen- und Magma-Essenz"),
    AQUA("Aqua", 0xFF00B4D8, "Tiefsee- und Gezeiten-Essenz"),
    TERRA("Terra", 0xFF52B788, "Uralte Natur- und Kristallwald-Essenz"),
    VOLT("Volt", 0xFFFFBE0B, "Sturm- und Magnetfeld-Essenz"),
    CRYO("Cryo", 0xFF90E0EF, "Subpolare Gletscher- und Frost-Essenz"),
    AETHER("Aether", 0xFF9D4EDD, "Kosmische Astral- und Resonanz-Essenz"),
    UMBRA("Umbra", 0xFF6C757D, "Schatten- und Gravitations-Essenz"),
    LUX("Lux", 0xFFFFEAA7, "Solare Licht- und Prisma-Essenz");

    /**
     * Calculates the elemental damage multiplier when attacking a [defender] element.
     */
    fun effectivenessAgainst(defender: ElementType): Float {
        return when (this) {
            PYRO -> when (defender) {
                TERRA, CRYO -> 1.5f
                AQUA -> 0.65f
                else -> 1.0f
            }
            AQUA -> when (defender) {
                PYRO -> 1.5f
                TERRA, VOLT -> 0.65f
                else -> 1.0f
            }
            TERRA -> when (defender) {
                AQUA, VOLT -> 1.5f
                PYRO, CRYO -> 0.65f
                else -> 1.0f
            }
            VOLT -> when (defender) {
                AQUA, CRYO -> 1.5f
                TERRA -> 0.65f
                else -> 1.0f
            }
            CRYO -> when (defender) {
                TERRA, AETHER -> 1.5f
                PYRO -> 0.65f
                else -> 1.0f
            }
            AETHER -> when (defender) {
                PYRO, AQUA, VOLT, UMBRA -> 1.25f
                CRYO -> 0.8f
                else -> 1.0f
            }
            UMBRA -> when (defender) {
                LUX, AETHER -> 1.5f
                TERRA -> 0.75f
                else -> 1.0f
            }
            LUX -> when (defender) {
                UMBRA -> 1.5f
                AETHER -> 0.75f
                else -> 1.0f
            }
        }
    }
}

/**
 * Encapsulates the four core base stats of a [Monster].
 */
@IgnoreExtraProperties
data class BaseStats(
    val attack: Int = 50,
    val defense: Int = 45,
    val speed: Int = 48,
    val stamina: Int = 100
)

/**
 * Core data class representing a Monster with ID, name, element type,
 * base stats (attack, defense, speed, stamina), current level, experience points,
 * genetics (Map of traits), and evolution level.
 */
@IgnoreExtraProperties
data class Monster(
    @DocumentId val id: String = "",
    val name: String = "Sylphling",
    val elementType: ElementType = ElementType.TERRA,
    val attack: Int = 52,
    val defense: Int = 48,
    val speed: Int = 56,
    val stamina: Int = 100,
    val currentLevel: Int = 5,
    val experiencePoints: Int = 0,
    val genetics: Map<String, String> = mapOf(
        "ivRank" to "A",
        "Solar-Ader" to "+15% Ausdauer-Regeneration bei Sonne"
    ),
    val evolutionLevel: Int = 16,
    val baseStats: BaseStats = BaseStats(
        attack = attack,
        defense = defense,
        speed = speed,
        stamina = stamina
    )
) {
    /**
     * Checks whether the monster has reached or exceeded the required [evolutionLevel].
     */
    val isReadyToEvolve: Boolean
        get() = evolutionLevel > 0 && currentLevel >= evolutionLevel

    /**
     * Computes scaled attack stat factoring in [currentLevel] and genetic IV rank.
     */
    val effectiveAttack: Int
        get() = ((attack * geneticMultiplier) + (currentLevel * 2.4f)).toInt()

    /**
     * Computes scaled defense stat factoring in [currentLevel] and genetic IV rank.
     */
    val effectiveDefense: Int
        get() = ((defense * geneticMultiplier) + (currentLevel * 2.2f)).toInt()

    /**
     * Computes scaled speed stat factoring in [currentLevel] and genetic IV rank.
     */
    val effectiveSpeed: Int
        get() = ((speed * geneticMultiplier) + (currentLevel * 2.3f)).toInt()

    /**
     * Computes scaled stamina stat factoring in [currentLevel] and genetic IV rank.
     */
    val effectiveStamina: Int
        get() = ((stamina * geneticMultiplier) + (currentLevel * 1.5f)).toInt()

    private val geneticMultiplier: Float
        get() = when (genetics["ivRank"]) {
            "S+" -> 1.25f
            "S" -> 1.15f
            "A" -> 1.08f
            "B" -> 1.0f
            else -> 0.95f
        }

    /**
     * Returns an updated [Monster] after gaining [earnedXp] experience points,
     * automatically leveling up when the XP threshold (`currentLevel * 50`) is reached.
     */
    fun gainExperience(earnedXp: Int): Monster {
        require(earnedXp >= 0) { "Earned experience points must be non-negative" }
        var newLevel = currentLevel
        var remainingXp = experiencePoints + earnedXp
        while (newLevel < 100 && remainingXp >= newLevel * 50) {
            remainingXp -= newLevel * 50
            newLevel += 1
        }
        return copy(
            currentLevel = newLevel,
            experiencePoints = remainingXp
        )
    }

    /**
     * Converts this model to the runtime domain [MonsterInstance].
     */
    fun toDomainInstance(speciesId: Int = 1): MonsterInstance {
        val species = AetheronWorldCatalog.getSpecies(speciesId.coerceIn(1, 240))
        val matchedTraits = genetics.entries
            .filter { it.key != "ivRank" }
            .mapNotNull { entry ->
                AetheronWorldCatalog.geneticTraits.find {
                    it.nameDe.equals(entry.key, ignoreCase = true) || it.id.equals(entry.key, ignoreCase = true)
                }
            }
            .ifEmpty { listOf(species.signatureTrait) }

        return MonsterInstance(
            instanceId = id.ifBlank { "mon_${species.id}_$currentLevel" },
            speciesId = species.id,
            nickname = name.ifBlank { species.nameDe },
            level = currentLevel.coerceIn(1, 100),
            xp = experiencePoints.coerceAtLeast(0),
            currentHp = species.baseHp + (currentLevel * 4),
            currentStamina = stamina.coerceAtLeast(10),
            ivRank = genetics["ivRank"] ?: "A",
            inheritedTraits = matchedTraits
        )
    }

    companion object {
        /**
         * Creates a [Monster] from an existing runtime [MonsterInstance].
         */
        fun fromDomainInstance(instance: MonsterInstance): Monster {
            val species = AetheronWorldCatalog.getSpecies(instance.speciesId)
            val element = runCatching {
                ElementType.valueOf(species.primaryElement.name)
            }.getOrDefault(ElementType.TERRA)

            val traitsMap = buildMap {
                put("ivRank", instance.ivRank)
                instance.inheritedTraits.forEach { trait ->
                    put(trait.nameDe, trait.descriptionDe)
                }
            }

            val nextEvoLevel = species.evolutions.minOfOrNull { it.minLevel } ?: 0

            return Monster(
                id = instance.instanceId,
                name = instance.nickname,
                elementType = element,
                attack = species.baseAtk + instance.trainedAtk,
                defense = species.baseDef + instance.trainedDef,
                speed = species.baseSpd + instance.trainedSpd,
                stamina = instance.currentStamina,
                currentLevel = instance.level,
                experiencePoints = instance.xp,
                genetics = traitsMap,
                evolutionLevel = nextEvoLevel,
                baseStats = BaseStats(
                    attack = species.baseAtk,
                    defense = species.baseDef,
                    speed = species.baseSpd,
                    stamina = instance.currentStamina
                )
            )
        }
    }
}
