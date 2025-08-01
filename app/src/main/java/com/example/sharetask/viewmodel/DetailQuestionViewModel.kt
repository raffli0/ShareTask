package com.example.sharetask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sharetask.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

// Import Answer model dari package data.model
import com.example.sharetask.data.model.Answer

class DetailQuestionViewModel : ViewModel() {
    private val _question = MutableLiveData<Question>()
    val question: LiveData<Question> = _question

    private val _answers = MutableLiveData<List<Answer>>()
    val answers: LiveData<List<Answer>> = _answers
    
    private val _isAddingAnswer = MutableLiveData<Boolean>(false)
    val isAddingAnswer: LiveData<Boolean> = _isAddingAnswer
    
    private val _addAnswerSuccess = MutableLiveData<Boolean?>(null)
    val addAnswerSuccess: LiveData<Boolean?> = _addAnswerSuccess
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun loadQuestion(questionId: String) {
        if (questionId.isEmpty()) return
        
        FirebaseFirestore.getInstance().collection("question")
            .document(questionId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val question = document.toObject(Question::class.java)
                    if (question != null) {
                        _question.value = question
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }

    fun loadAnswers(questionId: String) {
        if (questionId.isEmpty()) return
        
        firestore.collection("question")
            .document(questionId)
            .collection("answers")
            .get()
            .addOnSuccessListener { result ->
                val answersList = result.documents.mapNotNull { doc ->
                    // Konversi dari Firestore document ke model Answer
                    val id = doc.id
                    val questionId = doc.getString("questionId") ?: ""
                    val userId = doc.getString("userId") ?: ""
                    val userName = doc.getString("userName") ?: ""
                    val userPhotoUrl = doc.getString("userPhotoUrl")
                    val content = doc.getString("content") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    
                    Answer(id, questionId, userId, userName, userPhotoUrl, content, timestamp)
                }
                _answers.value = answersList
            }
            .addOnFailureListener { e ->
                // Handle error - for now, provide empty list
                _answers.value = emptyList()
            }
    }
    
    /**
     * Menambahkan jawaban baru ke pertanyaan
     */
    fun addAnswer(questionId: String, content: String) {
        if (questionId.isEmpty() || content.isEmpty()) return
        
        val currentUser = auth.currentUser ?: return
        
        _isAddingAnswer.value = true
        _addAnswerSuccess.value = null
        
        // Buat objek Answer baru
        val answerId = firestore.collection("question")
            .document(questionId)
            .collection("answers")
            .document().id
            
        val answer = Answer(
            id = answerId,
            questionId = questionId,
            userId = currentUser.uid,
            userName = currentUser.displayName ?: "",
            userPhotoUrl = currentUser.photoUrl?.toString(),
            content = content,
            timestamp = System.currentTimeMillis()
        )
        
        // Simpan ke Firestore
        firestore.collection("question")
            .document(questionId)
            .collection("answers")
            .document(answerId)
            .set(answer)
            .addOnSuccessListener {
                // Reload answers setelah berhasil menambahkan
                loadAnswers(questionId)
                _isAddingAnswer.value = false
                _addAnswerSuccess.value = true
            }
            .addOnFailureListener { e ->
                // Handle error
                _isAddingAnswer.value = false
                _addAnswerSuccess.value = false
            }
    }
}