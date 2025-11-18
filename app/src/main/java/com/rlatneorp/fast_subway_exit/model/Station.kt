package com.rlatneorp.fast_subway_exit.model

import com.google.gson.annotations.SerializedName

data class ElevatorApiResponse(
    @SerializedName("SeoulMetroFaciInfo")
    val seoulMetroFaciInfo: SeoulMetroFaciInfo
)

data class SeoulMetroFaciInfo(
    @SerializedName("row")
    val row: List<ElevatorRow>
)

data class ElevatorRow(
    @SerializedName("STN_CD")
    val stationCode: String,

    @SerializedName("STN_NM")
    val stationName: String,

    @SerializedName("ELVTR_NM")
    val facilityName: String,

    @SerializedName("OPR_SEC")
    val operationSection: String,

    @SerializedName("INSTL_PSTN")
    val location: String,

    @SerializedName("USE_YN")
    val runStatus: String,

    @SerializedName("ELVTR_SE")
    val facilityType: String
)