package com.example.cloudstine.main

import android.app.Activity
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudstine.repositories.CloudRepository
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import kotlin.math.roundToInt

class MainViewModel(activity: Activity) : ViewModel() {

    companion object {
        const val SHOW_INFO = "showInfo"

        const val USE_HAMBURG = "useHamburg"
        const val STANDARD_ID = "standardId"
        const val STANDARD_NAME = "standardName"

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

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    private val _locationId = MutableLiveData<Int>()
    val locationId: LiveData<Int> = _locationId

    private val _locationName = MutableLiveData<String>()
    val locationName: LiveData<String> = _locationName

    private val cloudRepository = CloudRepository()


    fun getData() {
        viewModelScope.launch {
            try {
                var useId = _locationId.value
                if (sharedPreferences.getBoolean(USE_HAMBURG,true)){
                    useId = sharedPreferences.getInt(STANDARD_ID, 178556)
                    _locationName.value = sharedPreferences.getString(STANDARD_NAME, "Hamburg")
                }

                val allData = cloudRepository.getData( useId.toString()).body()!!.string()

                _cloudOpacity.value = getCutData(allData, START_OPACITY, 30)
                _cloudHeightMeter.value = getCutData(allData, START_HEIGHT, 32)
                _cloudHeightFeet.value = convertHeightToFeet(getCutData(allData, START_HEIGHT, 32))
                _cloudVisibility.value = getCutData(allData, START_VISIBILITY, 31)
                _status.value = "success"
            } catch (exception: Exception) {
                _status.value = exception.message?:"error"
            }
        }
    }

    fun getCutData(allData: String, startString: String, offset: Int): String{
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
            it.putInt(STANDARD_ID, _locationId.value?:178556)
            it.putString(STANDARD_NAME, _locationName.value?:"Hamburg")
            it.apply()
        }

    }

    fun cutUnit(height: String): Float{
        val noMHeight = (height.substring(0, height.indexOf("m") - 1))
        val germanWithoutFirst = noMHeight.replace(".", "")
        val englishComma = germanWithoutFirst.replace(",", ".")
        return englishComma.toFloat()
    }

    private fun convertHeightToFeet(height: String): String {
        val  floatHeight = cutUnit(height)
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
        val stringWithFt =  StringBuilder(stringWithDot).append(" ft")
        return stringWithFt.toString()
    }
}