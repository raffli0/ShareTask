package com.example.sharetask.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sharetask.R
import com.example.sharetask.data.model.AuthResult
import com.example.sharetask.databinding.ActivityRegisterBinding
import com.example.sharetask.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            task.result?.let { viewModel.loginWithGoogle(it, isRegister = true) }
        } else {
            showToast("Login Google gagal")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.vm = viewModel

        binding.btnRegister.setOnClickListener { viewModel.registerWithEmail() }
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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

                        // reset input field
                        binding.etFullName.text?.clear()
                        binding.etEmail.text?.clear()
                        binding.etPassword.text?.clear()

                        viewModel.resetAuthResult()
                    }
                    is AuthResult.Error -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                    null -> { /* Initial state */ }
                }
            }
        }
    }

    // Fungsi untuk memulai proses login dengan Google
    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleClient.signInIntent)
    }


    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        viewModel.resetAuthResult()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}