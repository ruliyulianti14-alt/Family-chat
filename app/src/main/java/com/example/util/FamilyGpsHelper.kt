package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class GpsReading(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val altitudeMeters: Double?,
    val speedMetersPerSec: Float?,
    val timestampMs: Long
)

object FamilyGpsHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun hasPreciseLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun isDeviceLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    fun isHardwareGpsProviderEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
    }

    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openInMaps(context: Context, latitude: Double, longitude: Double, label: String): Boolean {
        val encodedLabel = Uri.encode(label)
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
        val geoIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(geoIntent)
            true
        } catch (_: ActivityNotFoundException) {
            val webUri = Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(webIntent) }.isSuccess
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentHighAccuracyFix(context: Context): Result<GpsReading> {
        if (!hasLocationPermission(context)) {
            return Result.failure(SecurityException("Izin akses GPS belum diberikan."))
        }
        if (!isDeviceLocationEnabled(context)) {
            return Result.failure(IllegalStateException("GPS / Layanan Lokasi perangkat sedang nonaktif."))
        }
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val priority = if (hasPreciseLocationPermission(context)) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val current = suspendCancellableCoroutine<Location?> { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            fusedClient.getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }

        val resolved = current ?: suspendCancellableCoroutine<Location?> { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }

        return if (resolved != null) {
            Result.success(resolved.toGpsReading())
        } else {
            Result.failure(
                IllegalStateException(
                    "Sinyal GPS belum ditemukan. Pastikan GPS aktif atau atur lokasi di emulator/perangkat."
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun observeLiveGpsUpdates(context: Context, intervalMs: Long = 4000L): Flow<GpsReading> = callbackFlow {
        if (!hasLocationPermission(context)) {
            close(SecurityException("Izin akses GPS belum diberikan."))
            return@callbackFlow
        }
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val priority = if (hasPreciseLocationPermission(context)) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val request = LocationRequest.Builder(priority, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(loc.toGpsReading())
                }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { err -> close(err) }

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val first = addresses.firstOrNull()
                            val line = first?.let { addr ->
                                listOfNotNull(
                                    addr.thoroughfare,
                                    addr.subLocality,
                                    addr.locality,
                                    addr.adminArea
                                ).distinct().joinToString(", ").ifBlank { addr.getAddressLine(0) }
                            }
                            if (cont.isActive) cont.resume(line)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val first = addresses?.firstOrNull()
                    first?.let { addr ->
                        listOfNotNull(
                            addr.thoroughfare,
                            addr.subLocality,
                            addr.locality,
                            addr.adminArea
                        ).distinct().joinToString(", ").ifBlank { addr.getAddressLine(0) }
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    fun calculateDistanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Float {
        val results = FloatArray(2)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

    fun calculateBearingDegrees(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Float {
        val results = FloatArray(2)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[1]
    }

    fun formatDistance(meters: Float): String =
        if (meters < 1000f) {
            "${meters.toInt()} m dari Anda"
        } else {
            String.format(Locale.US, "%.2f km dari Anda", meters / 1000f)
        }

    private fun Location.toGpsReading(): GpsReading = GpsReading(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracy,
        altitudeMeters = if (hasAltitude()) altitude else null,
        speedMetersPerSec = if (hasSpeed()) speed else null,
        timestampMs = time
    )
}
