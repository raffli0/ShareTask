package com.example.sharetask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharetask.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val _tasks = MutableLiveData<List<Question>>()
    val tasks: LiveData<List<Question>> = _tasks

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val firestore = FirebaseFirestore.getInstance()
    private var allQuestions: List<Question> = emptyList()

    init {
        loadTasks()
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun loadTasks() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("tasks")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()

                allQuestions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Question::class.java)
                }
                _tasks.value = allQuestions
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun filterTasksBySubject(subjectId: String) {
        if (subjectId.isEmpty()) {
            _tasks.value = allQuestions
        } else {
            _tasks.value = allQuestions.filter { it.subjectId == subjectId }
        }
    }
}
