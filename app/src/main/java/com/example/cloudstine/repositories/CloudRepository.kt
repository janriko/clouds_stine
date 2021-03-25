package com.example.cloudstine.repositories

import com.example.cloudstine.api.CloudApiService
import com.example.cloudstine.api.CloudApiService.Companion.CLOUDS_BASE_URL
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit

class CloudRepository {

    private val retrofit = Retrofit.Builder()
        .baseUrl(CLOUDS_BASE_URL)
        .build()

    private val retrofitServiceCloud: CloudApiService by lazy { retrofit.create(CloudApiService::class.java) }

    suspend fun getData(locationId: String): Response<ResponseBody> = retrofitServiceCloud.getCloudData(locationId)

}