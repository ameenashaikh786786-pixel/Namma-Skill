package com.namma.skill.ui.success

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.namma.skill.data.model.SuccessStory
import com.namma.skill.databinding.ItemSuccessBinding

class SuccessAdapter : ListAdapter<SuccessStory, SuccessAdapter.SuccessViewHolder>(SuccessDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuccessViewHolder {
        val binding = ItemSuccessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SuccessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuccessViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SuccessViewHolder(private val binding: ItemSuccessBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: SuccessStory) {
            binding.textSuccessName.text = story.name
            binding.textSuccessQuote.text = "\"${story.quote}\""
            binding.textSuccessDetails.text = "Trained in: ${story.trade} | Now at: ${story.company}"
            binding.textLocation.text = story.location
            
            // Show play icon only if video URL exists
            binding.imagePlayIcon.visibility = if (story.videoUrl.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            
            Glide.with(binding.imageSuccess.context)
                .load(story.imageUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.imageSuccess)
        }
    }

    class SuccessDiffCallback : DiffUtil.ItemCallback<SuccessStory>() {
        override fun areItemsTheSame(oldItem: SuccessStory, newItem: SuccessStory): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SuccessStory, newItem: SuccessStory): Boolean = oldItem == newItem
    }
}
