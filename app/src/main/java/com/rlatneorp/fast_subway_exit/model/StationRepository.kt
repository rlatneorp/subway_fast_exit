package com.rlatneorp.fast_subway_exit.model

import android.content.Context
import com.rlatneorp.fast_subway_exit.model.network.ApiService
import com.rlatneorp.fast_subway_exit.model.network.RetrofitClient

class StationRepository(context: Context) {

    private val apiService: ApiService = RetrofitClient.instance
    private val locationService: LocationService = LocationService(context.applicationContext)

    suspend fun getElevatorInfoForCurrentLocation(): Result<ElevatorStatus> {
        return try {
            val location = locationService.getCurrentLocation()
            val response = apiService.getElevatorStatusByLocation(location.latitude, location.longitude)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API 에러: ${response.message()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchElevatorInfoByStation(stationName: String): Result<ElevatorStatus> {
        return try {
            val response = apiService.getElevatorInfoByName(stationName)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API 에러: ${response.message()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}