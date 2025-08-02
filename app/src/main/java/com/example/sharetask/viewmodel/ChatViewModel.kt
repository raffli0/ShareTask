package com.example.sharetask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharetask.data.model.ChatMessage
import com.example.sharetask.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages
    
    private val _chatPartner = MutableLiveData<User?>()
    val chatPartner: LiveData<User?> = _chatPartner
    
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadChatPartner(userId: String) {
        _loading.value = true
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    _chatPartner.value = user
                } else {
                    _error.value = "User not found"
                }
                _loading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Error loading user: ${e.message}"
                _loading.value = false
            }
    }
    
    fun loadMessages(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Buat ID chat room yang unik berdasarkan ID kedua pengguna (diurutkan agar konsisten)
        val chatRoomId = if (currentUserId < otherUserId) {
            "${currentUserId}_${otherUserId}"
        } else {
            "${otherUserId}_${currentUserId}"
        }
        
        _loading.value = true
        db.collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Error loading messages: ${e.message}"
                    _loading.value = false
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        val message = doc.toObject(ChatMessage::class.java)
                        message?.copy(id = doc.id)
                    }
                    _messages.value = messageList
                }
                
                _loading.value = false
            }
    }
    
    fun sendMessage(receiverId: String, messageText: String) {
        if (messageText.isBlank()) return
        
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Buat ID chat room yang unik
        val chatRoomId = if (currentUserId < receiverId) {
            "${currentUserId}_${receiverId}"
        } else {
            "${receiverId}_${currentUserId}"
        }
        
        val message = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId,
            message = messageText
        )
        
        db.collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnFailureListener { e ->
                _error.value = "Failed to send message: ${e.message}"
            }
    }
    
    fun clearError() {
        _error.value = null
    }
}