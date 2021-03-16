package com.example.cloudstine.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {

    companion object {
        const val BASE_URL = "https://14-tage-wettervorhersage.de"
    }

    @GET("/wetter/hamburg/aktuell/{locationId}/")
    suspend fun getData(@Path("locationId") locationId: String): Response<ResponseBody>

}