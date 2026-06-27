package com.example.voyagers.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.voyagers.data.model.SmsLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Sent SMS logs database operations.
 */
@Dao
interface SmsLogDao {
    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SmsLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SmsLogEntity): Long

    @Query("DELETE FROM sms_logs")
    suspend fun clearAllLogs(): Int
}
