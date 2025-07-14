package com.example.sharetask.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val jurusan: String = "",
    val angkatan: String = "",
    val profilePic: String = "",
    val rewardPoints: Int = 0
)
