package com.example.sharetask.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharetask.data.model.Subject
import com.example.sharetask.data.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UploadViewModel : ViewModel() {
    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> = _uploadState

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun uploadTask(description: String, imageUri: Uri? = null, subject: Subject? = null) {
        viewModelScope.launch {
            try {
                _uploadState.value = UploadState.Loading

                // Get current user
                val user = auth.currentUser
                if (user == null) {
                    _uploadState.value = UploadState.Error("User not authenticated")
                    return@launch
                }

                // Upload image if provided
                var imageUrl: String? = null
                if (imageUri != null) {
                    val imageRef = storage.reference.child("task_images/${UUID.randomUUID()}")
                    imageRef.putFile(imageUri).await()
                    imageUrl = imageRef.downloadUrl.await().toString()
                }

                // Create task document
                val question = Question(
                    id = UUID.randomUUID().toString(),
                    description = description,
                    uploadedBy = user.displayName ?: "Anonymous",
                    uploadedByPhotoUrl = user.photoUrl?.toString() ?: "",
                    imageUrl = imageUrl,
                    subjectId = subject?.id ?: "",
                    subjectName = subject?.name ?: "",
                    subjectCode = subject?.code ?: "",
                    timestamp = System.currentTimeMillis()
                )

                // Save to Firestore
                firestore.collection("question")
                    .document(question.id)
                    .set(question)
                    .await()

                _uploadState.value = UploadState.Success("Question posted successfully")
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