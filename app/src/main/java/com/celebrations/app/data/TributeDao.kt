package com.celebrations.app.data

import androidx.room.*
import com.celebrations.app.model.Tribute
import kotlinx.coroutines.flow.Flow

@Dao
interface TributeDao {
    @Query("SELECT * FROM tributes ORDER BY date DESC")
    fun getAllTributes(): Flow<List<Tribute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTribute(tribute: Tribute)

    @Update
    suspend fun updateTribute(tribute: Tribute)

    @Query("SELECT * FROM tributes WHERE id = :id")
    suspend fun getTributeById(id: String): Tribute?

    @Query("SELECT * FROM tributes WHERE isDownloaded = 0")
    suspend fun getPendingDownloads(): List<Tribute>
}
