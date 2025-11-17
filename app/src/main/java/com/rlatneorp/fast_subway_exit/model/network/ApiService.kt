package com.rlatneorp.fast_subway_exit.model.network

import com.rlatneorp.fast_subway_exit.model.ElevatorStatus
import com.rlatneorp.fast_subway_exit.model.StationInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/search/station")
    suspend fun searchStation(
        @Query("name") stationName: String
    ): Response<StationInfo>

    @GET("api/elevator/statusByLocation")
    suspend fun getElevatorStatusByLocation(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): Response<ElevatorStatus>

    @GET("api/elevator/statusByName")
    suspend fun getElevatorInfoByName(
        @Query("stationName") stationName: String
    ): Response<ElevatorStatus>
}