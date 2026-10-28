package com.example.domain.engine

import com.example.domain.catalog.AetheronWorldCatalog
import com.example.domain.model.*
import java.util.UUID
import kotlin.math.roundToInt

enum class TrainingExerciseType(
    val id: String,
    val titleDe: String,
    val subtitleDe: String,
    val primaryStat: StatType,
    val secondaryStat: StatType,
    val instructionsDe: String
) {
    POWER_FORGE(
        "power_forge",
        "Kraft-Schmiede (Timing-Schlag)",
        "Trainiert Angriff (ATK) & Elementarkraft (ELM)",
        StatType.ATK,
        StatType.ELEM,
        "Tippe im richtigen Moment auf 'SCHLAG!', wenn der Aether-Pendel die goldene Fokus-Zone (75% - 95%) durchläuft!"
    ),
    AEGIS_REFLEX(
        "aegis_reflex",
        "Aegis-Reflex (Schild-Parade)",
        "Trainiert Verteidigung (DEF) & Vitalität (HP)",
        StatType.DEF,
        StatType.HP,
        "Blocke die anfliegenden Elementar-Salven, indem du blitzschnell den passenden Schild-Sektor (Nord, Ost, Süd, West) antippst!"
    ),
    AETHER_SPRINT(
        "aether_sprint",
        "Aether-Sprint (Runen-Sequenz)",
        "Trainiert Geschwindigkeit (SPD) & Initiative",
        StatType.SPD,
        StatType.ATK,
        "Aktiviere die aufleuchtenden Geschwindigkeits-Runen in aufsteigender Reihenfolge (1 bis 6), um maximale S-Rang-Kombos zu erzielen!"
    )
}

data class TrainingSessionResult(
    val exercise: TrainingExerciseType,
    val scorePercent: Int, // 0..100
    val gradeRank: String, // S, A, B, C
    val primaryStatGained: Int,
    val secondaryStatGained: Int,
    val xpGained: Int,
    val bonusItemRewardId: String?,
    val bonusItemNameDe: String?,
    val updatedMonster: MonsterInstance,
    val unlockedEvolutionPreview: String?
)

object DynamicQuestAndTrainingEngine {

