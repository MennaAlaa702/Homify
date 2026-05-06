package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        // يفتح شاشة الـ Register
        btnStart.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // يفتح شاشة الـ Login
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}