package com.example.sharetask.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", //beginer - professional
    val profilePic: String = "", //fetch dari acc google atau isi sendiri
    val rewardPoints: Int = 0 //berdasar banyaknya menjawab
)
