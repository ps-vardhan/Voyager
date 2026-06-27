package com.example.voyagers.data.local

import androidx.room.*
import com.example.voyagers.data.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Emergency Contacts database operations.
 */
@Dao
interface ContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    @Delete
    suspend fun deleteContact(contact: EmergencyContact): Int

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContactById(id: String): Int
}
