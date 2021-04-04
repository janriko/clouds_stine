package com.example.cloudstine.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CloudApiService {

    companion object {
        const val CLOUDS_BASE_URL = "https://14-tage-wettervorhersage.de"
    }

    @GET("/wetter/aktuell/{locationId}/")
    suspend fun getCloudData(@Path("locationId") locationId: String): Response<ResponseBody>
}
