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

        private const val KAKAO_API_KEY = "KakaoAK ${BuildConfig.KAKAO_API_RAW_KEY}"

        fun create(): KakaoApiService {

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

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(KAKAO_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(KakaoApiService::class.java)
        }
    }
}