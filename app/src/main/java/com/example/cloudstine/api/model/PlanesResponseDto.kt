package com.example.cloudstine.api.model


import com.google.gson.annotations.SerializedName

data class PlanesResponseDtoNew(

    @SerializedName("time")
    val time: Int,

    @SerializedName("states")
    val states: List<List<Any>>
) {
    fun toAppModel() = PlanesListEntity(
        time,
        states.map { list -> (
                PlaneEntity(list[0].toString(),
                    list[1] as String,
                    list[2] as String,
                    list[3] as Int,
                    list[],
                    list[],
                    list[],
                    list[],
                    list[],
                    list[],
                    list[],
                    list[],
                    list[],
                    list[],
                    list[],
                ))})
}