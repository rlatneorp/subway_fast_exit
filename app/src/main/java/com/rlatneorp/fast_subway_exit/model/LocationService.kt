package com.rlatneorp.fast_subway_exit.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

        continuation.resumeWithException(Exception("지역정보 얻기를 실패하였습니다."))
    }
}

private class FlowLocationCallback(
    private val scope: ProducerScope<Location>
) : LocationCallback() {

    override fun onLocationResult(locationResult: LocationResult) {
        locationResult.lastLocation?.let {
            scope.trySend(it)
        }
    }
}

class LocationService(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                handleLastLocationSuccess(location, continuation)
            }
            .addOnFailureListener {
                continuation.resumeWithException(it)
            }
    }

    private fun handleLastLocationSuccess(
        location: Location?,
        continuation: CancellableContinuation<Location>
    ) {
        if (location != null) {
            continuation.resume(location)
            return
        }
        requestNewLocation(continuation)
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

    @SuppressLint("MissingPermission")
    fun locationUpdatesFlow(): Flow<Location> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()

        val locationCallback = FlowLocationCallback(this)

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}