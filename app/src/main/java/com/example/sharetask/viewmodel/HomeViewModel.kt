package com.example.sharetask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sharetask.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HomeViewModel : ViewModel() {
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val firestore = FirebaseFirestore.getInstance()
    private var snapshotListener: ListenerRegistration? = null

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun loadTasks() {
        _isLoading.value = true
        _error.value = null

        try {
            // Remove previous listener if exists
            snapshotListener?.remove()

            // Set up real-time updates
            snapshotListener = firestore.collection("tasks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        android.util.Log.e("HomeViewModel", "Listen failed", e)
                        _error.value = e.message ?: "Failed to listen for updates"
                        _tasks.value = emptyList()
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        android.util.Log.d("HomeViewModel", "Got ${snapshot.documents.size} tasks")
                        val taskList = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Task::class.java)?.also { task ->
                                    android.util.Log.d("HomeViewModel", "Task parsed: $task")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("HomeViewModel", "Error parsing document ${doc.id}", e)
                                android.util.Log.d("HomeViewModel", "Document data: ${doc.data}")
                                null
                            }
                        }
                        _tasks.value = taskList
                    } else {
                        android.util.Log.d("HomeViewModel", "Got null snapshot")
                        _tasks.value = emptyList()
                    }
                    _isLoading.value = false
                }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Error setting up listener", e)
            _error.value = e.message ?: "Failed to set up updates"
            _tasks.value = emptyList()
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up listener when ViewModel is destroyed
        snapshotListener?.remove()
    }

    init {
        loadTasks()
    }
}
