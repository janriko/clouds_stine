package com.example.CloudStine.WebView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.CloudStine.R
import com.example.CloudStine.api.UserApiService
import com.example.CloudStine.databinding.MainFragmentBinding
import com.example.CloudStine.databinding.WebViewBinding

class WebViewFragment : Fragment(R.layout.web_view) {


    private var _binding: WebViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var webView: WebView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WebViewBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()

        binding.webview.let {
            it.settings.javaScriptEnabled = true
            it.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    view?.loadUrl(url?:UserApiService.BASE_URL)
                    return true
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
}