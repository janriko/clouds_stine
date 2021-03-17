package com.example.cloudstine.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cloudstine.R
import com.example.cloudstine.WebView.WebViewFragmentDirections
import com.example.cloudstine.api.UserApiService
import com.example.cloudstine.databinding.MainFragmentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_fragment.*
import java.lang.StringBuilder
import kotlin.math.roundToInt

class MainFragment : Fragment(R.layout.main_fragment) {

    var locationId: Int = 173609
    var locationName: String = "HH"

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

            setObservers()
            setListener()
            setupWebView()

            getDataFromLocation()
            //mainViewModel.getData(locationId, true)
        }
    }

    /* override fun onResume() {
         super.onResume()
         if (args.currentLocationId != 0) {
             mainViewModel.getData(args.currentLocationId, false)
         }
     } */

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
        binding.tempData.text = message
        //Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setListener() {
        binding.swiperefreshMain.setOnRefreshListener { getDataFromLocation() }
        binding.switchPosition.setOnCheckedChangeListener { view, isChecked ->
            useHamburg = !isChecked
            getDataFromLocation()
            //binding.root.setBackgroundColor(Color.LTGRAY)
        }
        binding.cloudOpacityData.setOnClickListener {
            val sb = Snackbar.make(binding.cloudVisibilityData, "VFD", 200)
            sb.setTextColor(Color.RED)
            sb.setBackgroundTint(Color.GRAY)
            sb.show()
        }
    }

    private fun getDataFromLocation() {
        //TODO: load baseUrl on start and after that only jscript to increase performance
        binding.swiperefreshMain.isRefreshing = true
        binding.webview.loadUrl(UserApiService.BASE_URL)
    }

    //TODO: do in ViewModel
    private fun setupWebView() {
        binding.webview.let {
            it.settings.javaScriptEnabled = true
            it.webChromeClient = object : WebChromeClient() {
                override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                    callback.invoke(origin, true, false)
                }
            }
            it.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    view?.loadUrl(url ?: UserApiService.BASE_URL)
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String) {
                    if (url == ("https://14-tage-wettervorhersage.de/")) {
                        Log.i("jan", url)
                        binding.swiperefreshMain.setColorSchemeColors(Color.RED)
                        it.loadUrl("javascript:getLocation();")
                    } else if (url.substring(30).contains("vorhersage")) {
                        val subUrl = url.substring(30)
                        locationId = subUrl.substring(subUrl.indexOf("vorhersage") + 11, subUrl.length - 1).toInt()
                        locationName = subUrl.substring(subUrl.indexOf("wetter") + 7, subUrl.indexOf("vorhersage") - 1)
                        Log.i("jan", locationId.toString())
                        Log.i("jan", locationName)

                        binding.swiperefreshMain.setColorSchemeColors(Color.BLUE)
                        mainViewModel.getData(locationId, useHamburg)
                    } else {
                        binding.swiperefreshMain.isRefreshing = false
                        Snackbar.make(binding.root, "getLocation() failed \n GPS turned on?", Snackbar.LENGTH_SHORT).show()
                    }
                    super.onPageFinished(view, url)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
