package com.example.coride.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

data class PlacePoi(
    val name: String,
    val rating: Double,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val iconUrl: String
)

object PlacesApi {
    private const val TAG = "PlacesApi"

    suspend fun getNearbyStops(
        waypoints: List<Pair<Double, Double>>,
        apiKey: String?
    ): List<PlacePoi> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty() || apiKey == "YOUR_API_KEY_HERE" || waypoints.isEmpty()) {
            return@withContext getMockPlaces()
        }

        val allPlaces = mutableListOf<PlacePoi>()
        
        // Search near the midpoint waypoint to avoid making too many API calls
        val midpoint = waypoints[waypoints.size / 2]
        
        try {
            val urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${midpoint.first},${midpoint.second}&radius=15000&type=tourist_attraction&key=$apiKey"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            
            if (json.getString("status") == "OK") {
                val results = json.getJSONArray("results")
                for (i in 0 until minOf(results.length(), 6)) {
                    val item = results.getJSONObject(i)
                    val name = item.getString("name")
                    val rating = item.optDouble("rating", 4.0)
                    val geometry = item.getJSONObject("geometry")
                    val location = geometry.getJSONObject("location")
                    val lat = location.getDouble("lat")
                    val lng = location.getDouble("lng")
                    val vicinity = item.optString("vicinity", "Nearby area")
                    
                    allPlaces.add(
                        PlacePoi(
                            name = name,
                            rating = rating,
                            category = "Attraction",
                            latitude = lat,
                            longitude = lng,
                            address = vicinity,
                            iconUrl = "https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/monument-71.png"
                        )
                    )
                }
            } else {
                Log.e(TAG, "Places API error status: ${json.getString("status")}")
                return@withContext getMockPlaces()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching nearby places", e)
            return@withContext getMockPlaces()
        }
        
        return@withContext if (allPlaces.isEmpty()) getMockPlaces() else allPlaces
    }

    private fun getMockPlaces(): List<PlacePoi> {
        return listOf(
            PlacePoi(
                name = "Pebble Beach Ocean View",
                rating = 4.8,
                category = "Scenic Spot",
                latitude = 36.5682,
                longitude = -121.9463,
                address = "17 Mile Dr, Pebble Beach, CA",
                iconUrl = "scenic"
            ),
            PlacePoi(
                name = "Bixby Creek Bridge",
                rating = 4.9,
                category = "Landmark",
                latitude = 36.3714,
                longitude = -121.9026,
                address = "CA-1, Monterey, CA",
                iconUrl = "landmark"
            ),
            PlacePoi(
                name = "Nepenthe Restaurant",
                rating = 4.6,
                category = "Food & Dining",
                latitude = 36.2418,
                longitude = -121.7588,
                address = "48510 Highway 1, Big Sur, CA",
                iconUrl = "food"
            ),
            PlacePoi(
                name = "McWay Falls",
                rating = 4.9,
                category = "Nature",
                latitude = 36.1578,
                longitude = -121.6721,
                address = "Julia Pfeiffer Burns State Park",
                iconUrl = "nature"
            ),
            PlacePoi(
                name = "Hearst Castle",
                rating = 4.7,
                category = "Museum",
                latitude = 35.6852,
                longitude = -121.1666,
                address = "750 Hearst Castle Rd, San Simeon, CA",
                iconUrl = "museum"
            )
        )
    }
}
