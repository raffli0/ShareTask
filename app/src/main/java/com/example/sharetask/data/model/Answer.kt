package com.example.sharetask.data.model

data class Answer(
    val id: String = "",
    val questionId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
