package com.example.voyagers.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Geocoder wrapper helper to translate text place descriptions to LatLng coordinates.
 */
@Singleton
class GeocoderHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val geocoder = Geocoder(context)

    /**
     * Resolves a location name string to a LatLng object.
     * Returns null on network failures or unresolvable descriptions.
     */
    suspend fun getLatLngFromAddress(addressString: String): LatLng? = withContext(Dispatchers.IO) {
        if (addressString.isBlank()) return@withContext null
        
        try {
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocationName(addressString, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                return@withContext LatLng(address.latitude, address.longitude)
            }
        } catch (e: IOException) {
            // Geocoder service backend network failure
        } catch (e: Exception) {
            // Other structural failures
        }
        return@withContext null
    }
}
