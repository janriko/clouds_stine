package com.example.cloudstine.main.RecyclerViewAdapter

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudstine.api.model.PlaneEntity
import com.example.cloudstine.databinding.PlaneListItemBinding
import okhttp3.Cache.Companion.hasVaryAll

class PlanesListAdapter (private val data: List<PlaneEntity>, private val location: Location) : RecyclerView.Adapter<PlanesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PlaneListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position], location)

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(private val binding: PlaneListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(planeEntity: PlaneEntity, location: Location) {
            binding.icaoData.text = planeEntity.icao24
            binding.callsignData.text = planeEntity.callsign
            binding.distanceData.text = planeEntity.distance.toString().plus(" m")
/*
            Glide.with(itemView.context)
                .load(questionEntity.owner.profileImage)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                )
                .into(binding.listItemImage)

            binding.root.setOnClickListener { onItemClick(questionEntity.questionId, questionEntity.title) }
 */
        }
    }
}