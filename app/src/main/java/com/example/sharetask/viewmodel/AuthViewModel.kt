package com.example.sharetask.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharetask.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthViewModel(private val app: Application) : AndroidViewModel(app) {

    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val name = MutableLiveData("")
    val jurusan = MutableLiveData("")
    val angkatan = MutableLiveData("")
    val status = MutableLiveData("")

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    fun loginWithEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val e = email.value ?: ""
        val p = password.value ?: ""

        if (e.isBlank() || p.isBlank()) return onError("Email dan password wajib diisi.")

        auth.signInWithEmailAndPassword(e, p)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.localizedMessage ?: "Login gagal.") }
    }

    fun registerWithEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val n = name.value ?: ""
        val e = email.value ?: ""
        val p = password.value ?: ""
        val j = jurusan.value ?: ""
        val a = angkatan.value ?: ""

        if (n.isBlank() || e.isBlank() || p.isBlank() || j.isBlank() || a.isBlank())
            return onError("Semua field harus diisi.")

        auth.createUserWithEmailAndPassword(e, p)
            .addOnSuccessListener {
                val uid = it.user?.uid ?: return@addOnSuccessListener
                val userMap = mapOf(
                    "uid" to uid,
                    "name" to n,
                    "email" to e,
                    "jurusan" to j,
                    "angkatan" to a,
                    "role" to "Mahasiswa",
                    "rewardPoints" to 0
                )
                db.collection("users").document(uid).set(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { err -> onError(err.localizedMessage ?: "Gagal menyimpan data user.") }
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "Gagal mendaftar.") }
    }

    fun loginWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val user = auth.currentUser ?: return@addOnSuccessListener
                val docRef = db.collection("users").document(user.uid)
                docRef.get().addOnSuccessListener { snap ->
                    if (!snap.exists()) {
                        val newUser = mapOf(
                            "uid" to user.uid,
                            "name" to (user.displayName ?: ""),
                            "email" to (user.email ?: ""),
                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                            "jurusan" to "",
                            "angkatan" to "",
                            "role" to "Mahasiswa",
                            "rewardPoints" to 0
                        )
                        docRef.set(newUser)
                    }
                    onSuccess()
                }.addOnFailureListener { onError("Gagal ambil data user.") }
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "Login Google gagal.") }
    }

    private fun goToMain() {
        val i = Intent(app, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        app.startActivity(i)
    }
}
