package com.namma.skill.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.namma.skill.data.model.Center
import com.namma.skill.databinding.ItemCenterBinding

class CenterAdapter(
    private val onCallClick: (Center) -> Unit,
    private val onDirectionsClick: (Center) -> Unit
) : ListAdapter<Center, CenterAdapter.CenterViewHolder>(CenterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CenterViewHolder {
        val binding = ItemCenterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CenterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CenterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CenterViewHolder(private val binding: ItemCenterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(center: Center) {
            Glide.with(binding.imageCenter.context)
                .load(center.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(binding.imageCenter)
            binding.textCenterName.text = center.name
            binding.textCenterAddress.text = center.address
            binding.btnCall.setOnClickListener { onCallClick(center) }
            binding.btnDirections.setOnClickListener { onDirectionsClick(center) }
        }
    }


    class CenterDiffCallback : DiffUtil.ItemCallback<Center>() {
        override fun areItemsTheSame(oldItem: Center, newItem: Center): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Center, newItem: Center): Boolean = oldItem == newItem
    }
}
