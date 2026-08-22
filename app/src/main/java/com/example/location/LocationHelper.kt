package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationPreset(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val subtitle: String
)

object LocationPresets {
    val defaultLocations = listOf(
        LocationPreset("Mount Shasta, CA", 41.3099, -122.3106, "Sacred Mountain Sanctuary"),
        LocationPreset("Sedona, AZ", 34.8697, -111.7610, "Vortex Energy Center"),
        LocationPreset("Rishikesh, India", 30.0869, 78.2676, "Capital of Yoga & Meditation"),
        LocationPreset("Kyoto, Japan", 35.0116, 135.7681, "Ancient Zen Monasteries"),
        LocationPreset("Glastonbury, UK", 51.1464, -2.7153, "Avalon & Isle of Glass"),
        LocationPreset("Cusco, Peru", -13.5319, -71.9675, "Sacred Valley of the Incas"),
        LocationPreset("Maui, Hawaii", 20.7984, -156.3319, "Haleakalā Sun Sanctuary"),
        LocationPreset("New York, NY", 40.7128, -74.0060, "Metropolitan Meridian"),
        LocationPreset("San Francisco, CA", 37.7749, -122.4194, "Pacific Bay"),
        LocationPreset("London, UK", 51.5074, -0.1278, "Prime Meridian"),
        LocationPreset("São Paulo, Brazil", -23.5505, -46.6333, "Southern Hemisphere Hub"),
        LocationPreset("Sydney, Australia", -33.8688, 151.2093, "Austral Sanctuary")
    )
}

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(): Pair<Double, Double>? {
        return try {
            val location: Location? = fusedLocationClient.lastLocation.await()
            if (location != null) {
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                    val country = addr.countryCode ?: addr.countryName ?: ""
                    if (locality.isNotEmpty() && country.isNotEmpty()) {
                        "$locality, $country"
                    } else if (locality.isNotEmpty()) {
                        locality
                    } else {
                        String.format(Locale.US, "%.4f°, %.4f°", latitude, longitude)
                    }
                } else {
                    String.format(Locale.US, "%.4f°, %.4f°", latitude, longitude)
                }
            } catch (e: Exception) {
                String.format(Locale.US, "%.4f°, %.4f°", latitude, longitude)
            }
        }
    }
}
