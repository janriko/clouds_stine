package com.example.cloudstine.main.recycler_view_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudstine.api.model.PlaneEntity
import com.example.cloudstine.databinding.PlaneListItemBinding

class PlanesListAdapter (private val data: List<PlaneEntity>, val onItemClick: (PlaneEntity) -> Unit) : RecyclerView.Adapter<PlanesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PlaneListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position])

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(private val binding: PlaneListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(planeEntity: PlaneEntity) {
            binding.icaoData.text = planeEntity.icao24
            binding.callsignData.text = planeEntity.callsign
            planeEntity.height_feet?.let { binding.heightData.text = it }     //{ binding.heightData.text = it.toString().plus(" m") }
            planeEntity.distance?.let { binding.distanceData.text = it.toString().plus(" m") }

            binding.planeCard.setOnClickListener { onItemClick(planeEntity) }
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