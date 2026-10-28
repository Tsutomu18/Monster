package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ElementType(
    val labelDe: String,
    val color: Color,
    val iconCode: String
) {
    TERRA("Terra", ElementTerra, "🌿"),
    PYRO("Pyro", ElementPyro, "🔥"),
    AQUA("Aqua", ElementAqua, "💧"),
    VOLT("Volt", ElementVolt, "⚡"),
    AERO("Aero", ElementAero, "🌪️"),
    CRYO("Cryo", ElementCryo, "❄️"),
    UMBRA("Umbra", ElementUmbra, "🌑"),
    LUX("Lux", ElementLux, "✨"),
    FERRO("Ferro", ElementFerro, "🛡️"),
    TOXIN("Toxin", ElementToxin, "🧪"),
    DRACO("Draco", ElementDraco, "🐉"),
    AETHER("Aether", ElementAether, "💠");

    fun effectivenessAgainst(defender: ElementType): Float {
        return when (this) {
            PYRO -> when (defender) {
                TERRA, CRYO, FERRO -> 1.6f
                AQUA, DRACO -> 0.65f
                else -> 1.0f
            }
            AQUA -> when (defender) {
                PYRO, TERRA -> 1.6f
                VOLT, TOXIN -> 0.65f
                else -> 1.0f
            }
            TERRA -> when (defender) {
                AQUA, VOLT -> 1.6f
                PYRO, AERO, TOXIN -> 0.65f
                else -> 1.0f
            }
            VOLT -> when (defender) {
                AQUA, AERO, FERRO -> 1.6f
                TERRA, DRACO -> 0.65f
                else -> 1.0f
            }
            AERO -> when (defender) {
                TERRA, TOXIN -> 1.6f
                VOLT, CRYO, FERRO -> 0.65f
                else -> 1.0f
            }
            CRYO -> when (defender) {
                AERO, TERRA, DRACO -> 1.6f
                PYRO, FERRO -> 0.65f
                else -> 1.0f
            }
            UMBRA -> when (defender) {
                LUX, AETHER -> 1.5f
                FERRO -> 0.7f
                else -> 1.0f
            }
            LUX -> when (defender) {
                UMBRA, TOXIN -> 1.6f
                AETHER -> 0.75f
                else -> 1.0f
            }
            FERRO -> when (defender) {
                CRYO, LUX, DRACO -> 1.5f
                PYRO, VOLT -> 0.65f
                else -> 1.0f
            }
            TOXIN -> when (defender) {
                TERRA, AQUA -> 1.5f
                FERRO, UMBRA -> 0.65f
                else -> 1.0f
            }
            DRACO -> when (defender) {
                DRACO, PYRO, VOLT -> 1.5f
                CRYO, AETHER -> 0.7f
                else -> 1.0f
            }
            AETHER -> when (defender) {
                DRACO, UMBRA, LUX, AETHER -> 1.45f
                else -> 1.1f
            }
        }
    }
}

enum class WeatherType(
    val labelDe: String,
    val descriptionDe: String,
    val boostedElement: ElementType,
    val weakenedElement: ElementType,
    val spawnBonusElement: ElementType,
    val rarityMultiplier: Float
) {
    SOLAR_FLARE(
        "Sonnenflut",
        "Verstärkt Pyro- & Lux-Attacken um 25%. Erhöht Pyro-Monster-Spawns.",
        ElementType.PYRO,
        ElementType.CRYO,
        ElementType.PYRO,
        1.15f
    ),
    TROPICAL_RAIN(
        "Tropenregen",
        "Verstärkt Aqua- & Terra-Fähigkeiten um 25%. Lockt Wasser- & Urwald-Kreaturen an.",
        ElementType.AQUA,
        ElementType.PYRO,
        ElementType.AQUA,
        1.2f
    ),
    THUNDER_TEMPEST(
        "Gewittersturm",
        "Volt- & Aero-Angriffe +30%. Seltene Sturm-Kreaturen erscheinen häufiger.",
        ElementType.VOLT,
        ElementType.TERRA,
        ElementType.VOLT,
        1.35f
    ),
    OBSIDIAN_SANDSTORM(
        "Obsidian-Sturm",
        "Schützt Ferro- & Draco-Typen (+20% Verteidigung). Seltene Wüsten-Monster aktiv.",
        ElementType.FERRO,
        ElementType.AERO,
        ElementType.DRACO,
        1.3f
    ),
    AETHER_MIST(
        "Aether-Nebel",
        "Mystischer Kosmos-Nebel: +25% Aether/Umbra-Kraft & doppelte Chance auf Legendäre Spawns!",
        ElementType.AETHER,
        ElementType.TOXIN,
        ElementType.AETHER,
        1.75f
    )
}

