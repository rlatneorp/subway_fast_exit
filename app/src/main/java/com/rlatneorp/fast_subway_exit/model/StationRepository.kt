package com.rlatneorp.fast_subway_exit.model

import android.content.Context
import com.rlatneorp.fast_subway_exit.model.network.ApiService
import com.rlatneorp.fast_subway_exit.model.network.RetrofitClient
import retrofit2.Response
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private data class StationCoords(val name: String, val lat: Double, val lon: Double)

class StationRepository(context: Context) {

    private val apiService: ApiService = RetrofitClient.instance
    private val locationService: LocationService = LocationService(context.applicationContext)

    private val API_KEY = "여기에_인증키를_붙여넣으세요"

    private val stationCoordsList = listOf(
        StationCoords("서울역", 37.554690, 126.972559),
        StationCoords("강남역", 37.497946, 127.027627),
        StationCoords("홍대입구역", 37.556846, 126.923769),
        StationCoords("시청역", 37.565709, 126.977098),
        StationCoords("잠실역", 37.513271, 127.100021),
        StationCoords("신설동역", 37.575300, 127.025175)
    )

    suspend fun getElevatorInfoForCurrentLocation(): Result<List<ElevatorRow>> {
        return try {
            val location = locationService.getCurrentLocation()

            val closestStationName = findClosestStation(location.latitude, location.longitude)

            searchElevatorInfoByName(closestStationName)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchElevatorInfoByName(stationName: String): Result<List<ElevatorRow>> {
        return try {
            val response = apiService.getSubwayElevatorInfo(API_KEY, stationName)

            handleApiResponse(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleApiResponse(response: Response<ElevatorApiResponse>): Result<List<ElevatorRow>> {
        if (response.isSuccessful && response.body() != null) {
            val elevatorList = response.body()!!.seoulMetroFaciInfo.row
            return Result.success(elevatorList)
        }

        return Result.failure(Exception("API Error: ${response.message()}"))
    }

    private fun findClosestStation(userLat: Double, userLon: Double): String {
        if (stationCoordsList.isEmpty()) return "서울역"

        return stationCoordsList.minByOrNull { station ->
            calculateDistance(userLat, userLon, station.lat, station.lon)
        }?.name ?: "서울역"
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }
}