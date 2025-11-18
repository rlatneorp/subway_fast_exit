package com.rlatneorp.fast_subway_exit.model

import com.google.gson.annotations.SerializedName

/**
 * 카카오맵 API (카테고리 검색) 응답을 담기 위한 데이터 클래스들
 */

// 1. 최상위 응답
data class KakaoApiResponse(
    @SerializedName("documents")
    val documents: List<PlaceDocument>, // 검색 결과 목록

    @SerializedName("meta")
    val meta: MetaData // 검색 메타 정보
)

// 2. "documents" 목록 내부의 각 장소 정보
data class PlaceDocument(
    @SerializedName("place_name")
    val placeName: String, // 예: "신설동역 1호선"

    @SerializedName("distance")
    val distance: String, // 중심좌표와의 거리 (미터)

    @SerializedName("category_group_code")
    val categoryGroupCode: String // "SW8" (지하철역)
)

// 3. "meta" 정보 (총 개수 등)
data class MetaData(
    @SerializedName("total_count")
    val totalCount: Int
)