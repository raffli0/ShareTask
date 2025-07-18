package com.example.sharetask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharetask.data.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UploadViewModel : ViewModel() {
    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> = _uploadState

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun uploadTask(description: String) {
        viewModelScope.launch {
            try {
                _uploadState.value = UploadState.Loading

                if (description.isBlank()) {
                    _uploadState.value = UploadState.Error("Please enter a description")
                    return@launch
                }

                // Get current user
                val user = auth.currentUser
                if (user == null) {
                    _uploadState.value = UploadState.Error("User not authenticated")
                    return@launch
                }

                // Create task document
                val task = Task(
                    id = UUID.randomUUID().toString(),
                    description = description,
                    uploadedBy = user.displayName ?: "Anonymous",
                    uploadedByPhotoUrl = user.photoUrl?.toString() ?: "",
                )

                // Save to Firestore
                firestore.collection("tasks")
                    .document(task.id)
                    .set(task)
                    .await()

                _uploadState.value = UploadState.Success("Task uploaded successfully")
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Upload failed")
            }
        }
    }
}

sealed class UploadState {
    object Loading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}