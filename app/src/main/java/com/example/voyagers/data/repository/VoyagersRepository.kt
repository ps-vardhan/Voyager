package com.example.voyagers.data.repository

import com.example.voyagers.data.local.ContactDao
import com.example.voyagers.data.local.SmsLogDao
import com.example.voyagers.data.local.TripDao
import com.example.voyagers.data.model.EmergencyContact
import com.example.voyagers.data.model.SmsLogEntity
import com.example.voyagers.data.model.TripEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository grouping DAO operations for Room Database access, optimized with IO dispatchers.
 */
@Singleton
class VoyagersRepository @Inject constructor(
    private val contactDao: ContactDao,
    private val tripDao: TripDao,
    private val smsLogDao: SmsLogDao
) {
    val allContacts: Flow<List<EmergencyContact>> = contactDao.getAllContacts()
    val activeTrip: Flow<TripEntity?> = tripDao.getActiveTrip()
    val allSmsLogs: Flow<List<SmsLogEntity>> = smsLogDao.getAllLogs()

    suspend fun getActiveTripDirect(): TripEntity? = withContext(Dispatchers.IO) {
        tripDao.getActiveTripDirect()
    }

    suspend fun insertContact(contact: EmergencyContact) = withContext(Dispatchers.IO) {
        contactDao.insertContact(contact)
    }

    suspend fun deleteContact(contact: EmergencyContact) = withContext(Dispatchers.IO) {
        contactDao.deleteContact(contact)
    }

    suspend fun deleteContactById(id: String) = withContext(Dispatchers.IO) {
        contactDao.deleteContactById(id)
    }

    suspend fun startTrip(
        travelerName: String,
        startLocationName: String,
        destinationName: String,
        startLat: Double = 0.0,
        startLng: Double = 0.0,
        destLat: Double = 0.0,
        destLng: Double = 0.0
    ) = withContext(Dispatchers.IO) {
        tripDao.deactivateActiveTrips()
        val trip = TripEntity(
            travelerName = travelerName,
            startLocationName = startLocationName,
            destinationName = destinationName,
            startLatitude = startLat,
            startLongitude = startLng,
            destinationLatitude = destLat,
            destinationLongitude = destLng,
            currentLatitude = startLat,
            currentLongitude = startLng,
            isActive = true
        )
        tripDao.insertTrip(trip)
    }

    suspend fun endTrip() = withContext(Dispatchers.IO) {
        tripDao.deactivateActiveTrips()
    }

    suspend fun updateTrip(trip: TripEntity) = withContext(Dispatchers.IO) {
        tripDao.updateTrip(trip)
    }

    suspend fun insertSmsLog(log: SmsLogEntity) = withContext(Dispatchers.IO) {
        smsLogDao.insertLog(log)
    }

    suspend fun clearSmsLogs() = withContext(Dispatchers.IO) {
        smsLogDao.clearAllLogs()
    }
}
