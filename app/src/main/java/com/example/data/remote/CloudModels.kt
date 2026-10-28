package com.example.data.remote

import com.example.domain.catalog.AetheronWorldCatalog
import com.example.domain.model.MonsterInstance
import com.example.domain.model.StatType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore 'Monster' entity data class including fields for combat stats (attack, defense, speed),
 * genetics (IV rank, trait, generation), current level, and evolution progress.
 */
@IgnoreExtraProperties
data class Monster(
    @DocumentId val id: String = "",
    val userId: String = "",
    val speciesId: Long = 1L,
    val name: String = "Sylphling",
    val primaryElement: String = "TERRA",
    val currentLevel: Long = 5L,
    val xp: Long = 0L,
    // Stats (Attack, Defense, Speed, Elemental Power, Max HP + Trained Stat Points)
    val attack: Long = 45L,
    val defense: Long = 40L,
    val speed: Long = 44L,
    val elementalPower: Long = 45L,
    val maxHp: Long = 100L,
    val trainedAttack: Long = 0L,
    val trainedDefense: Long = 0L,
    val trainedSpeed: Long = 0L,
    // Genetics
    val geneticIvRank: String = "A",
    val geneticTraitId: String = "trait_solar_vein",
    val geneticTraitName: String = "Solar-Ader",
    val generation: Long = 1L,
    // Evolution Progress
    val evolutionStage: Long = 1L,
    val evolutionProgressPercent: Long = 0L,
    val targetEvolutionSpeciesId: Long = 2L,
    val requiredArtifactId: String = "",
    // Server Timestamps
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMonsterInstance(): MonsterInstance {
        val sp = AetheronWorldCatalog.getSpecies(speciesId.toInt().coerceIn(1, 240))
        val matchedTrait = AetheronWorldCatalog.geneticTraits.find { it.id == geneticTraitId }
            ?: sp.signatureTrait
        return MonsterInstance(
            instanceId = id.ifBlank { "mon_${speciesId}_${currentLevel}" },
            speciesId = sp.id,
            nickname = name.ifBlank { sp.nameDe },
            level = currentLevel.toInt().coerceIn(1, 100),
            xp = xp.toInt().coerceAtLeast(0),
            currentHp = maxHp.toInt().coerceAtLeast(1),
            currentStamina = 100,
            ivRank = if (geneticIvRank in listOf("C", "B", "A", "S", "S+")) geneticIvRank else "A",
            inheritedTraits = listOf(matchedTrait),
            trainedAtk = trainedAttack.toInt().coerceIn(0, 250),
            trainedDef = trainedDefense.toInt().coerceIn(0, 250),
            trainedSpd = trainedSpeed.toInt().coerceIn(0, 250),
            generation = generation.toInt().coerceIn(1, 100)
        )
    }

    companion object {
        fun fromMonsterInstance(userId: String, instance: MonsterInstance): Monster {
            val sp = AetheronWorldCatalog.getSpecies(instance.speciesId)
            val primaryBranch = sp.evolutions.firstOrNull()
            val evoProgress = if (primaryBranch == null) {
                100L
            } else {
                val levelRatio = (instance.level.toFloat() / primaryBranch.minLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
                val statRatio = if (primaryBranch.minTrainedStatValue > 0) {
                    val relevantStat = when (primaryBranch.requiredDominantStat) {
                        StatType.ATK -> instance.trainedAtk
                        StatType.DEF -> instance.trainedDef
                        StatType.SPD -> instance.trainedSpd
                        StatType.ELEM -> instance.trainedElem
                        StatType.HP -> instance.trainedHp
                        null -> instance.totalTrainedPoints()
                    }
                    (relevantStat.toFloat() / primaryBranch.minTrainedStatValue).coerceIn(0f, 1f)
                } else 1f
                (((levelRatio * 0.6f) + (statRatio * 0.4f)) * 100f).toLong().coerceIn(0L, 100L)
            }
            val trait = instance.inheritedTraits.firstOrNull() ?: sp.signatureTrait

            return Monster(
                id = instance.instanceId,
                userId = userId,
                speciesId = sp.id.toLong(),
                name = instance.nickname,
                primaryElement = sp.primaryElement.name,
                currentLevel = instance.level.toLong(),
                xp = instance.xp.toLong(),
                attack = instance.effectiveAtk(sp).toLong(),
                defense = instance.effectiveDef(sp).toLong(),
                speed = instance.effectiveSpd(sp).toLong(),
                elementalPower = instance.effectiveElem(sp).toLong(),
                maxHp = instance.maxHp(sp).toLong(),
                trainedAttack = instance.trainedAtk.toLong(),
                trainedDefense = instance.trainedDef.toLong(),
                trainedSpeed = instance.trainedSpd.toLong(),
                geneticIvRank = instance.ivRank,
                geneticTraitId = trait.id,
                geneticTraitName = trait.nameDe,
                generation = instance.generation.toLong(),
                evolutionStage = sp.stage.toLong(),
                evolutionProgressPercent = evoProgress,
                targetEvolutionSpeciesId = (primaryBranch?.targetSpeciesId ?: 0).toLong(),
                requiredArtifactId = primaryBranch?.requiredArtifactId ?: ""
            )
        }
    }
}

@IgnoreExtraProperties
data class PlayerCloudProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val trainerName: String = "Aether-Sucher",
    val level: Long = 1L,
    val gold: Long = 1000L,
    val continentId: Long = 1L,
    val badgesCount: Long = 0L,
    val capturedCount: Long = 1L,
    val questsCompleted: Long = 0L,
    val factionSylvariaRep: Long = 100L,
    val factionPyroclastRep: Long = 100L,
    val factionAetherisRep: Long = 100L,
    val saveStateJson: String = "{}",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TradeOffer(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "Trainer",
    val offeredSpeciesId: Long = 1L,
    val offeredMonsterName: String = "Sylphox",
    val offeredLevel: Long = 10L,
    val offeredIvRank: String = "A",
    val offeredTrait: String = "Aether-Puls",
    val offeredTrainedStats: String = "ATK+20 / SPD+15",
    val requestedSpeciesId: Long = 81L,
    val requestedSpeciesName: String = "Pyroclaw",
    val status: String = "open",
    val completedByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ArenaRoster(
    @DocumentId val id: String = "",
    val userId: String = "",
    val trainerName: String = "Trainer",
    val combatPower: Long = 1200L,
    val badgesCount: Long = 0L,
    val wins: Long = 0L,
    val leadSpeciesId: Long = 1L,
    val teamSummary: String = "Sylphox Lv.15",
    val status: String = "active",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
