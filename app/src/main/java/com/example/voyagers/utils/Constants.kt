package com.example.voyagers.utils

/**
 * Global Constants for notifications, channels, and locations.
 */
object Constants {
    const val NOTIFICATION_CHANNEL_ID = "voyagers_tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Voyagers Voyage Tracking"
    const val NOTIFICATION_ID = 4124
    
    const val ACTION_START_TRACKING = "com.example.voyagers.ACTION_START_TRACKING"
    const val ACTION_STOP_TRACKING = "com.example.voyagers.ACTION_STOP_TRACKING"
    const val ACTION_SIMULATE_SPEED = "com.example.voyagers.ACTION_SIMULATE_SPEED"

    // Default simulation update interval in milliseconds
    const val SIMULATION_INTERVAL_MS = 2000L
}
