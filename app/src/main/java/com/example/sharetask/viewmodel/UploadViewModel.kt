package com.example.sharetask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharetask.data.StorageService
import com.example.sharetask.data.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class UploadViewModel : ViewModel() {
    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> = _uploadState

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageService = StorageService()

    fun uploadDocument(file: File, mimeType: String, description: String) {
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

                // Upload document to Supabase
                val documentUploadResult = storageService.uploadDocument(file, mimeType)
                
                if (documentUploadResult.isSuccess) {
                    val documentUrl = documentUploadResult.getOrNull()
                    
                    // Create task document with document URL
                    val task = Task(
                        id = UUID.randomUUID().toString(),
                        description = description,
                        uploadedBy = user.displayName ?: "Anonymous",
                        uploadedByPhotoUrl = user.photoUrl?.toString() ?: "",
                        documentUrl = documentUrl
                    )

                    // Save to Firestore
                    firestore.collection("tasks")
                        .document(task.id)
                        .set(task)
                        .await()

                    _uploadState.value = UploadState.Success("Document uploaded successfully")
                } else {
                    _uploadState.value = UploadState.Error("Failed to upload document: ${documentUploadResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Upload failed: ${e.message}")
            }
        }
    }
}

sealed class UploadState {
    object Loading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}