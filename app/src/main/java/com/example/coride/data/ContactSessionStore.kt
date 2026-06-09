package com.example.coride.data

import android.content.Context
import android.content.SharedPreferences

data class EmergencyContact(
    val name: String,
    val phone: String
)

object ContactSessionStore {
    private const val PREFS_NAME = "coride_session_prefs"
    private const val KEY_CONTACTS = "emergency_contacts"

    // Simple in-memory session cache
    private val contactsList = mutableListOf<EmergencyContact>()

    fun getContacts(context: Context): List<EmergencyContact> {
        if (contactsList.isNotEmpty()) {
            return contactsList
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawStr = prefs.getString(KEY_CONTACTS, "") ?: ""
        if (rawStr.isNotEmpty()) {
            val loaded = rawStr.split(";").mapNotNull {
                val parts = it.split(",")
                if (parts.size == 2) {
                    EmergencyContact(parts[0], parts[1])
                } else null
            }
            contactsList.clear()
            contactsList.addAll(loaded)
        }
        return contactsList
    }

    fun addContact(context: Context, contact: EmergencyContact) {
        contactsList.add(contact)
        saveToPreferences(context)
    }

    fun removeContact(context: Context, contact: EmergencyContact) {
        contactsList.remove(contact)
        saveToPreferences(context)
    }

    private fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawStr = contactsList.joinToString(";") { "${it.name},${it.phone}" }
        prefs.edit().putString(KEY_CONTACTS, rawStr).apply()
    }
}
