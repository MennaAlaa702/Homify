package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class onboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        // يفتح شاشة الـ Register
        btnStart.setOnClickListener {
            startActivity(Intent(this, registerActivity::class.java))
        }

        // يفتح شاشة الـ Login
        btnLogin.setOnClickListener {
            startActivity(Intent(this, loginActivity::class.java))
        }
    }
}