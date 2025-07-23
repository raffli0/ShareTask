package com.example.sharetask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharetask.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommunityViewModel : ViewModel() {
    private val _questions = MutableLiveData<List<Task>>()
    val questions: LiveData<List<Task>> = _questions

    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("tasks")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val tasks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Task::class.java)
                }
                _questions.value = tasks
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 