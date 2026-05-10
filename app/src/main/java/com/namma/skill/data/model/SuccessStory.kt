package com.namma.skill.data.model

data class SuccessStory(
    val id: String = "",
    val name: String = "",
    val trade: String = "",
    val quote: String = "",
    val company: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val isFeatured: Boolean = false
)
