package com.example.cloudstine.WebView

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
import androidx.navigation.fragment.findNavController
import com.example.cloudstine.R
import com.example.cloudstine.api.UserApiService
import com.example.cloudstine.databinding.WebViewBinding
import com.example.cloudstine.main.MainFragmentDirections

class WebViewFragment : Fragment(R.layout.web_view) {

    var locationId: Int = 0
    var locationName: String = "-"

    private var _binding: WebViewBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()
        setupWebView()
    }

    private fun setupWebView() {
        binding.webview.let {
            it.settings.javaScriptEnabled = true
            //it.webChromeClient = mWebChromeCient
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
                        //Snackbar.make(binding.root, url, Snackbar.LENGTH_SHORT).show()
                        Log.i("jan", url)
                        it.loadUrl("javascript:getLocation();")
                    } else if (url.substring(30).contains("vorhersage",)) {
                        val subUrl = url.substring(30)
                        locationId = subUrl.substring(subUrl.indexOf("vorhersage") + 11, subUrl.length -1).toInt()
                        locationName = subUrl.substring(subUrl.indexOf("wetter") + 7, subUrl.indexOf("vorhersage") -1)
                        Log.i("jan", locationId.toString())
                        Log.i("jan", locationName?:"null")
                        it.loadUrl("https://14-tage-wettervorhersage.de/wetter/aktuell/$locationId")
                    } else {
                        view?.post {
                            findNavController().navigate(WebViewFragmentDirections.actionWebViewFragmentToMainFragment(locationId, locationName))
                        }

                    }
                    super.onPageFinished(view, url)
                }
            }

            it.loadUrl(UserApiService.BASE_URL)
        }
    }

    private fun setClickListeners() {
        binding.locationButton.setOnClickListener {
            binding.webview.loadUrl(
                //"https://www.google.com"
                "javascript:getLocation();"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}