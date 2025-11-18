package com.rlatneorp.fast_subway_exit.model.network

import com.rlatneorp.fast_subway_exit.model.KakaoApiResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.rlatneorp.fast_subway_exit.BuildConfig

interface KakaoApiService {

    @GET("v2/local/search/category.json")
    suspend fun searchSubwayStationByCategory(
        @Query("category_group_code") categoryCode: String = "SW8",
        @Query("x") longitude: String,
        @Query("y") latitude: String,
        @Query("radius") radius: Int = 1000
    ): retrofit2.Response<KakaoApiResponse>

    companion object {
        private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"

        // (수정) 하드코딩된 키 대신 BuildConfig에서 읽어온 키 사용
        private const val KAKAO_API_KEY = "KakaoAK ${BuildConfig.KAKAO_API_RAW_KEY}"

        fun create(): KakaoApiService {

            // (수정) Interceptor를 명시적인 object 구현으로 변경하여 빨간 줄 오류를 해결합니다.
            val authInterceptor = object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .header("Authorization", KAKAO_API_KEY)
                        .method(original.method, original.body)

                    val request = requestBuilder.build()
                    return chain.proceed(request)
                }
            }

            // Interceptor를 포함한 OkHttpClient 생성
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build()

            // OkHttpClient를 Retrofit 클라이언트에 연결
            return Retrofit.Builder()
                .baseUrl(KAKAO_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(KakaoApiService::class.java)
        }
    }
}