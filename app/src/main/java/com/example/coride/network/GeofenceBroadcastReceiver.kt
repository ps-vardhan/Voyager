package com.example.coride.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.coride.data.ContactSessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "GeofenceReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val transitionType = intent.getIntExtra("transition_type", 1) // 1 = ENTER, 2 = EXIT
        val waypointName = intent.getStringExtra("waypoint_name") ?: "Unknown Checkpoint"
        
        Log.d(TAG, "Geofence transition triggered! Type: $transitionType, Waypoint: $waypointName")
        
        val contacts = ContactSessionStore.getContacts(context)
        if (contacts.isEmpty()) {
            Log.d(TAG, "No emergency contacts defined to alert.")
            return
        }

        val transitionStr = if (transitionType == 1) "entered" else "exited"
        val message = "Voyageur Alert: Traveler has safely $transitionStr checkpoint: $waypointName."

        // Asynchronously dispatch messages
        CoroutineScope(Dispatchers.IO).launch {
            for (contact in contacts) {
                Log.d(TAG, "Notifying contact: ${contact.name} (${contact.phone})")
                TwilioSmsSender.sendSms(
                    toPhone = contact.phone,
                    message = message,
                    accountSid = null, // Mock values, or fetch from BuildConfig/Secrets
                    authToken = null,
                    fromPhone = null
                )
            }
        }
    }
}
