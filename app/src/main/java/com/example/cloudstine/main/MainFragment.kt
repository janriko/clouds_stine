package com.example.cloudstine.main

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat.checkSelfPermission
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
import java.util.jar.Manifest


class MainFragment : Fragment(R.layout.main_fragment) {

    private val REQUEST_LOCATION = 1

    private lateinit var sharedPref: SharedPreferences

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory

    private var isThisInternal = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setupBindingAndShardPref(inflater, container)
        setUpInitialViews()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            setupViewModel()

            setObservers()
            setListener()
            setupWebView()
    }

    private fun setupBindingAndShardPref(inflater: LayoutInflater, container: ViewGroup?) {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        sharedPref = requireActivity().getSharedPreferences("main", 0)
    }

    private fun setUpInitialViews() {
        isThisInternal = true
        binding.radioGroup.check(
            if (sharedPref.getBoolean(MainViewModel.USE_HAMBURG, true)) {
                R.id.home_radio
            } else {
                R.id.gps_radio
            }
        )
        isThisInternal = false
        binding.homeLocationText.text = sharedPref.getString(MainViewModel.STANDARD_NAME, "Hamburg")?.capitalize(Locale.GERMANY)
        binding.infoGroup.isVisible = sharedPref.getBoolean(MainViewModel.SHOW_INFO, true)
    }

    private fun setupViewModel() {
        mainViewModelFactory = MainViewModelFactory(requireActivity())
        mainViewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)
    }

    private fun setObservers() {
        mainViewModel.status.observe(viewLifecycleOwner) { message -> requestFinished(message) }

        mainViewModel.cloudOpacity.observe(viewLifecycleOwner) { opacity -> binding.cloudOpacityData.text = opacity }
        mainViewModel.cloudVisibility.observe(viewLifecycleOwner) { visibility -> binding.cloudVisibilityData.text = visibility }
        mainViewModel.cloudHeightMeter.observe(viewLifecycleOwner) { heightMeter -> binding.cloudHeightDataMeter.text = heightMeter }
        mainViewModel.cloudHeightFeet.observe(viewLifecycleOwner) { heightFeet -> binding.cloudHeightDataFeet.text = heightFeet }
        mainViewModel.cloudHeightMax.observe(viewLifecycleOwner) { show -> binding.cloudHeightMax.isVisible = show }

        mainViewModel.locationName.observe(viewLifecycleOwner) { name ->
            requireActivity().toolbar.title = "Wetterstation: " + name.capitalize(Locale.GERMANY)
        }
    }

    private fun setListener() {
        binding.swiperefreshMain.setOnRefreshListener { getDataFromLocation() }
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (isThisInternal) {
                isThisInternal = false
            } else {
                sharedPref.edit().putBoolean(MainViewModel.USE_HAMBURG, checkedId == R.id.home_radio).apply()
                getDataFromLocation()
            }
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
                if (binding.radioGroup.checkedRadioButtonId == R.id.gps_radio && sharedPref.getString(
                        MainViewModel.STANDARD_NAME,
                        "null"
                    ) != mainViewModel.locationName.value
                ) {
                    mainViewModel.storeNewStandardValues()
                    binding.homeLocationText.text = mainViewModel.locationName.value?.capitalize(Locale.GERMANY)
                    binding.infoGroup.isVisible = false
                    sharedPref.edit().putBoolean(MainViewModel.SHOW_INFO, false).apply()
                } else Snackbar.make(binding.root, "Position bereits gespeichert", Snackbar.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true
        }
        binding.openWebButton.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://14-tage-wettervorhersage.de/wetter/aktuell/${mainViewModel.locationId.value.toString()}/")
                )
            )
        }
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

                            binding.swiperefreshMain.setColorSchemeColors(getColor(requireContext(), R.color.blue))
                            mainViewModel.getData()
                        }
                        else -> requestFinished("getLocation() failed \n GPS turned on?")
                    }
                }
            }
            it.loadUrl(UserApiService.BASE_URL)
        }
    }

    private fun requestFinished(message: String) {
        binding.swiperefreshMain.let {
            it.isRefreshing = false
            it.setColorSchemeColors(Color.RED)
        }
        if (message != "success") Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun getDataFromLocation() {
        binding.swiperefreshMain.let {
            it.setColorSchemeColors(getColor(requireContext(), R.color.red))
            it.isRefreshing = true
        }

        if (sharedPref.getBoolean(MainViewModel.USE_HAMBURG, true)){
            binding.swiperefreshMain.setColorSchemeColors(getColor(requireContext(), R.color.blue))
            mainViewModel.getData()
        } else {
            if (checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                binding.webview.loadUrl("javascript:getLocation();")
            } else {
                requestFinished("success")
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                binding.swiperefreshMain.let {
                    it.setColorSchemeColors(getColor(requireContext(), R.color.red))
                    it.isRefreshing = true
                }
                binding.webview.loadUrl("javascript:getLocation();")
            } else {
                sharedPref.edit().putBoolean(MainViewModel.USE_HAMBURG, true).apply()
                isThisInternal = true
                binding.radioGroup.check(R.id.home_radio)
                Snackbar.make(binding.root, "Ohne Standortberechtigungen ist nur die Abfrage der Gespeicherte Wetterstation möglich", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
