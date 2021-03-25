package com.example.cloudstine.main

import android.app.Activity
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudstine.api.model.PlanesListEntity
import com.example.cloudstine.repositories.CloudRepository
import com.example.cloudstine.repositories.PlaneRepository
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.*
import kotlin.math.roundToInt

class MainViewModel(activity: Activity) : ViewModel() {

    companion object {
        const val SHOW_INFO = "showInfo"

        const val USE_HAMBURG = "useHamburg"
        const val STANDARD_ID = "standardId"
        const val STANDARD_NAME = "standardName"

        private const val RADIUS = 1f //0.3f

        private const val START_OPACITY = "Bewölkung"
        private const val START_HEIGHT = "Wolkenhöhe"
        private const val START_VISIBILITY = "Sichtweite"
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

    private val _locationId = MutableLiveData<Int>()
    val locationId: LiveData<Int> = _locationId

    private val _locationName = MutableLiveData<String>()
    val locationName: LiveData<String> = _locationName

    private val cloudRepository = CloudRepository()
    private val planeRepository = PlaneRepository()


    fun getCloudData() {
        viewModelScope.launch {
            try {
                var useId = _locationId.value
                if (sharedPreferences.getBoolean(USE_HAMBURG, true)) {
                    useId = sharedPreferences.getInt(STANDARD_ID, 178556)
                    _locationName.value = sharedPreferences.getString(STANDARD_NAME, "Hamburg")
                }

                val allData = cloudRepository.getData(useId.toString()).body()!!.string()
                if (allData.substring(0, 100).contains("Ungewöhnliche Zugriffe erkannt")) {
                    throw(Exception("captcha"))
                }

                _cloudOpacity.value = getCutData(allData, START_OPACITY, 30)
                _cloudHeightMeter.value = getCutData(allData, START_HEIGHT, 32)
                _cloudHeightFeet.value = convertHeightToFeet(getCutData(allData, START_HEIGHT, 32))
                _cloudVisibility.value = getCutData(allData, START_VISIBILITY, 31)
                _cloudStatus.value = "success"
            } catch (exception: Exception) {
                _cloudStatus.value = exception.message ?: "cloud error"
            }
        }
    }

    fun getPlaneData() {
        if (_location.value != null) {
            val locationSquare = getLocationSquare(_location.value!!.latitude.toFloat(), location.value!!.longitude.toFloat())
            Log.i("jan", locationSquare.contentToString())
            viewModelScope.launch {
                try {
                    _planeList.value = planeRepository.getData(locationSquare[0], locationSquare[1], locationSquare[2], locationSquare[3])
                    _planeStatus.value = "success"
                } catch (exception: Exception) {
                    Log.i("janPlanes", exception.toString())
                    _planeStatus.value = exception.toString() ?: "plane error"
                }
            }
        } else _planeStatus.value = "GPS angeschaltet?"
    }

    fun getCutData(allData: String, startString: String, offset: Int): String {
        val startIndex = allData.indexOf(startString, ignoreCase = true) + offset
        val endIndex = allData.indexOf("<", startIndex)
        return allData.substring(startIndex, endIndex)
    }

    fun storeValues(id: Int, name: String) {
        _locationId.value = id
        _locationName.value = name
    }

    fun storeNewStandardValues() {
        sharedPreferences.edit().let {
            it.putInt(STANDARD_ID, _locationId.value ?: 178556)
            it.putString(STANDARD_NAME, _locationName.value ?: "Hamburg")
            it.apply()
        }
    }

    fun storeNewLocation(location: Location) {
        _location.value = location
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