package com.namma.skill.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namma.skill.data.model.Course
import com.namma.skill.data.model.UserProfile
import com.namma.skill.data.repository.SkillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = SkillRepository
    
    private val _tradeFilter = MutableStateFlow("All Trades")
    private val _durationFilter = MutableStateFlow("All") // "Short Term", "Long Term", "All"
    private val _jobGuaranteeFilter = MutableStateFlow(false)

    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    
    private val _filteredCourses = MutableStateFlow<List<Course>>(emptyList())
    val filteredCourses: StateFlow<List<Course>> = _filteredCourses

    init {
        viewModelScope.launch {
            repository.getCourses().collect {
                _allCourses.value = it
                applyFilters()
            }
        }
    }

    fun getUserProfile(): Flow<UserProfile> = repository.getUserProfile()

    fun requestCallback(course: Course) {
        repository.requestCallback(course)
    }

    fun setTradeFilter(trade: String) {
        _tradeFilter.value = trade
        applyFilters()
    }

    fun setDurationFilter(duration: String) {
        _durationFilter.value = duration
        applyFilters()
    }

    fun setJobGuaranteeFilter(isGuaranteedOnly: Boolean) {
        _jobGuaranteeFilter.value = isGuaranteedOnly
        applyFilters()
    }

    private fun applyFilters() {
        val trade = _tradeFilter.value
        val duration = _durationFilter.value
        val jobGuaranteeOnly = _jobGuaranteeFilter.value

        _filteredCourses.value = _allCourses.value.filter { course ->
            val courseTrade = course.trade.trim()
            val matchesTrade = if (trade == "All Trades" || trade.isBlank()) true 
                               else courseTrade.equals(trade.trim(), ignoreCase = true)
            val matchesDuration = when (duration) {
                "Short Term" -> course.isShortTerm
                "Long Term" -> !course.isShortTerm
                else -> true
            }
            val matchesJobGuarantee = if (jobGuaranteeOnly) course.hasJobGuarantee else true
            
            matchesTrade && matchesDuration && matchesJobGuarantee
        }
    }
}
