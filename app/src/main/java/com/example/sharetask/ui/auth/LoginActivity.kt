package com.example.sharetask.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.sharetask.R
import com.example.sharetask.databinding.ActivityLoginBinding
import com.example.sharetask.ui.main.MainActivity
import com.example.sharetask.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    // Handle hasil dari Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // DataBinding dan ViewModel
        binding.lifecycleOwner = this
        binding.vm = viewModel

        // Login pakai Email & Password
        binding.btnLogin.setOnClickListener {
            viewModel.loginWithEmail(
                onSuccess = { goToMain() },
                onError = { showToast(it) }
            )
        }

        // Login pakai Akun Google
        binding.btnGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, gso)
            googleSignInLauncher.launch(googleClient.signInIntent)
        }

        // Arahkan ke halaman Register
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser

        // Auto-login kalau user sudah login sebelumnya
        user?.reload()?.addOnSuccessListener { //user.reload() adalah kunci untuk cek akun masih valid di Firebase
            if (user.uid.isNotEmpty()) {
                goToMain()
            } else {
                FirebaseAuth.getInstance().signOut()
            }
        }?.addOnFailureListener {
            // Akun sudah dihapus dari Firebase, logout local
            FirebaseAuth.getInstance().signOut()
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