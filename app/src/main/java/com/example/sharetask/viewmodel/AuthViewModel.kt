package com.example.sharetask.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sharetask.data.model.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()

    val name = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")

    private val _authResult = MutableStateFlow<AuthResult?>(null)
//    private val _clearInput = MutableLiveData<Boolean>()
//    val clearInput: LiveData<Boolean> = _clearInput
    val authResult: StateFlow<AuthResult?> = _authResult

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
            .addOnSuccessListener {
                _authResult.value = AuthResult.Success("Registrasi berhasil, silahkan login.")
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

    fun loginWithGoogle(account: GoogleSignInAccount, isRegister: Boolean) {
        _authResult.value = AuthResult.Loading
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _authResult.value = AuthResult.Success("Login berhasil.")
            }
            .addOnFailureListener { e ->
                _authResult.value = AuthResult.Error(e.localizedMessage ?: "Login gagal.")
            }
    }


    fun resetAuthResult() {
        _authResult.value = null
    }
}
