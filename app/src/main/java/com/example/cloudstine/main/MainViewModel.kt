package com.example.cloudstine.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudstine.repositories.CloudRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    companion object {
        private const val START_OPACITY = "Bewölkung"
        private const val START_HEIGHT = "Wolkenhöhe"
        private const val START_VISIBILITY = "Sichtweite"
    }

    private val _cloudOpacity = MutableLiveData<String>()
    val cloudOpacity: LiveData<String> = _cloudOpacity

    private val _cloudHeight = MutableLiveData<String>()
    val cloudHeight: LiveData<String> = _cloudHeight

    private val _cloudVisibility = MutableLiveData<String>()
    val cloudVisibility: LiveData<String> = _cloudVisibility

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    private val cloudRepository = CloudRepository()


    fun getData(locationId: Int, useHamburg: Boolean = false ) {
        viewModelScope.launch {
            try {
                val allData = if (useHamburg) {
                    cloudRepository.getData("178556").body()!!.string()
                } else {
                    cloudRepository.getData(locationId.toString()).body()!!.string()
                }

                val opacityIndex = allData.indexOf(START_OPACITY, ignoreCase = true) + 30
                val heightIndex = allData.indexOf(START_HEIGHT, ignoreCase = true) + 32
                val visibilityIndex = allData.indexOf(START_VISIBILITY, ignoreCase = true) + 31

                val opacityEndIndex = allData.indexOf("<", opacityIndex)
                val heightEndIndex = allData.indexOf("<", heightIndex)
                val visibilityEndIndex = allData.indexOf("<", visibilityIndex)

                val cutOpacityData = allData.substring(opacityIndex, opacityEndIndex)
                val cutHeightData = allData.substring(heightIndex, heightEndIndex)
                val cutVisibilityData = allData.substring(visibilityIndex, visibilityEndIndex)

                Log.i("jan", opacityIndex.toString())
                Log.i("jan", opacityEndIndex.toString())
                Log.i("jan", cutOpacityData)

                _cloudOpacity.value = cutOpacityData
                _cloudHeight.value = cutHeightData
                _cloudVisibility.value = cutVisibilityData
                _status.value = "Data was retrieved"
            } catch (exception: Exception) {
                _status.value = exception.message?:"error"
            }
        }
    }
}