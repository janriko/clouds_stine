package com.example.cloudstine.api.model


import android.util.Log
import com.google.gson.annotations.SerializedName

data class PlanesResponseDto(

    @SerializedName("time")
    val time: Int,

    @SerializedName("states")
    val states: List<List<Any?>>?
) {
    fun toAppModel(): PlanesListEntity {
        return PlanesListEntity(
            time,
            states!!.map { list ->
                PlaneEntity(
                    list[0] as String,
                    list[1] as String,
                    list[2] as String,
                    list[3] as Double,
                    list[4] as Double,
                    list[5] as Double?,
                    list[6] as Double?,
                    list[7] as Double?,
                    list[8] as Boolean,
                    list[9] as Double?,
                    list[10] as Double?,
                    list[11] as Double?,
                    list[13] as Double?,
                    list[14] as String?,
                    list[15] as Boolean,
                    list[16] as Double,
                    null
                )
            }
        )
    }
}