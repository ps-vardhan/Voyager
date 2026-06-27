package com.example.voyagers.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.voyagers.data.model.SmsLogEntity
import com.example.voyagers.data.model.TripEntity
import com.example.voyagers.data.repository.VoyagersRepository
import com.example.voyagers.utils.Constants
import com.example.voyagers.utils.LocationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Foreground Service that handles active location tracking (GPS) or simulated routing in the background,
 * runs distance checks, and dispatches native SMS updates.
 */
@AndroidEntryPoint
class TrackingService : Service() {

    @Inject
    lateinit var repository: VoyagersRepository

    @Inject
    lateinit var locationHelper: LocationHelper

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var trackingJob: Job? = null
    
    // Simulation state
    private var simulationJob: Job? = null
    private var isSimulating = false
    private var simulationSpeedMultiplier = 1

    // Tracks which checkpoints have already fired SMS notifications to prevent duplicate dispatch
    private val notifiedCheckpoints = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_TRACKING -> {
                val simulate = intent.getBooleanExtra("EXTRA_SIMULATE", false)
                isSimulating = simulate
                notifiedCheckpoints.clear()
                startTrackingService()
            }
            Constants.ACTION_STOP_TRACKING -> {
                stopTrackingService()
            }
            Constants.ACTION_SIMULATE_SPEED -> {
                simulationSpeedMultiplier = intent.getIntExtra("EXTRA_SPEED_MULTIPLIER", 1)
            }
        }
        return START_STICKY
    }

    private fun startTrackingService() {
        val notification = buildNotification("Voyagers safety shield is active.")
        startForeground(Constants.NOTIFICATION_ID, notification)

        trackingJob?.cancel()
        simulationJob?.cancel()

        if (isSimulating) {
            startSimulationMode()
        } else {
            startRealLocationTracking()
        }
    }

    private fun startRealLocationTracking() {
        trackingJob = locationHelper.getLocationUpdates(5000L)
            .onEach { location ->
                updateCurrentLocationInDb(location.latitude, location.longitude)
                
                val trip = repository.getActiveTripDirect()
                if (trip != null) {
                    // Check distance to destination
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude, location.longitude,
                        trip.destinationLatitude, trip.destinationLongitude,
                        results
                    )
                    val distanceToDestMeters = results[0]
                    
                    if (distanceToDestMeters < 500f) {
                        triggerCheckpointSms(trip, "Destination Arrival", location.latitude, location.longitude)
                    } else if (distanceToDestMeters < 5000f) {
                        triggerCheckpointSms(trip, "Approaching Destination (5km)", location.latitude, location.longitude)
                    }
                }
            }
            .catch { e ->
                // Log service error
            }
            .launchIn(serviceScope)
    }

    private fun startSimulationMode() {
        simulationJob = serviceScope.launch {
            val activeTrip = repository.getActiveTripDirect() ?: return@launch
            
            val startLat = if (activeTrip.startLatitude != 0.0) activeTrip.startLatitude else 34.0522
            val startLng = if (activeTrip.startLongitude != 0.0) activeTrip.startLongitude else -118.2437
            val destLat = if (activeTrip.destinationLatitude != 0.0) activeTrip.destinationLatitude else 37.7749
            val destLng = if (activeTrip.destinationLongitude != 0.0) activeTrip.destinationLongitude else -122.4194

            val steps = 100
            var currentStep = 0

            while (isActive && currentStep <= steps) {
                val currentTrip = repository.getActiveTripDirect()
                if (currentTrip == null || !currentTrip.isActive) {
                    break
                }

                val fraction = currentStep.toDouble() / steps.toDouble()
                val interpolatedLat = startLat + fraction * (destLat - startLat)
                val interpolatedLng = startLng + fraction * (destLng - startLng)

                updateCurrentLocationInDb(interpolatedLat, interpolatedLng)

                // Trigger simulated milestone checkpoints
                when (currentStep) {
                    25 -> triggerCheckpointSms(currentTrip, "Quarterway Milestone", interpolatedLat, interpolatedLng)
                    50 -> triggerCheckpointSms(currentTrip, "Halfway Milestone", interpolatedLat, interpolatedLng)
                    75 -> triggerCheckpointSms(currentTrip, "Three-Quarterway Milestone", interpolatedLat, interpolatedLng)
                    100 -> triggerCheckpointSms(currentTrip, "Destination Arrival", interpolatedLat, interpolatedLng)
                }

                currentStep++

                val tickDelay = (Constants.SIMULATION_INTERVAL_MS / simulationSpeedMultiplier).coerceAtLeast(100L)
                delay(tickDelay)
            }

            if (currentStep > steps) {
                repository.endTrip()
                stopTrackingService()
            }
        }
    }

    private suspend fun triggerCheckpointSms(
        trip: TripEntity,
        checkpointName: String,
        latitude: Double,
        longitude: Double
    ) {
        if (notifiedCheckpoints.contains(checkpointName)) return
        notifiedCheckpoints.add(checkpointName)

        // Retrieve emergency contacts from database flow
        val contactsList = repository.allContacts.first()
        if (contactsList.isEmpty()) return

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        // Update Foreground Notification status message to show checkpoint
        updateNotification("Reached: $checkpointName safely.")

        contactsList.forEach { contact ->
            val messageText = "[Voyagers Shield Alert] ${trip.travelerName} has reached checkpoint: $checkpointName safely. Location: https://maps.google.com/?q=$latitude,$longitude"

            if (hasSmsPermission) {
                try {
                    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        this.getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }
                    smsManager.sendTextMessage(contact.phoneNumber, null, messageText, null, null)
                } catch (e: Exception) {
                    // Log SMS hardware error
                }
            }

            // Save sent record to Room log history
            repository.insertSmsLog(
                SmsLogEntity(
                    contactName = contact.name,
                    phoneNumber = contact.phoneNumber,
                    messageText = messageText,
                    isSimulated = !hasSmsPermission
                )
            )
        }
    }

    private suspend fun updateCurrentLocationInDb(latitude: Double, longitude: Double) {
        val activeTrip = repository.getActiveTripDirect() ?: return
        val updatedTrip = activeTrip.copy(
            currentLatitude = latitude,
            currentLongitude = longitude
        )
        repository.updateTrip(updatedTrip)
    }

    private fun stopTrackingService() {
        trackingJob?.cancel()
        simulationJob?.cancel()
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Voyagers Travel Guardian")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Voyagers active location tracking notification channel."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
