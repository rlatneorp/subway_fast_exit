package com.rlatneorp.fast_subway_exit.model.network

import com.rlatneorp.fast_subway_exit.model.ElevatorApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("{key}/json/SeoulMetroFaciInfo/{startIndex}/{endIndex}/{stationName}")
    suspend fun getSubwayElevatorInfoByName(
        @Path("key") apiKey: String,
        @Path("startIndex") startIndex: Int,
        @Path("endIndex") endIndex: Int,
        @Path("stationName") stationName: String
    ): Response<ElevatorApiResponse>
}