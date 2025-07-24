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
import com.example.sharetask.databinding.ActivityLoginBinding
import com.example.sharetask.ui.main.MainActivity
import com.example.sharetask.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            task.result?.let { viewModel.loginWithGoogle(it, true) }
        } else {
            // Handle jika user membatalkan login
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.vm = viewModel

        // Inisialisasi GoogleSignInClient
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Pastikan ID ini benar dari Firebase Console
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLogin.setOnClickListener { viewModel.loginWithEmail() }
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeAuthentication()
    }

    override fun onStart() {
        super.onStart()
        // Auto-login should be handled in a dedicated Splash/Launcher activity,
        // not here, to prevent unexpected behavior when navigating from RegisterActivity.
    }

    // Fungsi untuk mengamati perubahan hasil autentikasi dari ViewModel
    private fun observeAuthentication() {
        lifecycleScope.launch {
            viewModel.authResult.collect { result ->
                when (result) {
                    is AuthResult.Loading -> { /* Show loading indicator */ }
                    is AuthResult.Success -> {
                        goToMain()
                    }
                    is AuthResult.Error -> {
                        Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
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


    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        viewModel.resetAuthResult()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}