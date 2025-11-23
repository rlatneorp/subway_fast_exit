package com.rlatneorp.fast_subway_exit.model

import android.content.Context
import android.util.Log
import com.rlatneorp.fast_subway_exit.BuildConfig
import com.rlatneorp.fast_subway_exit.model.network.ApiService
import com.rlatneorp.fast_subway_exit.model.network.KakaoApiService
import com.rlatneorp.fast_subway_exit.model.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.Response

// --- Constants (하드코딩 제거) ---
private const val TAG = "StationRepository"
private const val SUFFIX_STATION = "역"

private const val ERR_KAKAO_API_FAIL = "카카오 API 오류: "
private const val ERR_NO_SUBWAY_NEARBY = "주변 1km 내에 지하철역이 없습니다."
private const val ERR_NO_STATION_NAME = "역 이름 없음"
private const val ERR_SEOUL_API_HTTP = "API HTTP Error: "
private const val ERR_NO_SEARCH_RESULT = "검색 결과가 없습니다."

private const val LOG_GPS = "GPS: "
private const val LOG_KAKAO_FAIL = "Kakao Fail: "
private const val LOG_KAKAO_FOUND = "Kakao Found: "
private const val LOG_EXTRACTED_NAME = "Extracted Name: "
private const val LOG_SEOUL_QUERY = "Seoul API Query: "
private const val LOG_SEOUL_EXCEPTION = "Seoul API Exception: "
private const val LOG_SEOUL_NO_DATA = "Seoul API: Body or Info is null (No Data)"
private const val LOG_SEOUL_EMPTY_LIST = "Seoul API: Row list is empty"
private const val LOG_SEOUL_RAW_COUNT = "Seoul API Raw Count: "
private const val LOG_FILTERED_COUNT = "Filtered Count: "
private const val LOG_FILTER_MISMATCH = "Filtered result is empty. Query: "
private const val LOG_SEARCH_ERROR = "Search Error: "
private const val LOG_FETCH_RANGE_ERROR = "Fetch Range Error "

// 검색 범위 설정 (전수 조사)
private const val RANGE_1_START = 1
private const val RANGE_1_END = 1000
private const val RANGE_2_START = 1001
private const val RANGE_2_END = 2000
private const val RANGE_3_START = 2001
private const val RANGE_3_END = 3000

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

            Log.d(TAG, "$LOG_GPS$latitude, $longitude")

            val kakaoResponse = kakaoApiService.searchSubwayStationByCategory(
                longitude = longitude,
                latitude = latitude
            )

            if (!kakaoResponse.isSuccessful) {
                Log.e(TAG, "$LOG_KAKAO_FAIL${kakaoResponse.code()}")
                throw Exception("$ERR_KAKAO_API_FAIL${kakaoResponse.code()}")
            }

            val documents = kakaoResponse.body()?.documents
            if (documents.isNullOrEmpty()) {
                return Result.failure(Exception(ERR_NO_SUBWAY_NEARBY))
            }
            val closestStation = documents[0]
            val placeName = closestStation.placeName ?: return Result.failure(Exception(ERR_NO_STATION_NAME))
            Log.d(TAG, "$LOG_KAKAO_FOUND$placeName")
            val stationName = extractStationName(placeName)
            Log.d(TAG, "$LOG_EXTRACTED_NAME$stationName")
            searchElevatorInfoByName(stationName)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }

    private fun extractStationName(fullStationName: String): String {
        if (!fullStationName.contains(SUFFIX_STATION)) return fullStationName
        var name = fullStationName.substringBefore(SUFFIX_STATION) + SUFFIX_STATION
        if (name.contains("(")) {
            name = name.substringBefore("(")
        }
        return name.trim()
    }

    suspend fun searchElevatorInfoByName(stationName: String): Result<List<ElevatorRow>> = withContext(Dispatchers.IO) {
        try {
            val query = stationName.trim().replace(SUFFIX_STATION, "")
            Log.d(TAG, "$LOG_SEOUL_QUERY$query")
            val deferred1 = async { fetchRange(RANGE_1_START, RANGE_1_END, query) }
            val deferred2 = async { fetchRange(RANGE_2_START, RANGE_2_END, query) }
            val deferred3 = async { fetchRange(RANGE_3_START, RANGE_3_END, query) }
            val allResults = listOf(deferred1, deferred2, deferred3).awaitAll().flatten()
            filterAndValidateList(allResults, query)
        } catch (e: Exception) {
            Log.e(TAG, "$LOG_SEARCH_ERROR${e.message}")
            Result.failure(e)
        }
    }

    private fun filterAndValidateList(allResults: List<ElevatorRow>, query: String): Result<List<ElevatorRow>> {
        val filteredList = allResults.filter { item ->
            item.stationName.contains(query)
        }
        Log.d(TAG, "$LOG_FILTERED_COUNT${filteredList.size}")
        if (filteredList.isNotEmpty()) {
            return Result.success(filteredList)
        }
        return Result.failure(Exception(ERR_NO_SEARCH_RESULT))
    }

    private suspend fun fetchRange(start: Int, end: Int, query: String): List<ElevatorRow> {
        return try {
            val response = seoulApiService.getSubwayElevatorInfoByName(SEOUL_API_KEY, start, end, query)

            if (response.isSuccessful && response.body()?.seoulMetroFaciInfo?.row != null) {
                response.body()!!.seoulMetroFaciInfo!!.row!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "$LOG_FETCH_RANGE_ERROR($start-$end): ${e.message}")
            emptyList()
        }
    }
}