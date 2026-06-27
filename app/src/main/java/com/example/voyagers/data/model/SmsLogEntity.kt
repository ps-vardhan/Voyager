package com.example.voyagers.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a log entry for sent emergency messages.
 */
@Entity(tableName = "sms_logs")
data class SmsLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contactName: String,
    val phoneNumber: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSimulated: Boolean = true
)
