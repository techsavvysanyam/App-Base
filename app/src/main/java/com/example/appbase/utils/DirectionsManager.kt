package com.example.appbase.utils

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class RouteInfo(
    val distance: String,
    val duration: String,
    val polylineOptions: PolylineOptions
)

class DirectionsManager(private val context: Context, private val apiKey: String) {
    
    /**
     * Get directions between two points using Google Directions API
     */
    suspend fun getDirections(
        origin: LatLng,
        destination: LatLng,
        travelMode: String = "driving"
    ): Result<RouteInfo> = withContext(Dispatchers.IO) {
        try {
            val url = buildDirectionsUrl(origin, destination, travelMode)
            val response = makeHttpRequest(url)
            
            if (response != null) {
                val jsonResponse = JSONObject(response)
                val routes = jsonResponse.getJSONArray("routes")
                
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val legs = route.getJSONArray("legs")
                    val leg = legs.getJSONObject(0)
                    
                    val distance = leg.getJSONObject("distance").getString("text")
                    val duration = leg.getJSONObject("duration").getString("text")
                    val polylineOptions = createPolylineOptions(route)
                    
                    val routeInfo = RouteInfo(
                        distance = distance,
                        duration = duration,
                        polylineOptions = polylineOptions
                    )
                    
                    Result.success(routeInfo)
                } else {
                    Result.failure(Exception("No routes found"))
                }
            } else {
                Result.failure(Exception("Failed to get directions"))
            }
        } catch (e: Exception) {
            Log.e("DirectionsManager", "Error getting directions: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Build Google Directions API URL
     */
    private fun buildDirectionsUrl(
        origin: LatLng,
        destination: LatLng,
        travelMode: String
    ): String {
        val originStr = "${origin.latitude},${origin.longitude}"
        val destinationStr = "${destination.latitude},${destination.longitude}"
        
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$originStr" +
                "&destination=$destinationStr" +
                "&mode=$travelMode" +
                "&key=$apiKey"
    }
    
    /**
     * Make HTTP request to Google Directions API
     */
    private fun makeHttpRequest(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e("DirectionsManager", "HTTP Error: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("DirectionsManager", "HTTP Request Error: ${e.message}")
            null
        }
    }
    
    /**
     * Create PolylineOptions from route JSON
     */
    private fun createPolylineOptions(route: JSONObject): PolylineOptions {
        val polylineOptions = PolylineOptions()
            .color(Color.parseColor("#4F46E5")) // Primary color
            .width(10f)
            .geodesic(true)
        
        try {
            val overviewPolyline = route.getJSONObject("overview_polyline")
            val points = overviewPolyline.getString("points")
            
            // Decode polyline points
            val decodedPoints = decodePolyline(points)
            polylineOptions.addAll(decodedPoints)
        } catch (e: Exception) {
            Log.e("DirectionsManager", "Error creating polyline: ${e.message}")
        }
        
        return polylineOptions
    }
    
    /**
     * Decode polyline string to list of LatLng points
     */
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
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
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        
        return poly
    }
    
    /**
     * Get travel time estimate
     */
    suspend fun getTravelTime(
        origin: LatLng,
        destination: LatLng,
        travelMode: String = "driving"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = buildDirectionsUrl(origin, destination, travelMode)
            val response = makeHttpRequest(url)
            
            if (response != null) {
                val jsonResponse = JSONObject(response)
                val routes = jsonResponse.getJSONArray("routes")
                
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val legs = route.getJSONArray("legs")
                    val leg = legs.getJSONObject(0)
                    val duration = leg.getJSONObject("duration").getString("text")
                    Result.success(duration)
                } else {
                    Result.failure(Exception("No routes found"))
                }
            } else {
                Result.failure(Exception("Failed to get travel time"))
            }
        } catch (e: Exception) {
            Log.e("DirectionsManager", "Error getting travel time: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Clean up resources (no-op for HTTP-based implementation)
     */
    fun shutdown() {
        // No cleanup needed for HTTP-based implementation
    }
}