    /**
     * Berechnet das Ergebnis einer interaktiven Trainings-Übung, wendet Stat-Steigerungen an,
     * berücksichtigt Ausrüstungs-/Skill-Boni und prüft, wie sich das Training auf die Evolution auswirkt.
     */
    fun evaluateTrainingMiniGame(
        monster: MonsterInstance,
        exercise: TrainingExerciseType,
        scorePercent: Int,
        dojoSkillRank: Int,
        hasPowerBracer: Boolean,
        factionBonusMultiplier: Float
    ): TrainingSessionResult {
        val clampedScore = scorePercent.coerceIn(10, 100)
        val grade = when {
            clampedScore >= 90 -> "S"
            clampedScore >= 75 -> "A"
            clampedScore >= 55 -> "B"
            else -> "C"
        }

        val basePrimary = when (grade) {
            "S" -> 12
            "A" -> 9
            "B" -> 6
            else -> 4
        }
        val baseSecondary = when (grade) {
            "S" -> 7
            "A" -> 5
            "B" -> 3
            else -> 2
        }

        val skillMult = 1.0f + (dojoSkillRank * 0.15f)
        val gearMult = if (hasPowerBracer) 1.5f else 1.0f
        val totalMult = skillMult * gearMult * factionBonusMultiplier

        val primaryGain = (basePrimary * totalMult).roundToInt().coerceAtLeast(2)
        val secondaryGain = (baseSecondary * totalMult).roundToInt().coerceAtLeast(1)
        val xpGain = (35 + clampedScore * 0.8f).roundToInt()

        var newHp = monster.trainedHp
        var newAtk = monster.trainedAtk
        var newDef = monster.trainedDef
        var newSpd = monster.trainedSpd
        var newElem = monster.trainedElem

        fun addStat(stat: StatType, amount: Int) {
            when (stat) {
                StatType.ATK -> newAtk = (newAtk + amount).coerceAtMost(250)
                StatType.DEF -> newDef = (newDef + amount).coerceAtMost(250)
                StatType.SPD -> newSpd = (newSpd + amount).coerceAtMost(250)
                StatType.ELEM -> newElem = (newElem + amount).coerceAtMost(250)
                StatType.HP -> newHp = (newHp + amount).coerceAtMost(250)
            }
        }

        addStat(exercise.primaryStat, primaryGain)
        addStat(exercise.secondaryStat, secondaryGain)

        val species = AetheronWorldCatalog.getSpecies(monster.speciesId)
        val totalXp = monster.xp + xpGain
        val xpThreshold = monster.level * 60
        val leveledUp = totalXp >= xpThreshold && monster.level < 100
        val newLevel = if (leveledUp) monster.level + 1 else monster.level
        val remainingXp = if (leveledUp) totalXp - xpThreshold else totalXp

        val updated = monster.copy(
            level = newLevel,
            xp = remainingXp,
            trainedHp = newHp,
            trainedAtk = newAtk,
            trainedDef = newDef,
            trainedSpd = newSpd,
            trainedElem = newElem,
            trainingSessionsDone = monster.trainingSessionsDone + 1,
            currentHp = monster.maxHp(species) + (newHp / 2)
        )

        // Bonus-Belohnung bei S- oder A-Rang
        val rewardItem: GameItem? = when (grade) {
            "S" -> AetheronWorldCatalog.items.find { it.id == "orb_aether_master" }
            "A" -> AetheronWorldCatalog.items.find { it.id == "pot_vital_elixir" }
            else -> null
        }

        // Prüfe Einfluss auf die individuelle Weiterentwicklung
        val evoPreview = species.evolutions.firstOrNull()?.let { branch ->
            val currentDom = updated.dominantTrainedStat()
            val statVal = when (branch.requiredDominantStat) {
                StatType.ATK -> updated.trainedAtk
                StatType.DEF -> updated.trainedDef
                StatType.SPD -> updated.trainedSpd
                StatType.ELEM -> updated.trainedElem
                StatType.HP -> updated.trainedHp
                null -> updated.totalTrainedPoints()
            }
            if (branch.requiredDominantStat != null) {
                "Evolutions-Pfad zu ${branch.targetSpeciesName}: ${branch.requiredDominantStat.shortLabel} $statVal/${branch.minTrainedStatValue} (Dominant: ${currentDom.shortLabel}, Level ${updated.level}/${branch.minLevel})"
            } else {
                "Bereit für ${branch.targetSpeciesName} ab Level ${branch.minLevel}"
            }
        }

        return TrainingSessionResult(
            exercise = exercise,
            scorePercent = clampedScore,
            gradeRank = grade,
            primaryStatGained = primaryGain,
            secondaryStatGained = secondaryGain,
            xpGained = xpGain,
            bonusItemRewardId = rewardItem?.id,
            bonusItemNameDe = rewardItem?.nameDe,
            updatedMonster = updated,
            unlockedEvolutionPreview = evoPreview
        )
    }

