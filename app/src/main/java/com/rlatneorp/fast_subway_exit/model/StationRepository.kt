package com.rlatneorp.fast_subway_exit.model

import android.content.Context
import com.rlatneorp.fast_subway_exit.BuildConfig
import com.rlatneorp.fast_subway_exit.model.network.ApiService
import com.rlatneorp.fast_subway_exit.model.network.KakaoApiService
import com.rlatneorp.fast_subway_exit.model.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.Response

private const val SUFFIX_STATION = "역"
private const val ERR_KAKAO_API_FAIL = "카카오 API 오류: "
private const val ERR_NO_SUBWAY_NEARBY = "주변 1km 내에 지하철역이 없습니다."
private const val ERR_NO_STATION_NAME = "역 이름 없음"
private const val ERR_NO_SEARCH_RESULT = "검색 결과가 없습니다."
private const val RANGE_1_START = 1
private const val RANGE_1_END = 1000
private const val RANGE_2_START = 1001
private const val RANGE_2_END = 2000
private const val RANGE_3_START = 2001
private const val RANGE_3_END = 3000

open class StationRepository(context: Context) {

    private val seoulApiService: ApiService = RetrofitClient.instance
    private val kakaoApiService: KakaoApiService = KakaoApiService.create()
    private val locationService: LocationService = LocationService(context.applicationContext)
    private val SEOUL_API_KEY = BuildConfig.SEOUL_API_KEY

    open suspend fun getElevatorInfoForCurrentLocation(): Result<List<ElevatorRow>> {
        return try {
            val stationName = findNearestStationName()
            searchElevatorInfoByName(stationName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun findNearestStationName(): String {
        val location = locationService.getCurrentLocation()
        val response = kakaoApiService.searchSubwayStationByCategory(
            longitude = location.longitude.toString(),
            latitude = location.latitude.toString()
        )
        return validateAndExtractStation(response)
    }

    private fun validateAndExtractStation(response: Response<KakaoApiResponse>): String {
        if (!response.isSuccessful) {
            throw Exception("$ERR_KAKAO_API_FAIL${response.code()}")
        }
        val documents = response.body()?.documents
        if (documents.isNullOrEmpty()) {
            throw Exception(ERR_NO_SUBWAY_NEARBY)
        }
        val placeName = documents[0].placeName ?: throw Exception(ERR_NO_STATION_NAME)
        return extractStationName(placeName)
    }

    private fun extractStationName(fullStationName: String): String {
        if (!fullStationName.contains(SUFFIX_STATION)) return fullStationName
        var name = fullStationName.substringBefore(SUFFIX_STATION) + SUFFIX_STATION
        if (name.contains("(")) {
            name = name.substringBefore("(")
        }
        return name.trim()
    }

    open suspend fun searchElevatorInfoByName(stationName: String): Result<List<ElevatorRow>> =
        withContext(Dispatchers.IO) {
            try {
                val query = prepareQuery(stationName)
                val allResults = fetchAllRanges(query)
                filterAndValidateList(allResults, query)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun prepareQuery(stationName: String): String {
        val query = stationName.trim().removeSuffix(SUFFIX_STATION)
        if (query.isBlank() && stationName.isNotBlank()) return stationName
        return query
    }

    private suspend fun fetchAllRanges(query: String): List<ElevatorRow> = withContext(Dispatchers.IO) {
        val d1 = async { fetchRange(RANGE_1_START, RANGE_1_END, query) }
        val d2 = async { fetchRange(RANGE_2_START, RANGE_2_END, query) }
        val d3 = async { fetchRange(RANGE_3_START, RANGE_3_END, query) }
        listOf(d1, d2, d3).awaitAll().flatten()
    }

    private suspend fun fetchRange(start: Int, end: Int, query: String): List<ElevatorRow> {
        return try {
            val response = seoulApiService.getSubwayElevatorInfoByName(SEOUL_API_KEY, start, end, query)
            val body = response.body()
            if (response.isSuccessful && body?.seoulMetroFaciInfo?.row != null) {
                return body.seoulMetroFaciInfo.row!!
            }
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun filterAndValidateList(allResults: List<ElevatorRow>, query: String): Result<List<ElevatorRow>> {
        val exactMatches = filterExactMatches(allResults, query)
        if (exactMatches.isNotEmpty()) return Result.success(exactMatches)
        val containsMatches = filterContainsMatches(allResults, query)
        if (containsMatches.isNotEmpty()) return Result.success(containsMatches)
        return Result.failure(Exception(ERR_NO_SEARCH_RESULT))
    }

    private fun filterExactMatches(list: List<ElevatorRow>, query: String): List<ElevatorRow> {
        return list.filter {
            val cleanName = it.stationName.replace(SUFFIX_STATION, "").substringBefore("(")
            cleanName == query
        }
    }

    private fun filterContainsMatches(list: List<ElevatorRow>, query: String): List<ElevatorRow> {
        return list.filter { it.stationName.contains(query) }
    }
}