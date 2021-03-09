package com.example.CloudStine.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.CloudStine.repositories.CloudRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val START_HEIGHT = "Wolkenhöhe"
    private val START_VISIBILITY = "Sichtweite"

    private val _cloudHeight = MutableLiveData<String>()
    val cloudHeight: LiveData<String> = _cloudHeight

    private val _cloudVisibility = MutableLiveData<String>()
    val cloudVisibility: LiveData<String> = _cloudVisibility

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    private val cloudRepository = CloudRepository()


    fun getData() {
        viewModelScope.launch {
            try {
                val allData = cloudRepository.getData().body()!!.string()

                val heightIndex = allData.indexOf(START_HEIGHT) + 32
                val visibilityIndex = allData.indexOf(START_VISIBILITY) + 31

                val heightEndIndex = allData.indexOf("<", heightIndex)
                val visibilityEndIndex = allData.indexOf("<", visibilityIndex)

                val cutHeightData = allData.substring(heightIndex, heightEndIndex)
                val cutVisibilityData = allData.substring(visibilityIndex, visibilityEndIndex)

                _cloudHeight.value = cutHeightData
                _cloudVisibility.value = cutVisibilityData
                _status.value = "Data was retrieved"
            } catch (exception: Exception) {
                _status.value = exception.message
            }
        }
    }
}