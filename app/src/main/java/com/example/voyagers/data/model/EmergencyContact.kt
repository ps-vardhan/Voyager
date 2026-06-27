package com.example.voyagers.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Database entity representing an emergency contact.
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String
)
