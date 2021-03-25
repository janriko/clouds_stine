package com.example.cloudstine.repositories

import android.util.Log
import com.example.cloudstine.api.PlaneApiService
import com.example.cloudstine.api.PlaneApiService.Companion.PLANE_BASE_URL
import com.example.cloudstine.api.model.PlanesListEntity
import com.example.cloudstine.api.model.PlanesResponseDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.logging.LogRecord

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

    suspend fun getData(lamin: Float, lomin: Float, lamax: Float, lomax: Float): PlanesListEntity {
        val data = retrofitService.getPlaneData(lamin, lomin, lamax, lomax)
        Log.i("janCast", data.toString())
        val data2 = data.toAppModel()
        Log.i("janCast2", data2.toString())
        return data2
    }

}