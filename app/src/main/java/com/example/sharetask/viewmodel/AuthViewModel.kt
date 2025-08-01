package com.example.sharetask.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharetask.data.model.AuthResult
import com.example.sharetask.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val name = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")

    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult: StateFlow<AuthResult?> = _authResult

    private fun saveUserToFirestore(user: FirebaseUser, name: String, isGoogleSignIn: Boolean) {
        // Temporarily comment out Firestore save to test
        Log.d("AuthViewModel", "Skipping Firestore save for testing")
        val userDoc = User(
            uid = user.uid,
            name = name,
            email = user.email ?: "",
            profilePic = user.photoUrl?.toString(),
            role = "Beginner",
            rewardPoints = 0,
            followingCount = 0,
            followersCount = 0
        )

        firestore.collection("users").document(user.uid)
            .set(userDoc)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "User profile saved to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Failed to save user: ${e.message}")
            }

    }
    
    fun registerWithEmail() {
        _authResult.value = AuthResult.Loading
        val nameValue = name.value.orEmpty().trim()
        val emailValue = email.value.orEmpty().trim()
        val passwordValue = password.value.orEmpty()

        if (nameValue.isBlank() || emailValue.isBlank() || passwordValue.isBlank()) {
            _authResult.value = AuthResult.Error("Semua field harus diisi.")
            return
        }
        if (passwordValue.length < 6) {
            _authResult.value = AuthResult.Error("Password minimal 6 karakter.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            _authResult.value = AuthResult.Error("Format email tidak valid.")
            return
        }

        auth.createUserWithEmailAndPassword(emailValue, passwordValue)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nameValue)
                        .build()
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileUpdateTask ->
                        if (profileUpdateTask.isSuccessful && user != null) {
                            saveUserToFirestore(user, nameValue, false)
                            _authResult.value = AuthResult.Success("Registrasi berhasil, silahkan login.", isNewUser = true, isGoogleSignIn = false)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                val message = if (e.message?.contains("email address is already in use") == true) {
                    "Email sudah terdaftar."
                } else {
                    e.localizedMessage ?: "Register gagal."
                }
                _authResult.value = AuthResult.Error(message)
            }
    }

    fun loginWithEmail() {
        _authResult.value = AuthResult.Loading
        val emailValue = email.value.orEmpty().trim()
        val passwordValue = password.value.orEmpty()

        if (emailValue.isBlank() || passwordValue.isBlank()) {
            _authResult.value = AuthResult.Error("Email dan password wajib diisi.")
            return
        }

        auth.signInWithEmailAndPassword(emailValue, passwordValue)
            .addOnSuccessListener {
                _authResult.value = AuthResult.Success("Login berhasil.")
            }
            .addOnFailureListener { e ->
                val message = if (e.message?.contains("email address is already in use") == true) {
                    "Email sudah terdaftar."
                } else if (e.message?.contains("wrong password") == true) {
                    "Password salah."
                } else {
                    e.localizedMessage ?: "Login gagal."
                }
                _authResult.value = AuthResult.Error(e.localizedMessage ?: "Login gagal.")
            }
    }

    fun loginWithGoogle(googleSignInAccount: GoogleSignInAccount, canRegisterNewUser: Boolean) {
        _authResult.value = AuthResult.Loading
        
        Log.d("AuthViewModel", "Starting Google Sign-In for: ${googleSignInAccount.email}")
        
        val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("AuthViewModel", "Google Sign-In task successful")
                    
                    if (user != null) {
                        val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                        Log.d("AuthViewModel", "User obtained: ${user.email}")
                        Log.d("AuthViewModel", "Is new user: $isNewUser")

                        if (isNewUser && user != null) {
                            // Pengguna baru, akun Google baru saja dibuat di Firebase
                            val displayName = googleSignInAccount.displayName ?: "Pengguna Baru"
                            val email = googleSignInAccount.email
                            saveUserToFirestore(user, displayName, true)
                            _authResult.value = AuthResult.Success(
                                message = "Pendaftaran dengan Google berhasil! Akun Anda telah dibuat.",
                                isNewUser = true,
                                isGoogleSignIn = true,
                            )
                        } else {
                            // Pengguna sudah ada, mencoba "mendaftar" dengan akun yang sudah terdaftar.
                            // Di sini kita bisa memberikan pesan spesifik.
                            _authResult.value = AuthResult.Success( // Tetap Success karena mereka berhasil diautentikasi
                                message = "Akun Google ini sudah terdaftar. Anda akan login.",
                                isNewUser = false,
                                isGoogleSignIn = true
                            )
                            // Alternatifnya, ini bisa dianggap sebagai AuthResult.Info atau jenis lain jika
                            // Anda ingin UI bereaksi sangat berbeda. Tapi untuk sekarang, Success dengan pesan
                            // yang jelas sudah cukup baik, dan pengguna akan diloginkan.
                        }
                        
                        // Pastikan session tersimpan dengan benar
                        Log.d("AuthViewModel", "Google Sign-In successful. User ID: ${user.uid}")
                        Log.d("AuthViewModel", "User email: ${user.email}")
                        Log.d("AuthViewModel", "User display name: ${user.displayName}")
                        
                        // Verifikasi session tersimpan
                        val currentUserAfterLogin = auth.currentUser
                        Log.d("AuthViewModel", "Current user after login: ${currentUserAfterLogin?.email}")
                        
                    } else {
                        Log.e("AuthViewModel", "User is null after successful Google Sign-In")
                        _authResult.value = AuthResult.Error("Gagal mendapatkan informasi pengguna.")
                    }
                } else {
                    // Gagal login/daftar dengan Google
                    Log.e("AuthViewModel", "Google Sign-In failed: ${task.exception?.message}")
                    Log.e("AuthViewModel", "Exception: ${task.exception}")
                    _authResult.value = AuthResult.Error("Autentikasi Google gagal: ${task.exception?.message}")
                }
            }
    }

    fun resetAuthResult() {
        _authResult.value = null
    }

    // Debug method untuk cek Firebase configuration
    fun debugFirebaseAuth() {
        Log.d("AuthViewModel", "=== Firebase Auth Debug ===")
        Log.d("AuthViewModel", "Auth instance: $auth")
        Log.d("AuthViewModel", "Current user: ${auth.currentUser?.email}")
        Log.d("AuthViewModel", "User ID: ${auth.currentUser?.uid}")
        Log.d("AuthViewModel", "User display name: ${auth.currentUser?.displayName}")
        Log.d("AuthViewModel", "User photo URL: ${auth.currentUser?.photoUrl}")
        Log.d("AuthViewModel", "User is email verified: ${auth.currentUser?.isEmailVerified}")
        Log.d("AuthViewModel", "User creation timestamp: ${auth.currentUser?.metadata?.creationTimestamp}")
        Log.d("AuthViewModel", "User last sign in timestamp: ${auth.currentUser?.metadata?.lastSignInTimestamp}")
        Log.d("AuthViewModel", "=== End Debug ===")
    }
}