    /**
     * Dynamischer Quest-Generator:
     * Erzeugt vielfältige Quests basierend auf aktuellem Standort (Kontinent & Biom),
     * Spielerstufe, bereits abgeschlossenen Quests, aktiven Fraktionen, Wetter und lokalen Monstern.
     */
    fun generateDynamicQuests(
        continentId: Int,
        biome: BiomeZone,
        playerLevel: Int,
        completedQuestsCount: Int,
        factionStandings: Map<String, Int>,
        currentWeather: WeatherType,
        currentTime: TimeOfDay
    ): List<DynamicQuest> {
        val localSpecies = AetheronWorldCatalog.allSpecies.filter {
            it.continentId == continentId && it.biomeId == biome.id
        }.ifEmpty {
            AetheronWorldCatalog.allSpecies.filter { it.continentId == continentId }
        }

        val activeFaction = AetheronWorldCatalog.factions.find { it.homeContinentId == continentId }
            ?: AetheronWorldCatalog.factions.first()
        val factionRep = factionStandings[activeFaction.id] ?: 100

        // Schwierigkeitsgrad skaliert mit Spieler-Level, Biom-Level, abgeschlossenen Quests & Ruf
        val baseDifficulty = ((playerLevel / 18) + (completedQuestsCount / 6) + (continentId)).coerceIn(1, 5)

        val huntTarget = localSpecies.firstOrNull { !it.isLegendary } ?: localSpecies.first()
        val bossTarget = localSpecies.lastOrNull() ?: huntTarget
        val targetResourceId = biome.resourceDrops.firstOrNull() ?: "mat_aether_shard"
        val targetResourceName = AetheronWorldCatalog.items.find { it.id == targetResourceId }?.nameDe ?: "Biom-Essenz"

        // 1. Monsterjagd-Quest (bezieht lokales Monster & Wetter ein)
        val huntDiff = baseDifficulty.coerceIn(1, 5)
        val huntCount = (2 + (huntDiff / 2)).coerceIn(2, 5)
        val huntRewards = calculateDifficultyScaledRewards(huntDiff, QuestCategory.MONSTER_HUNT, continentId, factionRep)

        val questHunt = DynamicQuest(
            id = "q_hunt_${UUID.randomUUID().toString().take(8)}",
            titleDe = "${huntTarget.nameDe}-Eindämmung in ${biome.nameDe}",
            descriptionDe = "Durch ${currentWeather.labelDe} sind wilde ${huntTarget.nameDe} (${huntTarget.primaryElement.labelDe}) besonders aktiv. Besiege oder fange $huntCount Exemplare für ${activeFaction.nameDe}.",
            category = QuestCategory.MONSTER_HUNT,
            continentId = continentId,
            biomeId = biome.id,
            biomeNameDe = biome.nameDe,
            factionId = activeFaction.id,
            factionNameDe = activeFaction.nameDe,
            targetSpeciesId = huntTarget.id,
            difficultyStars = huntDiff,
            requiredCount = huntCount,
            rewardGold = huntRewards.gold,
            rewardXp = huntRewards.xp,
            rewardFactionRep = huntRewards.rep,
            rewardItemId = huntRewards.itemId,
            rewardItemCount = huntRewards.itemCount
        )

        // 2. Ressourcen-Sammel-Quest (Biom-Materialien)
        val gatherDiff = (baseDifficulty - 1).coerceIn(1, 5)
        val gatherCount = 2 + gatherDiff
        val gatherRewards = calculateDifficultyScaledRewards(gatherDiff, QuestCategory.RESOURCE_GATHER, continentId, factionRep)

        val questGather = DynamicQuest(
            id = "q_gather_${UUID.randomUUID().toString().take(8)}",
            titleDe = "Seltene $targetResourceName bergen",
            descriptionDe = "Sammle $gatherCount Einheiten '$targetResourceName' bei der Erkundung oder aus Kämpfen in ${biome.nameDe} (${currentTime.labelDe}).",
            category = QuestCategory.RESOURCE_GATHER,
            continentId = continentId,
            biomeId = biome.id,
            biomeNameDe = biome.nameDe,
            factionId = activeFaction.id,
            factionNameDe = activeFaction.nameDe,
            targetItemId = targetResourceId,
            difficultyStars = gatherDiff,
            requiredCount = gatherCount,
            rewardGold = gatherRewards.gold,
            rewardXp = gatherRewards.xp,
            rewardFactionRep = gatherRewards.rep,
            rewardItemId = gatherRewards.itemId,
            rewardItemCount = gatherRewards.itemCount
        )

        // 3. Biom-Erkundungs-Mission
        val exploreDiff = baseDifficulty.coerceIn(1, 5)
        val exploreSteps = 6 + exploreDiff * 2
        val exploreRewards = calculateDifficultyScaledRewards(exploreDiff, QuestCategory.BIOME_EXPLORE, continentId, factionRep)

        val questExplore = DynamicQuest(
            id = "q_explore_${UUID.randomUUID().toString().take(8)}",
            titleDe = "Kartografierung: ${biome.nameDe}",
            descriptionDe = "Erkunde $exploreSteps Geländefelder in ${biome.nameDe} und dokumentiere die Wetter-Anomalien (${currentWeather.labelDe}) für die ${activeFaction.nameDe}.",
            category = QuestCategory.BIOME_EXPLORE,
            continentId = continentId,
            biomeId = biome.id,
            biomeNameDe = biome.nameDe,
            factionId = activeFaction.id,
            factionNameDe = activeFaction.nameDe,
            difficultyStars = exploreDiff,
            requiredCount = exploreSteps,
            rewardGold = exploreRewards.gold,
            rewardXp = exploreRewards.xp,
            rewardFactionRep = exploreRewards.rep,
            rewardItemId = exploreRewards.itemId,
            rewardItemCount = exploreRewards.itemCount
        )

        // 4. Fraktions-Spezialauftrag (Hochrisiko-Bounty mit Artefakt-/Meister-Sphären-Belohnung)
        val bountyDiff = (baseDifficulty + 1).coerceIn(2, 5)
        val bountyRewards = calculateDifficultyScaledRewards(bountyDiff, QuestCategory.FACTION_BOUNTY, continentId, factionRep)

        val questBounty = DynamicQuest(
            id = "q_bounty_${UUID.randomUUID().toString().take(8)}",
            titleDe = "Fraktions-Dekret: Alpha-${bossTarget.nameDe}",
            descriptionDe = "Politischer Prestige-Auftrag (#${completedQuestsCount + 1}): Beweise deine Stärke im Biom ${biome.nameDe} durch 2 Siege oder Fänge unter ${currentWeather.labelDe}-Bedingungen.",
            category = QuestCategory.FACTION_BOUNTY,
            continentId = continentId,
            biomeId = biome.id,
            biomeNameDe = biome.nameDe,
            factionId = activeFaction.id,
            factionNameDe = activeFaction.nameDe,
            targetSpeciesId = bossTarget.id,
            difficultyStars = bountyDiff,
            requiredCount = 2,
            rewardGold = bountyRewards.gold,
            rewardXp = bountyRewards.xp,
            rewardFactionRep = bountyRewards.rep,
            rewardItemId = bountyRewards.itemId,
            rewardItemCount = bountyRewards.itemCount
        )

        return listOf(questHunt, questGather, questExplore, questBounty)
    }

