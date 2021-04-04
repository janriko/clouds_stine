package com.example.cloudstine.main

import android.app.Activity
import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudstine.api.model.PlanesListEntity
import com.example.cloudstine.repositories.CloudRepository
import com.example.cloudstine.repositories.PlaneRepository
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import kotlin.math.roundToInt

class MainViewModel(activity: Activity) : ViewModel() {

    companion object {
        /*
         * shared preferences
         */
        const val SHOW_INFO = "showInfo"
        const val USE_HAMBURG = "useHamburg"

        const val CLOUD_LOCATION_ID = "standardId"
        const val CLOUD_LOCATION_NAME = "standardName"

        const val PLANES_LOCATION_LAT = "LOCATION_LATITUDE"
        const val PLANES_LOCATION_LON = "LOCATION_LONGITUDE"
        const val PLANES_LOCATION_PROV = "LOCATION_PROVIDER"


        /*
         *   crawler constants
         */
        private const val START_OPACITY = "Bewölkung"
        private const val START_HEIGHT = "Wolkenhöhe"
        private const val START_VISIBILITY = "Sichtweite"

        /*
         * startup Hamburg data
         */

        const val LOCATION_ID_HAMBURG = 178556
        const val LOCATION_NAME_HAMBURG = "Hamburg"
        const val LOCATION_LAT_HAMBURG = 53.5511.toLong()
        const val LOCATION_LON_HAMBURG = 9.9937.toLong()
        const val LOCATION_PROV_HAMBURG = "fused"

        /*
         * other
         */
        private const val RADIUS = 1f //0.3f
    }

    private val sharedPreferences: SharedPreferences = activity.getSharedPreferences("main", 0)

    private val _cloudOpacity = MutableLiveData<String>()
    val cloudOpacity: LiveData<String> = _cloudOpacity

    private val _cloudHeightMeter = MutableLiveData<String>()
    val cloudHeightMeter: LiveData<String> = _cloudHeightMeter

    private val _cloudHeightFeet = MutableLiveData<String>()
    val cloudHeightFeet: LiveData<String> = _cloudHeightFeet

    private val _showCloudHeightMax = MutableLiveData<Boolean>()
    val cloudHeightMax: LiveData<Boolean> = _showCloudHeightMax

    private val _cloudVisibility = MutableLiveData<String>()
    val cloudVisibility: LiveData<String> = _cloudVisibility

    private val _cloudStatus = MutableLiveData<String>()
    val cloudStatus: LiveData<String> = _cloudStatus

    private val _planeList = MutableLiveData<PlanesListEntity>()
    val planeList: LiveData<PlanesListEntity> = _planeList

    private val _planeStatus = MutableLiveData<String>()
    val planeStatus: LiveData<String> = _planeStatus

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _gpsLocation = MutableLiveData<Location>()
    val gpsLocation: LiveData<Location> = _gpsLocation

    private val _locationId = MutableLiveData<Int>()
    val locationId: LiveData<Int> = _locationId

    private val _locationName = MutableLiveData<String>()
    val locationName: LiveData<String> = _locationName

    private val cloudRepository = CloudRepository()
    private val planeRepository = PlaneRepository()


    fun getCloudData() {
        viewModelScope.launch {
            try {
                val allData = cloudRepository.getData(_locationId.value.toString()).body()!!.string()
                if (allData.substring(0, 100).contains("Ungewöhnliche Zugriffe erkannt")) {
                    throw(Exception("captcha"))
                }

                _cloudOpacity.postValue(getCutData(allData, START_OPACITY, 30))
                _cloudHeightMeter.postValue(getCutData(allData, START_HEIGHT, 32))
                _cloudHeightFeet.postValue(convertHeightToFeet(getCutData(allData, START_HEIGHT, 32)))
                _cloudVisibility.postValue(getCutData(allData, START_VISIBILITY, 31))
                _cloudStatus.postValue("success")
            } catch (exception: Exception) {
                _cloudStatus.postValue(exception.message ?: "cloud error")
            }
        }
    }

