package com.example.cloudstine.api.model

import com.google.gson.annotations.SerializedName

data class PlanesResponseDto(
    @SerializedName("time")
    val time: Int,

    @SerializedName("states")
    val states: List<PlaneEntity>?
)