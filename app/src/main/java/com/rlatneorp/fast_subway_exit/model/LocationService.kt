package com.rlatneorp.fast_subway_exit.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


private class OneTimeLocationCallback(
    private val continuation: CancellableContinuation<Location>
) : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        if (locationResult.lastLocation != null) {
            continuation.resume(locationResult.lastLocation!!)
            return
        }
        continuation.resumeWithException(IllegalAccessException("지역정보를 얻는데 실패하였습니다."))
    }
}

class LocationService(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                continuation.resume(location)
                return@addOnSuccessListener
            }

            if (location == null) {
                requestNewLocation(continuation)
            }
        }.addOnFailureListener {
            continuation.resumeWithException(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation(continuation: CancellableContinuation<Location>) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMaxUpdates(1)
            .build()

        val locationCallback = OneTimeLocationCallback(continuation)

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}