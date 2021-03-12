package com.example.CloudStine.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.CloudStine.R
import com.example.CloudStine.databinding.MainFragmentBinding
import com.google.android.material.snackbar.Snackbar
import java.lang.StringBuilder
import kotlin.math.roundToInt

class MainFragment : Fragment(R.layout.main_fragment) {

    private var showSnackBar = false

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            mainViewModelFactory = MainViewModelFactory()
            mainViewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)

            binding.swiperefreshMain.isRefreshing = true

            setObservers()
            setListener()
            mainViewModel.getData()
        }
    }

    private fun setObservers() {
        mainViewModel.status.observe(viewLifecycleOwner) { message -> showStatus(message) }

        mainViewModel.cloudOpacity.observe(viewLifecycleOwner) { opacity -> binding.cloudOpacityData.text = opacity }
        mainViewModel.cloudVisibility.observe(viewLifecycleOwner) { visibility -> binding.cloudVisibilityData.text = visibility }
        mainViewModel.cloudHeight.observe(viewLifecycleOwner) { height ->
            binding.cloudHeightDataMeter.text = height
            binding.cloudHeightDataFeet.text = convertHeight(height)
        }
    }

    private fun convertHeight(height: String): String {
        val noMHeight = (height.substring(0, height.indexOf("m") - 1))
        val germanWithoutFirst = noMHeight.replace(".", "")
        val englishComma = germanWithoutFirst.replace(",", ".")
        val floatHeight = englishComma.toFloat()
        val heightInFeet = floatHeight * 3.2808f
        val roundedHeight = heightInFeet.roundToInt()
        val feetString = roundedHeight.toString()
        val stringWithDot = StringBuilder(feetString).insert(feetString.length - 3, ".")
        val stringWithFt = StringBuilder(stringWithDot).append(" ft")
        return stringWithFt.toString()
    }

    private fun showStatus(message: String) {
        binding.swiperefreshMain.isRefreshing = false
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setListener() {
        binding.swiperefreshMain.setOnRefreshListener {
            showSnackBar = true
            mainViewModel.getData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

