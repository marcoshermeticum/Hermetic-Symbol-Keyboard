package com.hermetic.keyboard.symbols.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    suspend fun getAll(): List<FavoriteEntity>

    @Query("SELECT symbolId FROM favorites")
    suspend fun getAllIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE symbolId = :symbolId")
    suspend fun deleteById(symbolId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE symbolId = :symbolId)")
    suspend fun isFavorite(symbolId: String): Boolean
}

@Dao
interface RecentDao {
    @Query("SELECT * FROM recents ORDER BY usedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 30): List<RecentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recent: RecentEntity)

    @Query("DELETE FROM recents WHERE symbolId NOT IN (SELECT symbolId FROM recents ORDER BY usedAt DESC LIMIT :keep)")
    suspend fun trimToSize(keep: Int = 30)

    @Query("DELETE FROM recents")
    suspend fun clearAll()
}