enum class TimeOfDay(
    val labelDe: String,
    val hoursRange: String,
    val npcRoutineDe: String,
    val favoredElements: List<ElementType>
) {
    DAWN(
        "Morgengrauen",
        "05:00 - 09:00",
        "NPCs öffnen Werkstätten & sammeln Morgentau-Kräuter",
        listOf(ElementType.AERO, ElementType.TERRA, ElementType.LUX)
    ),
    DAY(
        "Tag",
        "09:00 - 17:00",
        "Händler, Arena-Leiter & Fraktions-Wachen auf den Hauptplätzen aktiv",
        listOf(ElementType.PYRO, ElementType.TERRA, ElementType.FERRO)
    ),
    DUSK(
        "Abenddämmerung",
        "17:00 - 21:00",
        "Züchter & Forscher versammeln sich an den Aether-Quellen",
        listOf(ElementType.DRACO, ElementType.AQUA, ElementType.VOLT)
    ),
    NIGHT(
        "Nacht",
        "21:00 - 05:00",
        "Schwarzmarkt-Chronisten & Nachtwächter patrouillieren; Schatten-Spawns aktiv",
        listOf(ElementType.UMBRA, ElementType.AETHER, ElementType.CRYO, ElementType.TOXIN)
    )
}

enum class StatType(val labelDe: String, val shortLabel: String) {
    ATK("Angriff", "ATK"),
    DEF("Verteidigung", "DEF"),
    SPD("Geschwindigkeit", "SPD"),
    ELEM("Elementarkraft", "ELM"),
    HP("Vitalität", "HP")
}

data class GeneticTrait(
    val id: String,
    val nameDe: String,
    val descriptionDe: String,
    val atkMult: Float = 1.0f,
    val defMult: Float = 1.0f,
    val spdMult: Float = 1.0f,
    val critBonus: Float = 0.0f,
    val lifestealRatio: Float = 0.0f
)

data class CombatSkill(
    val id: String,
    val nameDe: String,
    val element: ElementType,
    val power: Int,
    val staminaCost: Int,
    val descriptionDe: String,
    val buffsSelfStat: StatType? = null
)

data class EvolutionBranch(
    val targetSpeciesId: Int,
    val targetSpeciesName: String,
    val minLevel: Int,
    val requiredDominantStat: StatType? = null,
    val minTrainedStatValue: Int = 0,
    val requiredArtifactId: String? = null,
    val conditionDescriptionDe: String
)

data class MonsterSpecies(
    val id: Int,
    val nameDe: String,
    val continentId: Int,
    val biomeId: String,
    val primaryElement: ElementType,
    val secondaryElement: ElementType? = null,
    val baseHp: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val baseSpd: Int,
    val baseElem: Int,
    val stage: Int, // 1, 2, 3
    val isLegendary: Boolean = false,
    val preferredWeather: WeatherType? = null,
    val preferredTime: TimeOfDay? = null,
    val signatureTrait: GeneticTrait,
    val skills: List<CombatSkill>,
    val evolutions: List<EvolutionBranch> = emptyList(),
    val loreDe: String
)

data class MonsterInstance(
    val instanceId: String,
    val speciesId: Int,
    val nickname: String,
    val level: Int = 5,
    val xp: Int = 0,
    val currentHp: Int = 60,
    val currentStamina: Int = 100,
    val ivRank: String = "B", // C, B, A, S, S+
    val inheritedTraits: List<GeneticTrait> = emptyList(),
    val trainedHp: Int = 0,
    val trainedAtk: Int = 0,
    val trainedDef: Int = 0,
    val trainedSpd: Int = 0,
    val trainedElem: Int = 0,
    val trainingSessionsDone: Int = 0,
    val generation: Int = 1 // Increases via breeding (including Legendaries!)
) {
    fun ivMultiplier(): Float = when (ivRank) {
        "S+" -> 1.32f
        "S" -> 1.22f
        "A" -> 1.14f
        "B" -> 1.06f
        else -> 1.0f
    }

    fun generationMultiplier(): Float = 1.0f + ((generation - 1).coerceAtLeast(0) * 0.08f)

    fun maxHp(species: MonsterSpecies): Int {
        val base = ((species.baseHp * 2.2f + level * 4.5f) * ivMultiplier() * generationMultiplier()).toInt()
        return base + (trainedHp / 2)
    }

    fun effectiveAtk(species: MonsterSpecies): Int {
        val traitMult = inheritedTraits.fold(1.0f) { acc, t -> acc * t.atkMult }
        val raw = ((species.baseAtk + level * 2.6f) * ivMultiplier() * generationMultiplier()) + (trainedAtk * 0.75f)
        return (raw * traitMult).toInt()
    }

    fun effectiveDef(species: MonsterSpecies): Int {
        val traitMult = inheritedTraits.fold(1.0f) { acc, t -> acc * t.defMult }
        val raw = ((species.baseDef + level * 2.4f) * ivMultiplier() * generationMultiplier()) + (trainedDef * 0.75f)
        return (raw * traitMult).toInt()
    }

    fun effectiveSpd(species: MonsterSpecies): Int {
        val traitMult = inheritedTraits.fold(1.0f) { acc, t -> acc * t.spdMult }
        val raw = ((species.baseSpd + level * 2.3f) * ivMultiplier() * generationMultiplier()) + (trainedSpd * 0.75f)
        return (raw * traitMult).toInt()
    }

    fun effectiveElem(species: MonsterSpecies): Int {
        val raw = ((species.baseElem + level * 2.6f) * ivMultiplier() * generationMultiplier()) + (trainedElem * 0.75f)
        return raw.toInt()
    }

    fun totalTrainedPoints(): Int = trainedHp + trainedAtk + trainedDef + trainedSpd + trainedElem

    fun dominantTrainedStat(): StatType {
        val map = mapOf(
            StatType.ATK to trainedAtk,
            StatType.DEF to trainedDef,
            StatType.SPD to trainedSpd,
            StatType.ELEM to trainedElem,
            StatType.HP to trainedHp
        )
        return map.maxByOrNull { it.value }?.key ?: StatType.ATK
    }

    fun combatPower(species: MonsterSpecies): Int {
        return maxHp(species) + effectiveAtk(species) + effectiveDef(species) + effectiveSpd(species) + effectiveElem(species)
    }
}

