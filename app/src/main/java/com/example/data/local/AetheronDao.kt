package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for local [LocalMonsterEntity] CRUD and synchronization queries.
 */
@Dao
interface LocalMonsterDao {

    @Query("SELECT * FROM local_monsters WHERE userId = :userId AND isDeletedLocally = 0 ORDER BY updatedAtMillis DESC")
    fun observeMonstersByUser(userId: String): Flow<List<LocalMonsterEntity>>

    @Query("SELECT * FROM local_monsters WHERE userId = :userId AND isDeletedLocally = 0 ORDER BY updatedAtMillis DESC")
    suspend fun getMonstersByUser(userId: String): List<LocalMonsterEntity>

    @Query("SELECT * FROM local_monsters WHERE id = :monsterId AND isDeletedLocally = 0 LIMIT 1")
    suspend fun getMonsterById(monsterId: String): LocalMonsterEntity?

    @Query("SELECT * FROM local_monsters WHERE pendingSync = 1")
    suspend fun getPendingSyncMonsters(): List<LocalMonsterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonster(monster: LocalMonsterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonsters(monsters: List<LocalMonsterEntity>)

    @Query("UPDATE local_monsters SET userId = :newUserId, pendingSync = 1, updatedAtMillis = :timestamp WHERE userId = :oldUserId")
    suspend fun reassignUserMonsters(
        oldUserId: String,
        newUserId: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE local_monsters SET isDeletedLocally = 1, pendingSync = 1, updatedAtMillis = :timestamp WHERE id = :monsterId")
    suspend fun markMonsterDeleted(
        monsterId: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM local_monsters WHERE id = :monsterId")
    suspend fun hardDeleteMonsterById(monsterId: String)
}

/**
 * Room DAO for local [LocalPlayerProfileEntity] persistence and synchronization queries.
 */
@Dao
interface LocalPlayerProfileDao {

    @Query("SELECT * FROM local_player_profiles WHERE userId = :userId LIMIT 1")
    fun observeProfile(userId: String): Flow<LocalPlayerProfileEntity?>

    @Query("SELECT * FROM local_player_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfile(userId: String): LocalPlayerProfileEntity?

    @Query("SELECT * FROM local_player_profiles WHERE pendingSync = 1")
    suspend fun getPendingSyncProfiles(): List<LocalPlayerProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: LocalPlayerProfileEntity)

    @Query("DELETE FROM local_player_profiles WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)
}
