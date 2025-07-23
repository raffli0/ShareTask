package com.example.sharetask.data.repository
import com.example.sharetask.data.model.Subject
import com.example.sharetask.data.model.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TaskRepository {

    private val db = Firebase.firestore

    // Mengambil semua subjek dari Firestore
    suspend fun getSubjects(): List<Subject> {
        return try {
            db.collection("subjects")
                .get()
                .await()
                .toObjects(Subject::class.java)
        } catch (e: Exception) {
            // Handle error, misalnya log atau return list kosong
            emptyList()
        }
    }

    // Mengambil semua tugas dari Firestore
    suspend fun getTasks(): List<Task> {
        return try {
            db.collection("tasks")
                // .orderBy("timestamp", Query.Direction.DESCENDING) // Contoh sorting
                .get()
                .await()
                .toObjects(Task::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}