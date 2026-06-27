package com.example.voyagers.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a voyage/trip details.
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val travelerName: String,
    val startLocationName: String,
    val destinationName: String,
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val destinationLatitude: Double = 0.0,
    val destinationLongitude: Double = 0.0,
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val isActive: Boolean = false,
    val startTimeStamp: Long = System.currentTimeMillis(),
    val endTimeStamp: Long = 0L
)
