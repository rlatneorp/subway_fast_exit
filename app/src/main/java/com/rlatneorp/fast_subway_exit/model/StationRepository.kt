package com.rlatneorp.fast_subway_exit.model

import android.content.Context
import com.rlatneorp.fast_subway_exit.BuildConfig
import com.rlatneorp.fast_subway_exit.model.network.ApiService
import com.rlatneorp.fast_subway_exit.model.network.KakaoApiService
import com.rlatneorp.fast_subway_exit.model.network.RetrofitClient
import retrofit2.Response

class StationRepository(context: Context) {

    private val seoulApiService: ApiService = RetrofitClient.instance
    private val kakaoApiService: KakaoApiService = KakaoApiService.create()
    private val locationService: LocationService = LocationService(context.applicationContext)
    private val SEOUL_API_KEY = BuildConfig.SEOUL_API_KEY

    suspend fun getElevatorInfoForCurrentLocation(): Result<List<ElevatorRow>> {
        return try {
            val location = locationService.getCurrentLocation()
            val longitude = location.longitude.toString()
            val latitude = location.latitude.toString()

            val kakaoResponse = kakaoApiService.searchSubwayStationByCategory(
                longitude = longitude,
                latitude = latitude
            )

            if (!kakaoResponse.isSuccessful || kakaoResponse.body() == null) {
                throw Exception("Kakao API Error: ${kakaoResponse.message()}")
            }

            val nearbyPlaces = kakaoResponse.body()!!.documents
            val closestStation = nearbyPlaces.firstOrNull()

            if (closestStation == null) {
                return Result.failure(Exception("주변에 지하철역을 찾을 수 없습니다."))
            }

            val stationName = extractStationName(closestStation.placeName)

            searchElevatorInfoByName(stationName)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractStationName(fullStationName: String): String {
        if (!fullStationName.contains("역")) {
            return fullStationName
        }

        var name = fullStationName.substringBefore("역") + "역"

        if (name.contains("(")) {
            name = name.substringBefore("(")
        }
        return name
    }

    suspend fun searchElevatorInfoByName(stationName: String): Result<List<ElevatorRow>> {
        return try {

            val query = stationName.trim().removeSuffix("역")

            val response = seoulApiService.getSubwayElevatorInfoByName(SEOUL_API_KEY, 1, 1000, query)

            handleSeoulApiResponse(response, query)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleSeoulApiResponse(
        response: Response<ElevatorApiResponse>,
        query: String
    ): Result<List<ElevatorRow>> {
        if (!response.isSuccessful || response.body() == null) {
            return Result.failure(Exception("API Error: ${response.message()}"))
        }

        val allList = response.body()!!.seoulMetroFaciInfo.row

        return filterAndValidateList(allList, query)
    }

    private fun filterAndValidateList(
        allList: List<ElevatorRow>,
        query: String
    ): Result<List<ElevatorRow>> {

        val filteredList = allList.filter { item ->
            item.stationName.contains(query)
        }

        if (filteredList.isNotEmpty()) {
            return Result.success(filteredList)
        }

        if (allList.isNotEmpty()) {
            return Result.failure(Exception("검색 결과가 없습니다."))
        }

        return Result.success(emptyList())
    }
}