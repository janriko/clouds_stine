package com.example.CloudStine.repositories

import com.example.CloudStine.api.UserApiService
import com.example.CloudStine.api.UserApiService.Companion.BASE_URL
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit

class CloudRepository {

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }

    suspend fun getData(): Response<ResponseBody> = retrofitService.getData()

}