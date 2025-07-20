package com.example.sharetask.data.model

data class Task(
    val id: String = "",
    val description: String = "",
    val uploadedBy: String = "",
    val uploadedByPhotoUrl: String = "",
    val documentUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
