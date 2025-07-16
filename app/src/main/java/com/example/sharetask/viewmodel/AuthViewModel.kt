package com.example.sharetask.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharetask.data.model.User
import com.example.sharetask.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthViewModel(private val app: Application) : AndroidViewModel(app) {

    // Firebase Auth & Firestore
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val name = MutableLiveData("")
    val jurusan = MutableLiveData("")
    val angkatan = MutableLiveData("")
    val status = MutableLiveData("")

    // Login pakai Email & Password
    fun loginWithEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val emailValue = email.value ?: ""
        val passwordValue = password.value ?: ""

        if (emailValue.isBlank() || passwordValue.isBlank()) return onError("Email dan password wajib diisi.")
        if (passwordValue.length < 6) return onError("Password minimal 6 karakter.")
        if (!emailValue.contains("@")) return onError("Format email salah.")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) return onError("Format email salah.")

        auth.signInWithEmailAndPassword(emailValue, passwordValue)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.localizedMessage ?: "Login gagal.") }
    }

    fun registerWithEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val nameValue = name.value.orEmpty().trim()
        val emailValue = email.value.orEmpty().trim()
        val passwordValue = password.value.orEmpty()

        when {
            nameValue.isBlank() || emailValue.isBlank() || passwordValue.isBlank() -> {
                onError("Semua field harus diisi.")
                return
            }
            passwordValue.length < 6 -> {
                onError("Password minimal 6 karakter.")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> {
                onError("Format email tidak valid.")
                return
            }
        }

        auth.createUserWithEmailAndPassword(emailValue, passwordValue)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onError("User ID tidak ditemukan.")
                    return@addOnSuccessListener
                }

                val newUser = mapOf(
                    "uid" to uid,
                    "name" to nameValue,
                    "email" to emailValue
                )

                db.collection("users").document(uid).set(newUser)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onError(e.localizedMessage ?: "Gagal menyimpan data user.")
                    }
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("email address is already in use") == true) {
                    onError("Email sudah terdaftar.")
                } else {
                    onError(e.message ?: "Register gagal.")
                }
            }
    }

    fun loginWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val user = auth.currentUser ?: return@addOnSuccessListener
                checkOrCreateUser(user, onSuccess, onError)
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "Login Google gagal.") }
    }

    private fun checkOrCreateUser(user: FirebaseUser, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userRef = db.collection("users").document(user.uid)
        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onSuccess()
            } else {
                val newUser = User(
                        uid = user.uid,
                        name = user.displayName ?: "",
                        email = user.email ?: "",
                        profilePic = user.photoUrl?.toString().orEmpty()
                    )

                    userRef.set(newUser)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError("Gagal menyimpan user.") }
            }
        }.addOnFailureListener {
            onError("Gagal mengakses data user.")
        }
    }

    private fun goToMain() {
        val i = Intent(app, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        app.startActivity(i)
    }
}
