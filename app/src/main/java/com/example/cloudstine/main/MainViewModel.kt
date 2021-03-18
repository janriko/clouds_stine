package com.example.cloudstine.main

import android.app.Activity
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudstine.MainActivity
import com.example.cloudstine.R
import com.example.cloudstine.repositories.CloudRepository
import kotlinx.coroutines.launch

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

    private val _cloudHeight = MutableLiveData<String>()
    val cloudHeight: LiveData<String> = _cloudHeight

    private val _cloudVisibility = MutableLiveData<String>()
    val cloudVisibility: LiveData<String> = _cloudVisibility

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    private val _locationId = MutableLiveData<Int>()

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
                _cloudHeight.value = getCutData(allData, START_HEIGHT, 32)
                _cloudVisibility.value = getCutData(allData, START_VISIBILITY, 31)
                _status.value = "Data was retrieved"
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
}