package com.example.cloudstine.plane_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cloudstine.R
import com.example.cloudstine.databinding.MainFragmentBinding
import com.example.cloudstine.main.MainViewModel

class PlaneDetailFragment: Fragment(R.layout.plane_detail_fragment) {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!


    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = MainFragmentBinding.inflate(inflater, container, false)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        return binding.root
    }

}