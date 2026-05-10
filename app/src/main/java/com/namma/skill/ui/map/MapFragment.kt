package com.namma.skill.ui.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.namma.skill.data.repository.SkillRepository
import com.namma.skill.databinding.FragmentMapBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val repository = SkillRepository
    private lateinit var adapter: CenterAdapter

    private val allCenters = MutableStateFlow<List<com.namma.skill.data.model.Center>>(emptyList())
    private val districtFilter = MutableStateFlow("All Districts")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFilter()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = CenterAdapter(
            onCallClick = { center ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${center.contact}"))
                startActivity(intent)
            },
            onDirectionsClick = { center ->
                val gmmIntentUri = Uri.parse("google.navigation:q=${center.latitude},${center.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        )
        binding.recyclerViewCenters.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCenters.adapter = adapter
    }

    private fun setupFilter() {
        binding.chipGroupDistrict.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: -1)
            districtFilter.value = chip?.text?.toString() ?: "All Districts"
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getCenters().collect { centers ->
                allCenters.value = centers
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            combine(allCenters, districtFilter) { centers, filter ->
                if (filter == "All Districts") centers
                else centers.filter { it.district == filter }
            }.collect { filteredList ->
                adapter.submitList(filteredList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
