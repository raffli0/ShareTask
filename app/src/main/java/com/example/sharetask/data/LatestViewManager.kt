package com.example.sharetask.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sharetask.data.model.LatestView
import com.example.sharetask.data.model.Question
import com.google.gson.Gson

class LatestViewManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "latest_view_prefs"
        private const val LATEST_VIEWS_KEY = "latest_views"
        private const val MAX_LATEST_VIEWS = 10 // Batas maksimum item yang disimpan
        
        @Volatile
        private var INSTANCE: LatestViewManager? = null
        
        fun getInstance(context: Context): LatestViewManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LatestViewManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Menambahkan pertanyaan ke daftar Latest View
     * Jika pertanyaan sudah ada dalam daftar, akan diperbarui timestamp-nya
     * Jika jumlah pertanyaan melebihi batas, pertanyaan terlama akan dihapus
     */
    fun addToLatestView(question: Question) {
        val latestViews = getLatestViews().toMutableList()
        
        // Cek apakah pertanyaan sudah ada dalam daftar
        val existingIndex = latestViews.indexOfFirst { it.questionId == question.id }
        
        if (existingIndex != -1) {
            // Jika sudah ada, hapus item lama
            latestViews.removeAt(existingIndex)
        }
        
        // Tambahkan pertanyaan baru ke awal list
        val latestView = LatestView(
            id = System.currentTimeMillis().toString(),
            questionId = question.id,
            title = question.subjectName,
            description = question.description,
            timestamp = question.timestamp,
            thumbnailUrl = question.imageUrl,
            viewedAt = System.currentTimeMillis()
        )
        
        latestViews.add(0, latestView)
        
        // Jika melebihi batas maksimum, hapus item terlama
        if (latestViews.size > MAX_LATEST_VIEWS) {
            latestViews.removeAt(latestViews.size - 1)
        }
        
        // Simpan kembali ke SharedPreferences
        saveLatestViews(latestViews)
    }
    
    /**
     * Mendapatkan daftar Latest View
     */
    fun getLatestViews(): List<LatestView> {
        val json = sharedPreferences.getString(LATEST_VIEWS_KEY, null) ?: return emptyList()
        val type = object : com.google.gson.reflect.TypeToken<List<LatestView>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Menyimpan daftar Latest View ke SharedPreferences
     */
    private fun saveLatestViews(latestViews: List<LatestView>) {
        val json = gson.toJson(latestViews)
        sharedPreferences.edit().putString(LATEST_VIEWS_KEY, json).apply()
    }
    
    /**
     * Menghapus semua data Latest View
     */
    fun clearLatestViews() {
        sharedPreferences.edit().remove(LATEST_VIEWS_KEY).apply()
    }
}