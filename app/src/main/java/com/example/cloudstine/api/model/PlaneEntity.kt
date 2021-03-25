package com.example.cloudstine.api.model


import com.google.gson.annotations.SerializedName

data class PlaneEntity(
    @SerializedName("states")
    val states: List<List<Any>>,
    @SerializedName("time")
    val time: Int
)