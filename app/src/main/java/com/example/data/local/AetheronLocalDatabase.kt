package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Local Room SQLite Database serving as the offline-first and unauthenticated persistence
 * source for Aetheron monsters and player profiles.
 */
@Database(
    entities = [
        LocalMonsterEntity::class,
        LocalPlayerProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AetheronLocalDatabase : RoomDatabase() {

    abstract fun localMonsterDao(): LocalMonsterDao
    abstract fun localPlayerProfileDao(): LocalPlayerProfileDao

    companion object {
        private const val DATABASE_NAME = "aetheron_local.db"

        @Volatile
        private var INSTANCE: AetheronLocalDatabase? = null

        fun getInstance(context: Context): AetheronLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AetheronLocalDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Creates an isolated in-memory Room database instance for local unit and Robolectric tests.
         */
        fun createInMemory(context: Context): AetheronLocalDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AetheronLocalDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