    fun getPlaneData() {
        if (_location.value != null) {
            val locationSquare = getLocationSquare(_location.value!!.latitude.toFloat(), location.value!!.longitude.toFloat())
            viewModelScope.launch {
                try {
                    val unsortedList = planeRepository.getData(locationSquare[0], locationSquare[1], locationSquare[2], locationSquare[3])
                    if (unsortedList != null) {
                        _planeList.postValue(sortList(unsortedList))
                        _planeStatus.postValue("success")
                    } else {
                        _planeStatus.postValue("noPlanes")
                    }
                } catch (exception: Exception) {
                    _planeStatus.postValue(exception.message ?: "plane error")
                }
            }
        } else _planeStatus.postValue("GPS angeschaltet?")
    }

    private fun sortList(unsortedList: PlanesListEntity): PlanesListEntity {
        for (planeEntity in unsortedList.states) {
            val results = FloatArray(3)
            if (planeEntity.latitude != null && planeEntity.longitude != null && _location.value != null) {
                Location.distanceBetween(
                    planeEntity.latitude.toDouble(),
                    planeEntity.longitude.toDouble(),
                    _location.value!!.latitude,
                    _location.value!!.longitude,
                    results
                )
            }
            planeEntity.distance = results[0].roundToInt()
        }
        return PlanesListEntity(unsortedList.time, unsortedList.states.sortedBy { it.distance })
    }

    fun getCutData(allData: String, startString: String, offset: Int): String {
        val startIndex = allData.indexOf(startString, ignoreCase = true) + offset
        val endIndex = allData.indexOf("<", startIndex)
        return allData.substring(startIndex, endIndex)
    }

    fun storeCloudValues(id: Int, name: String) {
        _locationId.value = id
        _locationName.value = name
    }

    fun storeNewLocation(location: Location?) {
        location?.let { _location.value = it}
    }

    fun storeNewLocationWithGpsLoc() {
        _gpsLocation.value?.let { _location.value = it}
    }

    fun storeNewGPSLocation(location: Location?) {
        location?.let { _gpsLocation.value = it}
    }

    fun storeNewStandardValues() {
        sharedPreferences.edit().let { edit ->
            _locationId.value?.let { id -> edit.putInt(CLOUD_LOCATION_ID, id) } ?: kotlin.run { edit.remove(CLOUD_LOCATION_ID) }
            _locationName.value?.let { name -> edit.putString(CLOUD_LOCATION_NAME, name) } ?: kotlin.run { edit.remove(CLOUD_LOCATION_NAME) }
            _location.value?.let { location ->
                edit.putLong(PLANES_LOCATION_LAT, java.lang.Double.doubleToRawLongBits(location.latitude))
                edit.putLong(PLANES_LOCATION_LON, java.lang.Double.doubleToRawLongBits(location.longitude))
                edit.putString(PLANES_LOCATION_PROV, location.provider)
            } ?: kotlin.run{
                edit.remove(PLANES_LOCATION_LAT)
                edit.remove(PLANES_LOCATION_LON)
                edit.remove(PLANES_LOCATION_PROV)
            }
            edit.apply()
        }
    }

    fun cutUnit(height: String): Float {
        val noMHeight = (height.substring(0, height.indexOf("m") - 1))
        val germanWithoutFirst = noMHeight.replace(".", "")
        val englishComma = germanWithoutFirst.replace(",", ".")
        return englishComma.toFloat()
    }

    private fun convertHeightToFeet(height: String): String {
        val floatHeight = cutUnit(height)
        _showCloudHeightMax.value = floatHeight == 12192.0f
        val heightInFeet = floatHeight * 3.2808f
        val roundedHeight = heightInFeet.roundToInt()
        val feetString = roundedHeight.toString()
        val stringWithDot = when (feetString.length) {
            in 7..9 -> {
                StringBuilder(
                    StringBuilder(feetString).insert(feetString.length - 3, ".")
                ).insert(feetString.length - 6, ".")
            }
            in 4..6 -> StringBuilder(feetString).insert(feetString.length - 3, ".")
            else -> feetString

        }
        val stringWithFt = StringBuilder(stringWithDot).append(" ft")
        return stringWithFt.toString()
    }

    private fun getLocationSquare(latitude: Float, longitude: Float): Array<Float> {
        //ca 20km Radius -> 40 x 40 square (bisschen mehr)
        return arrayOf(latitude - RADIUS, longitude - RADIUS, latitude + RADIUS, longitude + RADIUS)
    }
}