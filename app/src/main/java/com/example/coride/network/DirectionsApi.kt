package com.example.coride.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

data class RouteResult(
    val polylinePoints: List<Pair<Double, Double>>,
    val waypoints: List<Pair<Double, Double>>,
    val durationText: String,
    val distanceText: String
)

object DirectionsApi {
    private const val TAG = "DirectionsApi"

    suspend fun getDirections(
        from: String,
        to: String,
        apiKey: String?
    ): RouteResult = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty() || apiKey == "YOUR_API_KEY_HERE") {
            Log.d(TAG, "No API Key provided. Returning mock routing details.")
            return@withContext getMockRoute(from, to)
        }

        try {
            val urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=${from}&destination=${to}&key=$apiKey"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            
            if (json.getString("status") == "OK") {
                val routes = json.getJSONArray("routes")
                val route = routes.getJSONObject(0)
                val overviewPolyline = route.getJSONObject("overview_polyline")
                val pointsStr = overviewPolyline.getString("points")
                val points = decodePolyline(pointsStr)
                
                val legs = route.getJSONArray("legs")
                val leg = legs.getJSONObject(0)
                val distance = leg.getJSONObject("distance").getString("text")
                val duration = leg.getJSONObject("duration").getString("text")
                
                // Sample 5 waypoints along the route for finding POIs
                val waypoints = mutableListOf<Pair<Double, Double>>()
                if (points.isNotEmpty()) {
                    val step = (points.size / 5).coerceAtLeast(1)
                    for (i in 0 until points.size step step) {
                        waypoints.add(points[i])
                    }
                }
                
                return@withContext RouteResult(points, waypoints, duration, distance)
            } else {
                Log.e(TAG, "Directions API error status: ${json.getString("status")}")
                return@withContext getMockRoute(from, to)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching directions", e)
            return@withContext getMockRoute(from, to)
        }
    }

    private fun getMockRoute(from: String, to: String): RouteResult {
        // Return a mock path starting from San Francisco coordinates to Los Angeles (approximate)
        val startLat = 37.7749
        val startLng = -122.4194
        val endLat = 34.0522
        val endLng = -118.2437
        
        val points = mutableListOf<Pair<Double, Double>>()
        val segments = 20
        for (i in 0..segments) {
            val fraction = i.toDouble() / segments
            val lat = startLat + (endLat - startLat) * fraction + (Math.sin(fraction * Math.PI) * 0.5)
            val lng = startLng + (endLng - startLng) * fraction
            points.add(Pair(lat, lng))
        }
        
        val waypoints = listOf(
            points[0],
            points[5],
            points[10],
            points[15],
            points[20]
        )
        
        return RouteResult(
            polylinePoints = points,
            waypoints = waypoints,
            durationText = "5 hrs 45 mins",
            distanceText = "382 miles"
        )
    }

    private fun decodePolyline(encoded: String): List<Pair<Double, Double>> {
        val poly = ArrayList<Pair<Double, Double>>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shl 1).inv() else result shl 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shl 1).inv() else result shl 1
            lng += dlng

            val p = Pair(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }
}
