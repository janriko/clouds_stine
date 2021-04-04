package com.example.cloudstine.repositories

import com.example.cloudstine.api.PlaneApiService
import com.example.cloudstine.api.PlaneApiService.Companion.PLANE_BASE_URL
import com.example.cloudstine.api.model.PlanesListEntity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlaneRepository {

    private val retrofit = Retrofit.Builder()
        .baseUrl(PLANE_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient()
                .newBuilder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    private val retrofitService: PlaneApiService by lazy { retrofit.create(PlaneApiService::class.java) }

    suspend fun getData(lamin: Float, lomin: Float, lamax: Float, lomax: Float): PlanesListEntity? {
        val data = retrofitService.getPlaneData(lamin, lomin, lamax, lomax)
        return data.states?.let { data.toAppModel() }
    }

}