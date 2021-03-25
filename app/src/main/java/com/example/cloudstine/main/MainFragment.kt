package com.example.cloudstine.main

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cloudstine.R
import com.example.cloudstine.api.CloudApiService
import com.example.cloudstine.api.model.PlanesListEntity
import com.example.cloudstine.databinding.MainFragmentBinding
import com.example.cloudstine.main.RecyclerViewAdapter.PlanesListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_fragment.*
import java.util.*


class MainFragment : Fragment(R.layout.main_fragment) {

    private val REQUEST_LOCATION = 1

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isThisInternal = false
    private var second = false

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
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            binding.gpsRadio.isInvisible = false
            binding.gpsText.paintFlags = Paint.ANTI_ALIAS_FLAG
        }
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
        mainViewModel.cloudStatus.observe(viewLifecycleOwner) { message ->
            requestFinished()
            requestFinishedClouds(message)
        }
        mainViewModel.planeStatus.observe(viewLifecycleOwner) { message ->
            requestFinished()
            requestFinishedPlanes(message)
        }

        mainViewModel.cloudOpacity.observe(viewLifecycleOwner) { opacity -> binding.cloudOpacityData.text = opacity }
        mainViewModel.cloudVisibility.observe(viewLifecycleOwner) { visibility -> binding.cloudVisibilityData.text = visibility }
        mainViewModel.cloudHeightMeter.observe(viewLifecycleOwner) { heightMeter -> binding.cloudHeightDataMeter.text = heightMeter }
        mainViewModel.cloudHeightFeet.observe(viewLifecycleOwner) { heightFeet -> binding.cloudHeightDataFeet.text = heightFeet }
        mainViewModel.cloudHeightMax.observe(viewLifecycleOwner) { show -> binding.cloudHeightMax.isVisible = show }

        mainViewModel.planeList.observe(viewLifecycleOwner) { planes -> fillAdapter(planes) }

        mainViewModel.locationName.observe(viewLifecycleOwner) { name ->
            requireActivity().toolbar.title = "Wetterstation: " + name.capitalize(Locale.GERMANY)
        }
    }

    private fun fillAdapter(planes: PlanesListEntity?) {
        if (planes?.states != null) {
            binding.noPlanes.isVisible = false
            binding.planesRecycler.let {
                it.isVisible = true
                it.adapter = PlanesListAdapter(planes.states, mainViewModel.location.value!!)
                it.layoutManager = LinearLayoutManager(requireContext())
            }
        } else {
            binding.planesRecycler.isVisible = false
            binding.noPlanes.isVisible = true
        }
    }

    private fun setListener() {
        binding.swiperefreshMain.setOnRefreshListener {
            getDataFromLocation()
        mainViewModel.getPlaneData()}
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
                    view?.loadUrl(url ?: CloudApiService.CLOUDS_BASE_URL)
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String) {
                    super.onPageFinished(view, url)
                    binding.webview.isVisible = false
                    when {
                        url == ("https://14-tage-wettervorhersage.de/") -> getDataFromLocation()
                        url.substring(30).contains("vorhersage") -> {
                            val subUrl = url.substring(30)
                            val locationId = subUrl.substring(subUrl.indexOf("vorhersage") + 11, subUrl.length - 1).toInt()
                            val locationName = subUrl.substring(subUrl.indexOf("wetter") + 7, subUrl.indexOf("vorhersage") - 1)
                            mainViewModel.storeValues(locationId, locationName)

                            binding.swiperefreshMain.setColorSchemeColors(getColor(requireContext(), R.color.blue))
                            mainViewModel.getCloudData()
                        }
                        url.contains("captcha") -> {
                            binding.planesRecycler.isVisible = false
                            binding.cloudOpacityData.text = "-"
                            binding.cloudHeightDataFeet.text = "-"
                            binding.cloudHeightDataMeter.text = ""
                            binding.cloudVisibilityData.text = "-"
                            binding.webview.isVisible = true
                        }
                        else -> {
                            requestFinished("Server für Wetteranfrage reagiert nicht")
                        }
                    }
                }
            }
            it.loadUrl(CloudApiService.CLOUDS_BASE_URL)
        }
    }

    private fun requestFinished(message: String? = null) {
        if (second) {
            second = false
            binding.swiperefreshMain.let {
                it.isRefreshing = false
                it.setColorSchemeColors(Color.RED)
            }
        } else second = true
        message?.let { Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show() }
    }

    private fun requestFinishedClouds(message: String){
        if (message != "success") {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            binding.tempDataClouds.text = message
            binding.tempDataClouds.isVisible = true
        } else binding.tempDataClouds.isVisible = false
    }

    private fun requestFinishedPlanes(message: String){
        if (message != "success"){
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            binding.tempDataPlanes.text = message
            binding.tempDataPlanes.isVisible = true
        } else binding.tempDataPlanes.isVisible = false
    }

    private fun getDataFromLocation() {
        binding.swiperefreshMain.let {
            it.setColorSchemeColors(getColor(requireContext(), R.color.red))
            it.isRefreshing = true
        }
        if (sharedPref.getBoolean(MainViewModel.USE_HAMBURG, true)) {
            binding.swiperefreshMain.setColorSchemeColors(getColor(requireContext(), R.color.blue))
            mainViewModel.getCloudData()
        } else {
            binding.webview.loadUrl("javascript:getLocation();")
        }
    }

    private fun checkPermission() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    mainViewModel.storeNewLocation(location)
                    mainViewModel.getPlaneData()
                }
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.swiperefreshMain.let {
                    it.setColorSchemeColors(getColor(requireContext(), R.color.red))
                    it.isRefreshing = true
                }
                binding.gpsRadio.isInvisible = false
                binding.gpsText.paintFlags = Paint.ANTI_ALIAS_FLAG
                checkPermission()
                binding.webview.loadUrl("javascript:getLocation();")
            } else {
                sharedPref.edit().putBoolean(MainViewModel.USE_HAMBURG, true).apply()
                isThisInternal = true
                binding.radioGroup.check(R.id.home_radio)
                binding.gpsRadio.isInvisible = true
                binding.gpsText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                Snackbar.make(
                    binding.root,
                    "Ohne Standortberechtigungen ist nur die Abfrage der Gespeicherte Wetterstation möglich",
                    Snackbar.LENGTH_SHORT
                ).show()
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
