package com.example.cloudstine.plane_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.cloudstine.R
import com.example.cloudstine.databinding.PlaneDetailFragmentBinding
import kotlin.math.roundToInt

class PlaneDetailFragment: Fragment(R.layout.plane_detail_fragment) {

    private var _binding: PlaneDetailFragmentBinding? = null
    private val binding get() = _binding!!
    //private lateinit var mainViewModel: PlaneDetailViewModel

    private val args: PlaneDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = PlaneDetailFragmentBinding.inflate(inflater, container, false)
        //mainViewModel = ViewModelProvider(this).get(PlaneDetailViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillList()
        //setOnClickListeners()
    }

    private fun fillList() {
        binding.icaoData.text = args.plane.icao24
        binding.callsignData.text = args.plane.callsign
        binding.distanceData.text = args.plane.distance.toString()
        binding.heightData.text = args.plane.height_feet
        binding.heightMeterData.text = args.plane.geo_altitude?.roundToInt().toString().plus(" m")
    }

    /* private fun setOnClickListeners() {
        binding.openFrButton.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("fr24.com/KLM96B${args.plane.callsign}/")
                )
            )
        }
    } */

}