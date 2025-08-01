package com.example.sharetask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sharetask.data.model.Question

// Dummy Answer data class (should be moved to its own file if needed)
data class Answer(
    val id: String = "",
    val questionId: String = "",
    val content: String = "",
    val author: String = "",
    val authorPhotoUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class DetailQuestionViewModel : ViewModel() {
    private val _question = MutableLiveData<Question>()
    val question: LiveData<Question> = _question

    private val _answers = MutableLiveData<List<Answer>>()
    val answers: LiveData<List<Answer>> = _answers

    fun loadQuestion(questionId: String) {
        // TODO: Replace with Firestore/Repository fetch
        _question.value = Question(
            id = questionId,
            description = "Contoh pertanyaan detail",
            uploadedBy = "Jane Doe",
            uploadedByPhotoUrl = "",
            subjectName = "Mobile Development",
            timestamp = System.currentTimeMillis()
        )
    }

    fun loadAnswers(questionId: String) {
        // TODO: Replace with Firestore/Repository fetch
        _answers.value = listOf(
            Answer(id = "1", questionId = questionId, content = "Ini jawaban 1", author = "User A", authorPhotoUrl = "", timestamp = System.currentTimeMillis()),
            Answer(id = "2", questionId = questionId, content = "Ini jawaban 2", author = "User B", authorPhotoUrl = "", timestamp = System.currentTimeMillis())
        )
    }
}