package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        MatchHistoryEntity::class,
        HouseRulePresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UnoDatabase : RoomDatabase() {

    abstract fun unoDao(): UnoDao

    companion object {
        @Volatile
        private var INSTANCE: UnoDatabase? = null

        fun getDatabase(context: Context): UnoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UnoDatabase::class.java,
                    "uno_arena_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
