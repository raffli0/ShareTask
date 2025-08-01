package com.example.sharetask.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sharetask.data.model.LatestView
import com.example.sharetask.data.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LatestViewManager private constructor(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _latestViewList = MutableLiveData<List<LatestView>>()
    val latestViewList: LiveData<List<LatestView>> = _latestViewList

    private val userId: String
        get() = auth.currentUser?.uid ?: ""
    
    companion object {
        private const val TAG = "LatestViewManager"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_LATEST_VIEWS = "latest_views"
        private const val MAX_LATEST_VIEW_ITEMS = 10
        
        @Volatile
        private var instance: LatestViewManager? = null
        
        fun getInstance(context: Context): LatestViewManager {
            return instance ?: synchronized(this) {
                instance ?: LatestViewManager(context.applicationContext).also { 
                    instance = it
                    it.loadLatestViewList() // Load data when instance is created
                }
            }
        }
    }
    
    /**
     * Menambahkan pertanyaan ke daftar latest view di Firestore
     */
    fun addQuestion(question: Question) {
        if (userId.isEmpty()) {
            Log.w(TAG, "Cannot add to latest view: User not logged in")
            return
        }
        
        // Buat objek LatestView baru dengan timestamp saat ini
        val currentTime = System.currentTimeMillis()
        val latestView = LatestView(
            id = currentTime.toString(),
            questionId = question.id,
            title = question.subjectName,
            description = question.description,
            timestamp = question.timestamp,
            thumbnailUrl = question.imageUrl,
            viewedAt = currentTime
        )
        
        Log.d(TAG, "Adding latest view: id=${latestView.id}, questionId=${latestView.questionId}, viewedAt=${latestView.viewedAt}")
        
        // Simpan ke Firestore
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_LATEST_VIEWS)
            .document(latestView.id)
            .set(latestView)
            .addOnSuccessListener {
                Log.d(TAG, "Latest view added successfully with viewedAt=${latestView.viewedAt}")
                // Reload data setelah menambahkan
                loadLatestViewList()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding latest view", e)
            }
    }
    
    // Removed getLatestViewList() method as it conflicts with the latestViewList property
    // which already provides the same functionality

    /**
     * Mendapatkan daftar latest view sebagai List (untuk kompatibilitas)
     */
    fun getLatestViewListSync(): List<LatestView> {
        return latestViewList.value ?: emptyList()
    }
    
    /**
     * Memuat daftar latest view dari Firestore
     */
    fun loadLatestViewList() {
        if (userId.isEmpty()) {
            Log.w(TAG, "Cannot load latest views: User not logged in")
            _latestViewList.value = emptyList()
            return
        }
        
        Log.d(TAG, "Loading latest views for user: $userId")
        
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_LATEST_VIEWS)
            .orderBy("viewedAt", Query.Direction.DESCENDING)
            .limit(MAX_LATEST_VIEW_ITEMS.toLong())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG, "No latest views found for user")
                    _latestViewList.value = emptyList()
                    return@addOnSuccessListener
                }
                
                val latestViews = documents.mapNotNull { doc ->
                    try {
                        val latestView = doc.toObject(LatestView::class.java)
                        Log.d(TAG, "Loaded item: ${latestView.id}, questionId: ${latestView.questionId}, viewedAt: ${latestView.viewedAt}")
                        latestView
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to LatestView", e)
                        null
                    }
                }
                
                _latestViewList.value = latestViews
                Log.d(TAG, "Loaded ${latestViews.size} latest views")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading latest views", e)
                _latestViewList.value = emptyList()
            }
    }
    
    /**
     * Menghapus semua item dari daftar latest view
     */
    fun clearLatestViewList() {
        if (userId.isEmpty()) {
            Log.w(TAG, "Cannot clear latest views: User not logged in")
            return
        }
        
        val latestViewsRef = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_LATEST_VIEWS)
        
        latestViewsRef.get()
            .addOnSuccessListener { documents ->
                val batch = firestore.batch()
                for (document in documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "Latest views cleared successfully")
                        _latestViewList.value = emptyList()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error clearing latest views", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting latest views to clear", e)
            }
    }
}