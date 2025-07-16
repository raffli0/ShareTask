package com.example.sharetask

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.sharetask.ui.auth.LoginActivity
import com.example.sharetask.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Tambahkan animasi
        val logo = findViewById<ImageView>(R.id.imgLogo)
        val anim = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        logo.startAnimation(anim)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // User sudah login
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // User belum login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            startActivity(intent)
            finish()
        }, 1500) // Delay 1.5 detik
    }
}
