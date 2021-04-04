package com.example.cloudstine.api

import com.example.cloudstine.api.model.PlanesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaneApiService {

    companion object {
        const val PLANE_BASE_URL = "https://opensky-network.org/api/"
    }

    @GET("states/all/")
    suspend fun getPlaneData(@Query("lamin") lamin: Float, @Query("lomin") lomin: Float, @Query("lamax") lamax: Float,@Query("lomax") lomax: Float): PlanesResponseDto
}
