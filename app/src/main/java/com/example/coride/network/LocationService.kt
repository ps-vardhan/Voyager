package com.example.coride.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class LocationService : Service() {

    private val NOTIFICATION_ID = 9918
    private val CHANNEL_ID = "voyageur_gps_channel"
    private val TAG = "LocationService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Location Tracking Service created.")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "START_TRACKING") {
            startForegroundServiceWithNotification()
            // In a production app, here you would register for FusedLocationProvider updates.
            // For mock demo/reliability, we simulate starting background location loops.
            Log.d(TAG, "Background GPS tracking started.")
        } else if (action == "STOP_TRACKING") {
            Log.d(TAG, "Background GPS tracking stopped.")
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voyageur GPS Tracking")
            .setContentText("Monitoring stops and sending alerts in background...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Voyageur Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Location Tracking Service destroyed.")
    }
}
