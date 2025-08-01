package com.example.sharetask.viewmodel

import androidx.lifecycle.ViewModel
import android.net.Uri
import androidx.lifecycle.*
import com.example.sharetask.data.model.User // Import User model Anda
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class EditProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    // LiveData untuk data binding dan observasi
    // Ini akan diisi dari Firestore saat loadUserProfile()
    val currentName = MutableLiveData<String>()
    val currentBio = MutableLiveData<String?>() // Anggap ada field 'bio' yang bisa ditambahkan ke User model jika perlu
    val currentNim = MutableLiveData<String?>()
    val currentProfilePicUrl = MutableLiveData<String?>() // Untuk menampilkan foto saat ini

    // Untuk menyimpan URI gambar lokal yang baru dipilih
    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Menggunakan Result wrapper untuk status update
    sealed class UpdateStatus {
        object Success : UpdateStatus()
        data class Error(val message: String) : UpdateStatus()
        object Idle : UpdateStatus() // Status awal
    }
    private val _profileUpdateStatus = MutableLiveData<UpdateStatus>(UpdateStatus.Idle)
    val profileUpdateStatus: LiveData<UpdateStatus> = _profileUpdateStatus


    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        if (currentUserId == null) {
            _profileUpdateStatus.value = UpdateStatus.Error("User not logged in")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val documentSnapshot = db.collection("users").document(currentUserId).get().await()
                if (documentSnapshot.exists()) {
                    // Mapping langsung ke User model Anda
                    val user = documentSnapshot.toObject(User::class.java)
                    user?.let {
                        currentName.postValue(it.name) // Dari User.name
                        // Jika Anda menambahkan 'bio' ke User model:
                        // currentBio.postValue(it.bio ?: "")
                        // Jika tidak ada 'bio', Anda bisa mengosongkannya atau tidak menampilkannya
                        currentBio.postValue("") // Kosongkan jika tidak ada di model User

                        currentNim.postValue(it.nim ?: "") // Dari User.nim
                        currentProfilePicUrl.postValue(it.profilePic) // Dari User.profilePic
                    }
                } else {
                    _profileUpdateStatus.value = UpdateStatus.Error("User profile not found.")
                }
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Error loading profile", e)
                _profileUpdateStatus.value = UpdateStatus.Error("Failed to load profile: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun saveProfileChanges(
        newName: String,
        newNim: String?, // Jadikan nullable jika NIM opsional
//        newBio: String? // Jika Anda memutuskan untuk menambahkan bio
    ) {
        if (currentUserId == null) {
            _profileUpdateStatus.value = UpdateStatus.Error("User not logged in")
            return
        }
        if (newName.isBlank()) {
            _profileUpdateStatus.value = UpdateStatus.Error("Name cannot be empty.")
            return
        }

        _isLoading.value = true
        _profileUpdateStatus.value = UpdateStatus.Idle // Reset status sebelum operasi baru
        viewModelScope.launch {
            try {
                var newUploadedPhotoUrl: String? = currentProfilePicUrl.value // URL foto lama sebagai default

                // 1. Upload foto baru jika ada
                val localImageUri = _selectedImageUri.value
                if (localImageUri != null) {
                    val photoRef = storage.reference.child("profile_pictures/$currentUserId/${localImageUri.lastPathSegment}")
                    val uploadTask = photoRef.putFile(localImageUri).await()
                    newUploadedPhotoUrl = uploadTask.storage.downloadUrl.await().toString()
                }

                // 2. Siapkan data untuk update di Firestore
                val profileUpdates = hashMapOf<String, Any?>(
                    "name" to newName, // Update field 'name' di Firestore
                    "nim" to newNim // Update field 'nim'
                    // "bio" to newBio, // Jika Anda menambahkan bio
                )
                // Hanya update profilePic jika gambar baru diupload ATAU jika foto dihapus (newUploadedPhotoUrl menjadi null)
                if (localImageUri != null || newUploadedPhotoUrl != currentProfilePicUrl.value) {
                    profileUpdates["profilePic"] = newUploadedPhotoUrl // Update field 'profilePic'
                }

                // 3. Update Firestore
                db.collection("users").document(currentUserId)
                    .update(profileUpdates)
                    .await()
                    
                // 4. Update Firebase Auth display name jika berubah
                if (newName != currentName.value) {
                    val profileUpdatesAuth = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                    auth.currentUser?.updateProfile(profileUpdatesAuth)?.await()
                }

                // Perbarui LiveData lokal agar UI sinkron tanpa perlu reload
                currentName.postValue(newName)
                currentNim.postValue(newNim ?: "")
                // currentBio.postValue(newBio ?: "")
                if (newUploadedPhotoUrl != currentProfilePicUrl.value) {
                    currentProfilePicUrl.postValue(newUploadedPhotoUrl)
                }
                _selectedImageUri.postValue(null) // Reset URI gambar yang dipilih

                _profileUpdateStatus.postValue(UpdateStatus.Success)
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Error saving profile", e)
                _profileUpdateStatus.postValue(UpdateStatus.Error("Failed to save profile: ${e.message}"))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
