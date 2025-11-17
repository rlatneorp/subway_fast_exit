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
            handleApiResponse(response) // (로직 추출)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun searchElevatorInfoByStation(stationName: String): Result<ElevatorStatus> {
        return try {
            val response = apiService.getElevatorInfoByName(stationName)

            handleApiResponse(response) // (로직 추출)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleApiResponse(response: Response<ElevatorStatus>): Result<ElevatorStatus> {
        if (response.isSuccessful && response.body() != null) {
            return Result.success(response.body()!!)
        }

        return Result.failure(IllegalAccessException("API Error: ${response.message()}"))
    }
}