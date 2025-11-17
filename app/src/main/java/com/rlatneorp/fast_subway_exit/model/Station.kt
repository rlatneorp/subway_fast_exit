package com.rlatneorp.fast_subway_exit.model

data class StationInfo(
    val stationName: String,
    val line: String,
    val address: String
)

data class ElevatorStatus(
    val stationName: String,
    val exitNumber: String,
    val status: String
)