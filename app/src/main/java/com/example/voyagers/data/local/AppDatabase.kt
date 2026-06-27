package com.example.voyagers.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.voyagers.data.model.EmergencyContact
import com.example.voyagers.data.model.TripEntity

/**
 * Main application Room database.
 */
import com.example.voyagers.data.model.SmsLogEntity

/**
 * Main application Room database.
 */
@Database(
    entities = [EmergencyContact::class, TripEntity::class, SmsLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun tripDao(): TripDao
    abstract fun smsLogDao(): SmsLogDao
}
