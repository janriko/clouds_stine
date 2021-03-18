package com.example.cloudstine.main

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cloudstine.MainActivity

class MainViewModelFactory(private val activity: Activity) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    return MainViewModel(activity) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
