package com.namma.skill.data.model

data class Course(
    val id: String = "",
    val title: String = "",
    val trade: String = "",
    val centerId: String = "",
    val centerName: String = "",
    val duration: String = "", // e.g. "3 Months"
    val isShortTerm: Boolean = true,
    val hasJobGuarantee: Boolean = false,
    val startDate: String = "",
    val eligibility: String = "",
    val description: String = "",
    val imageUrl: String = ""
)
