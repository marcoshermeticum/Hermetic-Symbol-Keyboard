package com.hermetic.keyboard.symbols.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, RecentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SymbolDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentDao(): RecentDao

    companion object {
        @Volatile
        private var INSTANCE: SymbolDatabase? = null

        fun getInstance(context: Context): SymbolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SymbolDatabase::class.java,
                    "hermetic_symbols.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
