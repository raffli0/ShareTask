package com.example.sharetask.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UploadViewModel : ViewModel() {

    private val _selectedFile = MutableLiveData<Uri?>()
    val selectedFile: LiveData<Uri?> get() = _selectedFile

    fun setSelectedFile(uri: Uri) {
        _selectedFile.value = uri
    }

    fun uploadFile(description: String) {
        val fileUri = _selectedFile.value
        // TODO: Implement the file upload logic here, using fileUri and the description
        // You might use Firebase Storage or another mechanism for uploading.
    }
}