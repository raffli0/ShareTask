package com.example.sharetask.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Beginner", //beginer - professional
    val profilePic: String? = null, //fetch dari acc google atau isi sendiri
    val rewardPoints: Int = 0, //berdasar banyaknya menjawab
    val followingCount: Int = 0,
    val followersCount: Int = 0,
    val nim: String? = null,
    val isFriend: Boolean = false, // Menandai apakah user ini adalah teman
)
