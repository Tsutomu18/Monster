package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.remote.Monster
import com.example.data.remote.PlayerCloudProfile

/**
 * Room Entity storing a [Monster] locally on-device for offline play and Firestore synchronization.
 */
@Entity(tableName = "local_monsters")
data class LocalMonsterEntity(
    @PrimaryKey val id: String,
    val userId: String = "local_trainer",
    val speciesId: Long = 1L,
    val name: String = "Sylphling",
    val primaryElement: String = "TERRA",
    val currentLevel: Long = 5L,
    val xp: Long = 0L,
    val attack: Long = 45L,
    val defense: Long = 40L,
    val speed: Long = 44L,
    val elementalPower: Long = 45L,
    val maxHp: Long = 100L,
    val trainedAttack: Long = 0L,
    val trainedDefense: Long = 0L,
    val trainedSpeed: Long = 0L,
    val geneticIvRank: String = "A",
    val geneticTraitId: String = "trait_solar_vein",
    val geneticTraitName: String = "Solar-Ader",
    val generation: Long = 1L,
    val evolutionStage: Long = 1L,
    val evolutionProgressPercent: Long = 0L,
    val targetEvolutionSpeciesId: Long = 2L,
    val requiredArtifactId: String = "",
    val pendingSync: Boolean = true,
    val isDeletedLocally: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toCloudMonster(): Monster = Monster(
        id = id,
        userId = userId,
        speciesId = speciesId,
        name = name,
        primaryElement = primaryElement,
        currentLevel = currentLevel,
        xp = xp,
        attack = attack,
        defense = defense,
        speed = speed,
        elementalPower = elementalPower,
        maxHp = maxHp,
        trainedAttack = trainedAttack,
        trainedDefense = trainedDefense,
        trainedSpeed = trainedSpeed,
        geneticIvRank = geneticIvRank,
        geneticTraitId = geneticTraitId,
        geneticTraitName = geneticTraitName,
        generation = generation,
        evolutionStage = evolutionStage,
        evolutionProgressPercent = evolutionProgressPercent,
        targetEvolutionSpeciesId = targetEvolutionSpeciesId,
        requiredArtifactId = requiredArtifactId
    )

    companion object {
        fun fromCloudMonster(
            monster: Monster,
            pendingSync: Boolean = false,
            isDeletedLocally: Boolean = false
        ): LocalMonsterEntity {
            return LocalMonsterEntity(
                id = monster.id.ifBlank { "mon_${monster.speciesId}_${monster.currentLevel}" },
                userId = monster.userId.ifBlank { "local_trainer" },
                speciesId = monster.speciesId,
                name = monster.name,
                primaryElement = monster.primaryElement,
                currentLevel = monster.currentLevel,
                xp = monster.xp,
                attack = monster.attack,
                defense = monster.defense,
                speed = monster.speed,
                elementalPower = monster.elementalPower,
                maxHp = monster.maxHp,
                trainedAttack = monster.trainedAttack,
                trainedDefense = monster.trainedDefense,
                trainedSpeed = monster.trainedSpeed,
                geneticIvRank = monster.geneticIvRank,
                geneticTraitId = monster.geneticTraitId,
                geneticTraitName = monster.geneticTraitName,
                generation = monster.generation,
                evolutionStage = monster.evolutionStage,
                evolutionProgressPercent = monster.evolutionProgressPercent,
                targetEvolutionSpeciesId = monster.targetEvolutionSpeciesId,
                requiredArtifactId = monster.requiredArtifactId,
                pendingSync = pendingSync,
                isDeletedLocally = isDeletedLocally,
                updatedAtMillis = monster.updatedAt?.toDate()?.time ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Room Entity storing the player's profile and serialized RPG state locally on-device.
 */
@Entity(tableName = "local_player_profiles")
data class LocalPlayerProfileEntity(
    @PrimaryKey val userId: String,
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
    val pendingSync: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toCloudProfile(): PlayerCloudProfile = PlayerCloudProfile(
        id = userId,
        userId = userId,
        trainerName = trainerName,
        level = level,
        gold = gold,
        continentId = continentId,
        badgesCount = badgesCount,
        capturedCount = capturedCount,
        questsCompleted = questsCompleted,
        factionSylvariaRep = factionSylvariaRep,
        factionPyroclastRep = factionPyroclastRep,
        factionAetherisRep = factionAetherisRep,
        saveStateJson = saveStateJson
    )

    companion object {
        fun fromCloudProfile(
            profile: PlayerCloudProfile,
            pendingSync: Boolean = false
        ): LocalPlayerProfileEntity {
            val resolvedUserId = profile.userId.ifBlank { profile.id.ifBlank { "local_trainer" } }
            return LocalPlayerProfileEntity(
                userId = resolvedUserId,
                trainerName = profile.trainerName,
                level = profile.level,
                gold = profile.gold,
                continentId = profile.continentId,
                badgesCount = profile.badgesCount,
                capturedCount = profile.capturedCount,
                questsCompleted = profile.questsCompleted,
                factionSylvariaRep = profile.factionSylvariaRep,
                factionPyroclastRep = profile.factionPyroclastRep,
                factionAetherisRep = profile.factionAetherisRep,
                saveStateJson = profile.saveStateJson,
                pendingSync = pendingSync,
                updatedAtMillis = profile.updatedAt?.toDate()?.time ?: System.currentTimeMillis()
            )
        }
    }
}
