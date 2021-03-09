package com.example.CloudStine.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface UserApiService {

    companion object {
        const val BASE_URL = "https://14-tage-wettervorhersage.de"
    }

    @GET("/wetter/hamburg/aktuell/178556/")
    suspend fun getData(): Response<ResponseBody>

}