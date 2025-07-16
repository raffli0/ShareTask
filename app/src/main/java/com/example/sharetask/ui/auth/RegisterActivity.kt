package com.example.sharetask.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sharetask.R
import com.example.sharetask.databinding.ActivityLoginBinding
import com.example.sharetask.databinding.ActivityRegisterBinding
import com.example.sharetask.ui.main.MainActivity
import com.example.sharetask.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    // Handle Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            viewModel.loginWithGoogle(account,
                onSuccess = { goToMain() },
                onError = { showToast(it) }
            )
        } else {
            showToast("Login Google gagal")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bind ViewModel dan Lifecycle ke layout
        binding.lifecycleOwner = this
        binding.vm = viewModel

        // Tombol Register Email/Password
        binding.btnRegister.setOnClickListener {
            viewModel.registerWithEmail(
                onSuccess = {
                    Snackbar.make(
                        binding.root,
                        "Akun berhasil dibuat. Silakan login.",
                        Snackbar.LENGTH_LONG
                    ).show()

                    // Delay biar snackbar muncul dulu
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }, 1500)
                },
                onError = {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
            )
        }

        // Tombol Google Sign-In
        binding.btnGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, gso)
            googleSignInLauncher.launch(googleClient.signInIntent)
        }

        // Arahkan ke halaman Login
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // Fungsi navigasi ke MainActivity (Dashboard)
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // Fungsi memunculkan Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}