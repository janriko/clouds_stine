package com.example.cloudstine.main

import android.content.SharedPreferences
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cloudstine.R
import com.example.cloudstine.api.UserApiService
import com.example.cloudstine.databinding.MainFragmentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_fragment.*
import java.lang.StringBuilder
import java.util.*
import kotlin.math.roundToInt

class MainFragment : Fragment(R.layout.main_fragment) {

    private lateinit var sharedPref: SharedPreferences

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        sharedPref = requireActivity().getSharedPreferences("main", 0)
        binding.switchPosition.isChecked = !sharedPref.getBoolean(MainViewModel.USE_HAMBURG, true)
        binding.homeLocationText.text = sharedPref.getString(MainViewModel.STANDARD_NAME, "-")
        binding.infoGroup.isVisible = sharedPref.getBoolean(MainViewModel.SHOW_INFO, true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            mainViewModelFactory = MainViewModelFactory(requireActivity())
            mainViewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)

            setObservers()
            setListener()
            setupWebView()
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

        mainViewModel.locationName.observe(viewLifecycleOwner) { name ->
            requireActivity().toolbar.title = "Wetterstation: " + name.capitalize(Locale.GERMANY)
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
        binding.swiperefreshMain.setColorSchemeColors(Color.BLACK)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setListener() {
        binding.swiperefreshMain.setOnRefreshListener { getDataFromLocation() }
        binding.switchPosition.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean(MainViewModel.USE_HAMBURG, !isChecked).apply()
            getDataFromLocation()
            //binding.root.setBackgroundColor(Color.LTGRAY)
        }
        binding.cloudOpacityData.setOnClickListener {
            Snackbar.make(binding.cloudVisibilityData, "Markus ist cool", 200).let {
                it.setTextColor(Color.LTGRAY)
                it.setBackgroundTint(Color.WHITE)
                it.show()
            }
        }
        binding.homeLayout.setOnLongClickListener {
            if (!swiperefreshMain.isRefreshing) {
                if (binding.switchPosition.isChecked && sharedPref.getString(MainViewModel.STANDARD_NAME, "null") != mainViewModel.locationName.value) {
                    mainViewModel.storeNewStandardValues()
                    binding.homeLocationText.text = mainViewModel.locationName.value?.capitalize(Locale.GERMANY)
                    binding.infoGroup.isVisible = false
                    sharedPref.edit().putBoolean(MainViewModel.SHOW_INFO, false).apply()
                } else Snackbar.make(binding.root, "Position bereits gespeichert", Snackbar.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true
        }
    }


    private fun getDataFromLocation() {
        binding.swiperefreshMain.let {
            it.setColorSchemeColors(Color.RED)
            it.isRefreshing = true
        }
        binding.webview.loadUrl("javascript:getLocation();")
    }

    private fun setupWebView() {
        binding.swiperefreshMain.let {
            it.setColorSchemeColors(Color.YELLOW)
            it.isRefreshing = true
        }
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
                    super.onPageFinished(view, url)
                    when {
                        url == ("https://14-tage-wettervorhersage.de/") -> getDataFromLocation()
                        url.substring(30).contains("vorhersage") -> {
                            val subUrl = url.substring(30)
                            val locationId = subUrl.substring(subUrl.indexOf("vorhersage") + 11, subUrl.length - 1).toInt()
                            val locationName = subUrl.substring(subUrl.indexOf("wetter") + 7, subUrl.indexOf("vorhersage") - 1)
                            mainViewModel.storeValues(locationId, locationName)

                            Log.i("jan", subUrl)
                            binding.swiperefreshMain.setColorSchemeColors(Color.BLUE)
                            mainViewModel.getData()
                        }
                        else -> showStatus("getLocation() failed \n GPS turned on?")
                    }
                }
            }
            it.loadUrl(UserApiService.BASE_URL)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
