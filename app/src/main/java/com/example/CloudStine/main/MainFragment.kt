package com.example.CloudStine.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.CloudStine.R
import com.example.CloudStine.databinding.MainFragmentBinding
import com.google.android.material.snackbar.Snackbar

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
        mainViewModel.cloudHeight.observe(viewLifecycleOwner) { height ->
            binding.cloudHeightDataMeter.text = height
//            val convertedHeight = convertHeight(height)
//            binding.cloudHeightDataFeet.text = (convertedHeight * 3.2808f).toString()
        }
        mainViewModel.cloudVisibility.observe(viewLifecycleOwner) { visibility -> binding.cloudVisibilityData.text = visibility}
        mainViewModel.status.observe(viewLifecycleOwner) { message ->
            binding.swiperefreshMain.isRefreshing = false
            showStatusSnackBar(message)
        }
    }

    private fun convertHeight(height: String): Float {
        val noMHeight = (height.substring(0, height.indexOf("m") - 1))
        val doublePoint = noMHeight.replace(",", ".")
        return doublePoint.replaceFirst(".", ",").toFloat()
    }

    private fun showStatusSnackBar(message: String) {
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

