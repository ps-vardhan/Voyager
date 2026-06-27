package com.example.voyagers.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyagers.data.model.EmergencyContact
import com.example.voyagers.data.model.SmsLogEntity
import com.example.voyagers.data.model.TripEntity
import com.example.voyagers.data.repository.VoyagersRepository
import com.example.voyagers.service.TrackingService
import com.example.voyagers.utils.Constants
import com.example.voyagers.utils.GeocoderHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to manage active trip state, emergency contacts, and input validations using Room and Hilt.
 */
@HiltViewModel
class TripViewModel @Inject constructor(
    private val repository: VoyagersRepository,
    private val geocoderHelper: GeocoderHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val activeTrip: StateFlow<TripEntity?> = repository.activeTrip
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contacts: StateFlow<List<EmergencyContact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smsLogs: StateFlow<List<SmsLogEntity>> = repository.allSmsLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError: StateFlow<String?> = _uiError.asStateFlow()

    private val _isSearchingLocation = MutableStateFlow(false)
    val isSearchingLocation: StateFlow<Boolean> = _isSearchingLocation.asStateFlow()

    /**
     * Validates inputs and inserts a new emergency contact.
     */
    fun addContact(name: String, phoneNumber: String): Boolean {
        if (name.isBlank()) {
            _uiError.value = "Contact name cannot be empty."
            return false
        }
        if (phoneNumber.isBlank() || !phoneNumber.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }) {
            _uiError.value = "Please enter a valid phone number."
            return false
        }

        viewModelScope.launch {
            repository.insertContact(
                EmergencyContact(name = name.trim(), phoneNumber = phoneNumber.trim())
            )
        }
        clearError()
        return true
    }

    /**
     * Deletes an emergency contact.
     */
    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    /**
     * Validates input values and starts a new trip configuration.
     */
    fun startTrip(travelerName: String, startLocation: String, destination: String): Boolean {
        if (travelerName.isBlank()) {
            _uiError.value = "Traveler name is required."
            return false
        }
        if (startLocation.isBlank()) {
            _uiError.value = "Current start location is required."
            return false
        }
        if (destination.isBlank()) {
            _uiError.value = "Destination location is required."
            return false
        }
        if (contacts.value.isEmpty()) {
            _uiError.value = "Please add at least one emergency contact before starting."
            return false
        }

        viewModelScope.launch {
            _isSearchingLocation.value = true

            // Resolve coordinates from Geocoder Helper
            val startLatLng = geocoderHelper.getLatLngFromAddress(startLocation)
            val destLatLng = geocoderHelper.getLatLngFromAddress(destination)

            val startLat = startLatLng?.latitude ?: 34.0522 // Fallback to Los Angeles
            val startLng = startLatLng?.longitude ?: -118.2437
            val destLat = destLatLng?.latitude ?: 37.7749 // Fallback to San Francisco
            val destLng = destLatLng?.longitude ?: -122.4194

            repository.startTrip(
                travelerName = travelerName.trim(),
                startLocationName = startLocation.trim(),
                destinationName = destination.trim(),
                startLat = startLat,
                startLng = startLng,
                destLat = destLat,
                destLng = destLng
            )

            // Start the background tracking service
            val intent = Intent(context, TrackingService::class.java).apply {
                action = Constants.ACTION_START_TRACKING
                putExtra("EXTRA_SIMULATE", true) // Default to simulated tracking for Phase 2
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            _isSearchingLocation.value = false
        }
        clearError()
        return true
    }

    /**
     * Ends the currently active trip.
     */
    fun endTrip() {
        viewModelScope.launch {
            repository.endTrip()

            // Stop the background tracking service
            val intent = Intent(context, TrackingService::class.java).apply {
                action = Constants.ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }

    /**
     * Updates the simulation speed multiplier in the background service.
     */
    fun updateSimulationSpeed(speedMultiplier: Int) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = Constants.ACTION_SIMULATE_SPEED
            putExtra("EXTRA_SPEED_MULTIPLIER", speedMultiplier)
        }
        context.startService(intent)
    }

    fun clearError() {
        _uiError.value = null
    }

    fun clearSmsLogs() {
        viewModelScope.launch {
            repository.clearSmsLogs()
        }
    }
}
