package com.namma.skill.data.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val education: String = "",
    val experience: String = "",
    val district: String = "",
    val imageUrl: String = "",
    val favoriteTrades: List<String> = emptyList()
)
