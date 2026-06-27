package com.example.voyagers.data.local

import androidx.room.*
import com.example.voyagers.data.model.TripEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Trip/Voyage database operations.
 */
@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE isActive = 1 LIMIT 1")
    fun getActiveTrip(): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTripDirect(): TripEntity?

    @Query("SELECT * FROM trips ORDER BY startTimeStamp DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity): Int

    @Delete
    suspend fun deleteTrip(trip: TripEntity): Int

    @Query("UPDATE trips SET isActive = 0, endTimeStamp = :endTime WHERE isActive = 1")
    suspend fun deactivateActiveTrips(endTime: Long = System.currentTimeMillis()): Int
}
