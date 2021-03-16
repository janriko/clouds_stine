package com.example.cloudstine.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cloudstine.R
import com.example.cloudstine.databinding.MainFragmentBinding
import kotlinx.android.synthetic.main.main_fragment.*
import java.lang.StringBuilder
import kotlin.math.roundToInt

class MainFragment : Fragment(R.layout.main_fragment) {

    private var showSnackBar = false
    private var useHamburg = false

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory

    val args: MainFragmentArgs by navArgs()

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
            mainViewModel.getData(173609, false)
        }
    }

    override fun onResume() {
        super.onResume()
        val tempString = args.currentLocationId.toString() + args.currentLocationName
        if (args.currentLocationId != 0) {
            mainViewModel.getData(args.currentLocationId, false)
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

    private fun showStatus(message: String) {
        binding.swiperefreshMain.isRefreshing = false
        //binding.tempData.text = message
        //Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setListener() {
        binding.swiperefreshMain.setOnRefreshListener {
            if (args.currentLocationId != 0) {
                showSnackBar = true
                mainViewModel.getData(args.currentLocationId, useHamburg)
            } else binding.swiperefreshMain.isRefreshing = false

        }
        binding.switchPosition.setOnCheckedChangeListener { view, isChecked ->
            useHamburg = !isChecked
            binding.tempData.append(useHamburg.toString())
            //TODO: update Data
            //binding.root.setBackgroundColor(Color.LTGRAY)
        }
        binding.cloudOpacityData.setOnClickListener {
            //TODO: don't show Webview
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToWebViewFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