    private data class ScaledRewardBundle(
        val gold: Int,
        val xp: Int,
        val rep: Int,
        val itemId: String,
        val itemCount: Int
    )

    private fun calculateDifficultyScaledRewards(
        difficultyStars: Int,
        category: QuestCategory,
        continentId: Int,
        factionRep: Int
    ): ScaledRewardBundle {
        val repBonusMult = 1.0f + (factionRep / 1000f).coerceAtMost(0.5f)
        val categoryGoldMult = when (category) {
            QuestCategory.MONSTER_HUNT -> 1.25f
            QuestCategory.RESOURCE_GATHER -> 1.0f
            QuestCategory.BIOME_EXPLORE -> 1.1f
            QuestCategory.FACTION_BOUNTY -> 1.65f
        }

        val baseGold = (120 * difficultyStars * continentId * categoryGoldMult * repBonusMult).roundToInt()
        val baseXp = (85 * difficultyStars * continentId * repBonusMult).roundToInt()
        val baseRep = 25 * difficultyStars + (if (category == QuestCategory.FACTION_BOUNTY) 35 else 10)

        val (rewardItem, count) = when {
            category == QuestCategory.FACTION_BOUNTY && difficultyStars >= 4 -> {
                val artId = when (continentId) {
                    1 -> "art_solar_core"
                    2 -> "art_abyss_relic"
                    else -> "art_chrono_prism"
                }
                artId to 1
            }
            category == QuestCategory.MONSTER_HUNT && difficultyStars >= 3 -> "orb_aether_master" to 2
            category == QuestCategory.RESOURCE_GATHER -> "pot_phoenix_nectar" to 1
            category == QuestCategory.BIOME_EXPLORE -> "orb_aether_basic" to (1 + difficultyStars)
            else -> "pot_vital_elixir" to 2
        }

        return ScaledRewardBundle(
            gold = baseGold,
            xp = baseXp,
            rep = baseRep,
            itemId = rewardItem,
            itemCount = count
        )
    }
}
