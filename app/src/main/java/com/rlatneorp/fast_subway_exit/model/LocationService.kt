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

class LocationService(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                continuation.resume(location)
            } else {
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

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    continuation.resume(it)
                    return
                }
                continuation.resumeWithException(Exception("지역 정보를 얻는데 실패하였습니다."))
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}