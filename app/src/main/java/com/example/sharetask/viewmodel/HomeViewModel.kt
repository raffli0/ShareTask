package com.example.sharetask.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharetask.data.model.Question
import com.example.sharetask.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val _question = MutableLiveData<List<Question>>()
    val question: LiveData<List<Question>> = _question

    private val _friendList = MutableLiveData<List<User>>()
    val friendList: LiveData<List<User>> = _friendList

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val firestore = FirebaseFirestore.getInstance()
    private var allQuestions: List<Question> = emptyList()

    init {
        loadQuestion()
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun loadFriendList(currentUid: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUid)
            .collection("following") // Atau "friends", sesuai struktur lo
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull {
                    val uid = it.getString("uid") ?: return@mapNotNull null
                    val name = it.getString("name") ?: ""
                    User(uid = uid, name = name)
                }
                _friendList.value = list
            }
    }

    fun loadQuestion() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("question")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()

                allQuestions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Question::class.java)
                }
                _question.value = allQuestions
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun filterQuestionBySubject(subjectId: String) {
        if (subjectId.isEmpty()) {
            _question.value = allQuestions
        } else {
            _question.value = allQuestions.filter { it.subjectId == subjectId }
        }
    }
}
