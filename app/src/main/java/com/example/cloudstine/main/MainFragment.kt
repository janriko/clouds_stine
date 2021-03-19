package com.example.cloudstine.main

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cloudstine.R
import com.example.cloudstine.api.UserApiService
import com.example.cloudstine.databinding.MainFragmentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_fragment.*
import java.util.*


class MainFragment : Fragment(R.layout.main_fragment) {

    private lateinit var sharedPref: SharedPreferences

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        sharedPref = requireActivity().getSharedPreferences("main", 0)
        binding.radioGroup.check(
            if (sharedPref.getBoolean(MainViewModel.USE_HAMBURG, true)) {
                R.id.home_radio
            } else {
                R.id.gps_radio
            }
        )
        binding.homeLocationText.text = sharedPref.getString(MainViewModel.STANDARD_NAME, "Hamburg")?.capitalize(Locale.GERMANY)
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
        mainViewModel.cloudHeightMeter.observe(viewLifecycleOwner) { heightMeter -> binding.cloudHeightDataMeter.text = heightMeter }
        mainViewModel.cloudHeightFeet.observe(viewLifecycleOwner) { heightFeet -> binding.cloudHeightDataFeet.text = heightFeet }
        mainViewModel.cloudHeightMax.observe(viewLifecycleOwner) { show -> binding.cloudHeightMax.isVisible = show }

        mainViewModel.locationName.observe(viewLifecycleOwner) { name -> requireActivity().toolbar.title = "Wetterstation: " + name.capitalize(Locale.GERMANY) }
    }

    private fun showStatus(message: String) {
        binding.swiperefreshMain.let {
            it.isRefreshing = false
            it.setColorSchemeColors(Color.RED)
        }
        if (message != "success") Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setListener() {
        binding.swiperefreshMain.setOnRefreshListener { getDataFromLocation() }
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            Log.i("jan", R.id.gps_radio.toString())
            sharedPref.edit().putBoolean(MainViewModel.USE_HAMBURG, checkedId == R.id.home_radio).apply()
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
                if (binding.radioGroup.checkedRadioButtonId == R.id.gps_radio && sharedPref.getString(MainViewModel.STANDARD_NAME, "null") != mainViewModel.locationName.value) {
                    mainViewModel.storeNewStandardValues()
                    binding.homeLocationText.text = mainViewModel.locationName.value?.capitalize(Locale.GERMANY)
                    binding.infoGroup.isVisible = false
                    sharedPref.edit().putBoolean(MainViewModel.SHOW_INFO, false).apply()
                } else Snackbar.make(binding.root, "Position bereits gespeichert", Snackbar.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true
        }
        binding.openWebButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://14-tage-wettervorhersage.de/wetter/aktuell/${mainViewModel.locationId.value.toString()}/"))) }
    }


    private fun getDataFromLocation() {
        binding.swiperefreshMain.let {
            it.setColorSchemeColors(getColor(requireContext(), R.color.red))
            it.isRefreshing = true
        }
        binding.webview.loadUrl("javascript:getLocation();")
    }

    private fun setupWebView() {
        binding.swiperefreshMain.let {
            it.setColorSchemeColors(getColor(requireContext(), R.color.green))
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
                            binding.swiperefreshMain.setColorSchemeColors(getColor(requireContext(), R.color.blue))
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
