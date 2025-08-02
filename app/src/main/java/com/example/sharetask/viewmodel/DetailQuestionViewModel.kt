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
                        // Ambil nama pengguna yang sebenarnya dari Firestore
                        val userId = document.getString("userId") ?: ""
                        if (userId.isNotEmpty()) {
                            FirebaseFirestore.getInstance().collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener { userDoc ->
                                    if (userDoc != null && userDoc.exists()) {
                                        val userName = userDoc.getString("name") ?: "User"
                                        val userPhotoUrl = userDoc.getString("profilePic") ?: ""
                                        // Buat salinan question dengan nama pengguna yang benar
                                        val updatedQuestion = question.copy(
                                            uploadedBy = userName,
                                            uploadedByPhotoUrl = userPhotoUrl
                                        )
                                        _question.value = updatedQuestion
                                    } else {
                                        _question.value = question
                                    }
                                }
                                .addOnFailureListener { _ ->
                                    _question.value = question
                                }
                        } else {
                            _question.value = question
                        }
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
                // Buat daftar untuk menyimpan jawaban sementara
                val tempAnswers = mutableListOf<Answer>()
                
                // Jika tidak ada jawaban, langsung set nilai kosong
                if (result.isEmpty) {
                    _answers.value = emptyList()
                    return@addOnSuccessListener
                }
                
                // Hitung berapa banyak jawaban yang perlu diproses
                var answersToProcess = result.size()
                
                // Proses setiap dokumen jawaban
                for (doc in result.documents) {
                    // Konversi dari Firestore document ke model Answer
                    val id = doc.id
                    val questionId = doc.getString("questionId") ?: ""
                    val userId = doc.getString("userId") ?: ""
                    var userName = doc.getString("userName") ?: ""
                    var userPhotoUrl = doc.getString("userPhotoUrl")
                    val content = doc.getString("content") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    
                    // Buat objek Answer awal
                    val answer = Answer(id, questionId, userId, userName, userPhotoUrl, content, timestamp)
                    
                    // Tambahkan ke daftar sementara
                    tempAnswers.add(answer)
                    
                    // Ambil data pengguna yang sebenarnya dari Firestore jika userId tidak kosong
                    if (userId.isNotEmpty()) {
                        firestore.collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc != null && userDoc.exists()) {
                                    // Update nama dan foto pengguna
                                    userName = userDoc.getString("name") ?: "User"
                                    userPhotoUrl = userDoc.getString("profilePic")
                                    
                                    // Cari indeks jawaban dalam daftar sementara
                                    val index = tempAnswers.indexOfFirst { it.id == id }
                                    if (index != -1) {
                                        // Buat salinan jawaban dengan data pengguna yang benar
                                        val updatedAnswer = tempAnswers[index].copy(
                                            userName = userName,
                                            userPhotoUrl = userPhotoUrl
                                        )
                                        // Ganti jawaban lama dengan yang baru
                                        tempAnswers[index] = updatedAnswer
                                    }
                                }
                                
                                // Kurangi hitungan jawaban yang perlu diproses
                                answersToProcess--
                                
                                // Jika semua jawaban sudah diproses, update LiveData
                                if (answersToProcess == 0) {
                                    _answers.value = tempAnswers
                                }
                            }
                            .addOnFailureListener { _ ->
                                // Kurangi hitungan jawaban yang perlu diproses
                                answersToProcess--
                                
                                // Jika semua jawaban sudah diproses, update LiveData
                                if (answersToProcess == 0) {
                                    _answers.value = tempAnswers
                                }
                            }
                    } else {
                        // Kurangi hitungan jawaban yang perlu diproses
                        answersToProcess--
                        
                        // Jika semua jawaban sudah diproses, update LiveData
                        if (answersToProcess == 0) {
                            _answers.value = tempAnswers
                        }
                    }
                }
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
        
        val userId = currentUser.uid
        
        // Ambil data pengguna yang sebenarnya dari Firestore
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                // Dapatkan nama dan foto pengguna dari Firestore
                val userName = if (userDoc != null && userDoc.exists()) {
                    userDoc.getString("name") ?: currentUser.displayName ?: "User"
                } else {
                    currentUser.displayName ?: "User"
                }
                
                val userPhotoUrl = if (userDoc != null && userDoc.exists()) {
                    userDoc.getString("profilePic") ?: currentUser.photoUrl?.toString()
                } else {
                    currentUser.photoUrl?.toString()
                }
                
                // Buat objek Answer baru
                val answerId = firestore.collection("question")
                    .document(questionId)
                    .collection("answers")
                    .document().id
                    
                val answer = Answer(
                    id = answerId,
                    questionId = questionId,
                    userId = userId,
                    userName = userName,
                    userPhotoUrl = userPhotoUrl,
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
            .addOnFailureListener { e ->
                // Fallback ke data pengguna dari Auth jika gagal mengambil dari Firestore
                val userName = currentUser.displayName ?: "User"
                val userPhotoUrl = currentUser.photoUrl?.toString()
                
                // Buat objek Answer baru
                val answerId = firestore.collection("question")
                    .document(questionId)
                    .collection("answers")
                    .document().id
                    
                val answer = Answer(
                    id = answerId,
                    questionId = questionId,
                    userId = userId,
                    userName = userName,
                    userPhotoUrl = userPhotoUrl,
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
                    .addOnFailureListener { e2 ->
                        // Handle error
                        _isAddingAnswer.value = false
                        _addAnswerSuccess.value = false
                    }
            }
    }
}