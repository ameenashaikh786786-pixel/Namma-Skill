package com.namma.skill.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.namma.skill.data.model.Course
import com.namma.skill.databinding.ItemCourseBinding

class CourseAdapter(
    private val onCourseClick: (Course) -> Unit,
    private val onApplyClick: (Course) -> Unit,
    private val onInterestedClick: (Course) -> Unit,
    private val onViewDetailsClick: (Course) -> Unit
) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CourseViewHolder(private val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) {
            binding.textCourseTitle.text = course.title
            binding.textCenterName.text = course.centerName
            binding.textEligibility.text = "Eligibility: ${course.eligibility}"
            binding.chipTrade.text = course.trade
            binding.chipDuration.text = course.duration
            
            binding.textJobGuarantee.visibility = if (course.hasJobGuarantee) View.VISIBLE else View.GONE
            
            binding.root.setOnClickListener { onCourseClick(course) }
            binding.btnViewDetails.setOnClickListener { onViewDetailsClick(course) }
            binding.btnApply.setOnClickListener { onApplyClick(course) }
            binding.btnInterested.setOnClickListener { onInterestedClick(course) }
        }
    }


    class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean = oldItem == newItem
    }
}
