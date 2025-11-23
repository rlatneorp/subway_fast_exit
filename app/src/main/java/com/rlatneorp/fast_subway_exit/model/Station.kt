package com.rlatneorp.fast_subway_exit.model

import com.google.gson.annotations.SerializedName

data class ElevatorApiResponse(
    @SerializedName("SeoulMetroFaciInfo")
    val seoulMetroFaciInfo: SeoulMetroFaciInfo?
)

data class SeoulMetroFaciInfo(
    @SerializedName("row")
    val row: List<ElevatorRow>?
)

data class ElevatorRow(
    @SerializedName("STN_NM")
    val stationName: String,

    @SerializedName("ELVTR_NM")
    val facilityName: String,

    @SerializedName("INSTL_PSTN")
    val location: String,

    @SerializedName("USE_YN")
    val runStatus: String
)

data class ElevatorUIModel(
    val location: String,
    val facilityName: String,
    val runStatus: String
)