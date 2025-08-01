package com.example.sharetask.data.model

import com.google.firebase.firestore.Exclude

data class LatestView(
    val id: String = "",
    val questionId: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null,
    val viewedAt: Long = System.currentTimeMillis()
)