data class BiomeZone(
    val id: String,
    val nameDe: String,
    val continentId: Int,
    val descriptionDe: String,
    val primaryElement: ElementType,
    val resourceDrops: List<String>,
    val recommendedLevel: Int,
    val mapColorHex: Long
)

data class GymLeaderBadge(
    val badgeIndex: Int, // 1..24
    val badgeNameDe: String,
    val leaderNameDe: String,
    val continentId: Int,
    val element: ElementType,
    val recommendedLevel: Int,
    val aceSpeciesId: Int,
    val mottoDe: String
)

data class WorldNpc(
    val id: String,
    val nameDe: String,
    val roleDe: String,
    val continentId: Int,
    val factionId: String,
    val dawnLocation: Pair<Int, Int>,
    val dayLocation: Pair<Int, Int>,
    val duskLocation: Pair<Int, Int>,
    val nightLocation: Pair<Int, Int>,
    val dialogueByTime: Map<TimeOfDay, String>
)

data class FactionInfo(
    val id: String,
    val nameDe: String,
    val homeContinentId: Int,
    val ideologyDe: String,
    val perkDescriptionDe: String
)

enum class QuestCategory(val labelDe: String, val icon: String) {
    MONSTER_HUNT("Monsterjagd", "⚔️"),
    RESOURCE_GATHER("Ressourcen-Sammlung", "🌿"),
    BIOME_EXPLORE("Biom-Erkundung", "🧭"),
    FACTION_BOUNTY("Fraktions-Spezialauftrag", "🏛️")
}

data class DynamicQuest(
    val id: String,
    val titleDe: String,
    val descriptionDe: String,
    val category: QuestCategory,
    val continentId: Int,
    val biomeId: String,
    val biomeNameDe: String,
    val factionId: String,
    val factionNameDe: String,
    val targetSpeciesId: Int? = null,
    val targetItemId: String? = null,
    val difficultyStars: Int, // 1..5
    val requiredCount: Int,
    val currentProgress: Int = 0,
    val rewardGold: Int,
    val rewardXp: Int,
    val rewardFactionRep: Int,
    val rewardItemId: String,
    val rewardItemCount: Int,
    val isCompleted: Boolean = false
)

enum class ItemCategory(val labelDe: String) {
    CAPTURE_ORB("Fang-Sphäre"),
    POTION("Heilung & Vitalität"),
    CRAFTING_MAT("Biom-Ressource"),
    EVOLUTION_ARTIFACT("Evolutions-Artefakt"),
    GEAR_BOOST("Trainings-Ausrüstung")
}

data class GameItem(
    val id: String,
    val nameDe: String,
    val category: ItemCategory,
    val descriptionDe: String,
    val rarityStars: Int,
    val effectValue: Int = 0
)

data class CraftingRecipe(
    val id: String,
    val resultItemId: String,
    val resultCount: Int = 1,
    val ingredients: Map<String, Int>, // itemId -> count
    val goldCost: Int,
    val requiredSkillLevel: Int = 0
)

data class PlayerSkillNode(
    val id: String,
    val branchDe: String,
    val nameDe: String,
    val descriptionDe: String,
    val maxRank: Int = 5,
    val currentRank: Int = 0
)
