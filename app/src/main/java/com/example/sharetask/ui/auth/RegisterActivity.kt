package com.example.sharetask.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sharetask.R
import com.example.sharetask.data.model.AuthResult
import com.example.sharetask.databinding.ActivityRegisterBinding
import com.example.sharetask.ui.main.MainActivity
import com.example.sharetask.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) { // Periksa resultCode juga
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!! // Melempar ApiException jika gagal
                // Panggil ViewModel dengan flag `true` untuk mengizinkan pendaftaran baru
                // atau login jika akun sudah ada.
                viewModel.loginWithGoogle(account, true)
            } catch (e: ApiException) {
                // Tangani error spesifik dari Google Sign-In (mis. pengguna membatalkan, masalah jaringan)
                Log.w("RegisterActivity", "Google sign in failed", e)
                Snackbar.make(binding.root, "Gagal mendapatkan akun Google: ${e.statusCode}", Snackbar.LENGTH_LONG).show()
            }
        } else {
            // Pengguna mungkin membatalkan dialog pemilihan akun Google
            Snackbar.make(binding.root, "Pendaftaran dengan Google dibatalkan.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.vm = viewModel

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnRegister.setOnClickListener { viewModel.registerWithEmail() }
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
        binding.tvLogin.setOnClickListener {
            goToLogin()
        }
        observeAuthentication()
    }

    // Fungsi untuk mengamati perubahan hasil autentikasi dari ViewModel
    private fun observeAuthentication() {
        lifecycleScope.launch {
            viewModel.authResult.collect { result ->
                when (result) {
                    is AuthResult.Loading -> { /* Show loading indicator */ }
                    is AuthResult.Success -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                        // Don't navigate to login, let the user do it manually.

                        if (result.isGoogleSignIn) {
                            goToMain()
                        } else {
                            if (result.isNewUser == true) {
                                goToLogin()
                            }
                        }
                        resetField()
                        viewModel.resetAuthResult()
                    }
                    is AuthResult.Error -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                        viewModel.resetAuthResult()
                    }
                    null -> { /* Initial state */ }
                }
            }
        }
    }

    // Fungsi untuk memulai proses login dengan Google
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        viewModel.resetAuthResult()
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Tutup RegisterActivity agar tidak bisa kembali
    }

    fun resetField() {
        binding.etFullName.text?.clear()
        binding.etEmail.text?.clear()
        binding.etPassword.text?.clear()
        viewModel.resetAuthResult()
    }
}