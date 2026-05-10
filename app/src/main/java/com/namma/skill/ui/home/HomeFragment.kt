package com.namma.skill.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.namma.skill.databinding.FragmentHomeBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(com.namma.skill.R.menu.home_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.namma.skill.R.id.action_notifications -> {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        "🔔 No new notifications for today.",
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        val detailAction: (com.namma.skill.data.model.Course) -> Unit = { course ->
            val intent = android.content.Intent(context, CourseDetailActivity::class.java).apply {
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

        adapter = CourseAdapter(
            onCourseClick = detailAction,
            onViewDetailsClick = detailAction,
            onApplyClick = { course ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val profile = viewModel.getUserProfile().first()
                    if (profile.education.isEmpty() || profile.phone.isEmpty() || profile.district.isEmpty()) {
                        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("📋 Complete Your Profile First")
                            .setMessage("Please fill in your Education, Phone, and District details so the skill center can contact you.")
                            .setPositiveButton("Go to Profile") { _, _ ->
                                (activity as? com.namma.skill.MainActivity)?.let { main ->
                                    main.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(com.namma.skill.R.id.nav_view).selectedItemId = com.namma.skill.R.id.navigation_profile
                                }
                            }
                            .setNegativeButton("Later", null)
                            .show()
                    } else {
                        val intent = android.content.Intent(context, CandidateSummaryActivity::class.java).apply {
                            putExtra("EXTRA_ID", course.id)
                            putExtra("EXTRA_TITLE", course.title)
                            putExtra("EXTRA_CENTER", course.centerName)
                        }
                        startActivity(intent)
                    }
                }
            },
            onInterestedClick = { course ->
                viewModel.requestCallback(course)
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "✅ A trainer from ${course.centerName} will call you soon!",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).setAction("OK") {}.show()
            }
        )
        binding.recyclerViewCourses.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCourses.adapter = adapter
    }

    private fun setupFilters() {
        binding.chipGroupTrade.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: -1)
            viewModel.setTradeFilter(chip?.text?.toString() ?: "All Trades")
        }

        binding.chipGroupDuration.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: -1)
            viewModel.setDurationFilter(chip?.text?.toString() ?: "All")
        }

        binding.switchJobGuarantee.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setJobGuaranteeFilter(isChecked)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredCourses.collect { courses ->
                adapter.submitList(courses)
            }
        }
        // ✅ Load personalized welcome banner with student's first name
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getUserProfile().collect { profile ->
                val firstName = profile.name.trim().split(" ").firstOrNull() ?: profile.name
                binding.textWelcomeGreeting.text =
                    getString(com.namma.skill.R.string.welcome_greeting, firstName)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
