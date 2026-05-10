package com.namma.skill.ui.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.namma.skill.databinding.FragmentSuccessBinding

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.namma.skill.data.repository.SkillRepository
import kotlinx.coroutines.launch

class SuccessFragment : Fragment() {
    private var _binding: FragmentSuccessBinding? = null
    private val binding get() = _binding!!
    private val repository = SkillRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = SuccessAdapter()
        binding.recyclerViewSuccess.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewSuccess.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repository.getSuccessStories().collect { stories ->
                adapter.submitList(stories)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
