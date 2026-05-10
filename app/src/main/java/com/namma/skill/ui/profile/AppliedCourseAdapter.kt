package com.namma.skill.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.namma.skill.data.model.Course
import com.namma.skill.databinding.ItemAppliedCourseBinding

class AppliedCourseAdapter(
    private val onViewDetailsClick: (Course) -> Unit
) : ListAdapter<Course, AppliedCourseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppliedCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppliedCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) {
            binding.textAppliedCourseName.text = course.title
            binding.textAppliedCenter.text = course.centerName
            binding.textAppliedDuration.text = course.duration
            binding.textAppliedEligibility.text = course.eligibility
            
            // Set status (mock for now)
            binding.textAppliedStatus.text = "Applied"

            binding.textViewViewDetails.setOnClickListener {
                onViewDetailsClick(course)
            }
            binding.root.setOnClickListener {
                onViewDetailsClick(course)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean = oldItem == newItem
    }
}
