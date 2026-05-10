package com.namma.skill.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.namma.skill.data.repository.SkillRepository
import com.namma.skill.databinding.FragmentProfileBinding
import com.namma.skill.ui.home.CourseDetailActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = SkillRepository
    private lateinit var adapter: AppliedCourseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeData()

        binding.fabEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), com.namma.skill.ui.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.imageProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // ✅ Update profile image URL in repository
            viewLifecycleOwner.lifecycleScope.launch {
                val currentProfile = repository.getUserProfile().first()
                repository.updateUserProfile(currentProfile.copy(imageUrl = it.toString()))
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AppliedCourseAdapter(
            onViewDetailsClick = { course ->
                val intent = Intent(requireContext(), CourseDetailActivity::class.java).apply {
                    putExtra("EXTRA_ID", course.id)
                    putExtra("EXTRA_TITLE", course.title)
                    putExtra("EXTRA_TRADE", course.trade)
                    putExtra("EXTRA_DURATION", course.duration)
                    putExtra("EXTRA_ELIGIBILITY", course.eligibility)
                    putExtra("EXTRA_DESCRIPTION", course.description)
                    putExtra("EXTRA_CENTER", course.centerName)
                    putExtra("EXTRA_CENTER_ID", course.centerId)
                    putExtra("EXTRA_IMAGE_URL", course.imageUrl)
                }
                startActivity(intent)
            }
        )
        binding.recyclerViewApplied.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewApplied.adapter = adapter
    }

    private fun observeData() {
        // ✅ repeatOnLifecycle(STARTED) re-subscribes every time the fragment
        //    becomes visible — including when returning from CandidateSummaryActivity.
        //    This fixes the bug where Applied count stayed "0" after applying.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    repository.getUserProfile().collect { profile ->
                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        val displayName = when {
                            profile.name.isNotBlank() -> profile.name
                            user?.email != null -> user.email?.substringBefore("@")
                            else -> "Skill Aspirant"
                        }
                        binding.textProfileName.text = displayName
                        binding.textProfileEmail.text = user?.email ?: "No email set"
                        binding.textProfileLocation.text = if (profile.district.isNotBlank()) 
                            "${profile.district}, Rural Youth" else "Rural Youth Aspirant"
                        binding.textProfileExperience.text = "${profile.experience.ifBlank { "0" }} Years Experience"

                        // Load Profile Image
                        if (profile.imageUrl.isNotBlank()) {
                            Glide.with(this@ProfileFragment)
                                .load(profile.imageUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .into(binding.imageProfile)
                        }

                        // Populate Interest Chips
                        binding.chipGroupInterests.removeAllViews()
                        profile.favoriteTrades.forEach { trade ->
                            val chip = com.google.android.material.chip.Chip(requireContext())
                            chip.text = trade
                            chip.isClickable = false
                            chip.isCheckable = false
                            binding.chipGroupInterests.addView(chip)
                        }
                    }
                }

                launch {
                    repository.getAppliedCourses().collect { appliedCourses ->
                        binding.textAppliedCount.text = appliedCourses.size.toString()
                        adapter.submitList(appliedCourses)
                        // Show/hide empty state
                        binding.cardEmptyState.visibility =
                            if (appliedCourses.isEmpty()) View.VISIBLE
                            else View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
