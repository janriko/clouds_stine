package com.example.cloudstine.api.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaneEntity(
    @SerializedName("icao24")
    val icao24: String,

    @SerializedName("callsign")
    val callsign: String,

    @SerializedName("origin_country")
    val origin_country: String,

    @SerializedName("time_position")
    val time_position: Double,

    @SerializedName("last_contact")
    val last_contact: Double,

    @SerializedName("longitude")
    val longitude: Double?,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("baro_altitude")
    val baro_altitude: Double?,

    @SerializedName("on_ground")
    val on_ground: Boolean,

    @SerializedName("velocity")
    val velocity: Double?,

    @SerializedName("true_track")
    val true_track: Double?,

    @SerializedName("vertical_rate")
    val vertical_rate: Double?,

    @SerializedName("geo_altitude")
    val geo_altitude: Double?,

    @SerializedName("squawk")
    val squawk: String?,

    @SerializedName("spi")
    val spi: Boolean,

    @SerializedName("position_source")
    val position_source: Double,

    var distance: Int? = null,
    var height_feet: String? = null
) : Parcelable