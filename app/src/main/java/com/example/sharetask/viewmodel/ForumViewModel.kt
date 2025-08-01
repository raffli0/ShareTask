package com.example.sharetask.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharetask.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ForumViewModel : ViewModel() {
    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val firestore = FirebaseFirestore.getInstance()


    fun loadQuestions() {
        viewModelScope.launch {
            try {
                val start = System.currentTimeMillis()
                val snapshot = firestore.collection("question")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
//                    .limit(20)
                    .get()
                    .await()

                Log.d("ForumPerf", "Fetch took ${System.currentTimeMillis() - start}ms")

                val questions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Question::class.java)
                }
                _questions.value = questions
            } catch (e: Exception) {
                // Handle error
            }
        }
    }


} 