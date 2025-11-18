package com.rlatneorp.fast_subway_exit.model

import com.google.gson.annotations.SerializedName

data class KakaoApiResponse(
    @SerializedName("documents")
    val documents: List<PlaceDocument>?,

    @SerializedName("meta")
    val meta: MetaData?
)

data class PlaceDocument(
    @SerializedName("place_name")
    val placeName: String?,

    @SerializedName("distance")
    val distance: String?,

    @SerializedName("category_group_code")
    val categoryGroupCode: String?
)

data class MetaData(
    @SerializedName("total_count")
    val totalCount: Int?
)