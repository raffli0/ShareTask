package com.example.sharetask.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
) {
    // Konstruktor kosong untuk Firestore
    constructor() : this("", "", "", "", Timestamp.now(), false)
}