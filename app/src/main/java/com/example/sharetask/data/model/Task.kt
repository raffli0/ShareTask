package com.example.sharetask.data.model

data class Task(
    val id: String = "",
    val description: String = "",
    val uploadedBy: String = "",
    val uploadedByPhotoUrl: String = "",
    val imageUrl: String? = null,
    val subjectId: String = "",
    val subjectName: String = "",
    val subjectCode: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
