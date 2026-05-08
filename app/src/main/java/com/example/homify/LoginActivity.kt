package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import data.entities.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvErrorEmail: TextView
    private lateinit var tvErrorPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupTextWatchers()

        // زرار الرجوع
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // زرار اللوجين
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            if (validateForm()) {
                loginUser()
            }
        }

        // لينك التسجيل
        findViewById<TextView>(R.id.tv_register_link).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    // ════════════════════════════════════════════
    //  التحقق من بيانات اللوجين من قاعدة البيانات
    // ════════════════════════════════════════════
    private fun loginUser() {
        val db      = (application as HomifyApp).database
        val userDao = db.userDao()

        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            // حالة خاصة للأدمن (مش موجود في شاشة التسجيل)
            val isAdmin = email.equals("admin@homify.com", ignoreCase = true)

            val user = userDao.getUserByEmail(email)

            withContext(Dispatchers.Main) {
                when {
                    // ── أدمن ──
                    isAdmin && user != null && user.password == password -> {
                        saveSession(user.userId, "${user.firstName} ${user.lastName}", email, "Admin")
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    }
                    // ── مستخدم عادي موجود وباسورده صح ──
                    user != null && user.password == password -> {
                        val roleStr = if (user.role == UserRole.landlord) "Landlord" else "Tenant"
                        saveSession(user.userId, "${user.firstName} ${user.lastName}", email, roleStr)

                        val intent = if (user.role == UserRole.landlord) {
                            Intent(this@LoginActivity, LandlordHomeActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, TenantHome::class.java)
                        }
                        Toast.makeText(this@LoginActivity, "Welcome back!", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
                    }
                    // ── الإيميل مش موجود ──
                    user == null -> {
                        showError(etEmail, tvErrorEmail, "Email not found")
                    }
                    // ── الباسورد غلط ──
                    else -> {
                        showError(etPassword, tvErrorPassword, "Incorrect password")
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════
    //  حفظ بيانات الجلسة في SharedPreferences
    // ════════════════════════════════════════════
    private fun saveSession(userId: Int, username: String, email: String, role: String) {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPref.edit().apply {
            putInt("userId", userId)
            putString("username", username)
            putString("email", email)
            putString("role", role)
            apply()
        }
    }

    // ════════════════════════════════════════════
    //  Validation
    // ════════════════════════════════════════════
    private fun validateForm(): Boolean {
        var isValid = true
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvErrorEmail, getString(R.string.err_email_invalid))
            isValid = false
        }
        if (password.isEmpty()) {
            showError(etPassword, tvErrorPassword, getString(R.string.err_pass_req))
            isValid = false
        }
        return isValid
    }

    private fun initializeViews() {
        etEmail         = findViewById(R.id.et_email)
        etPassword      = findViewById(R.id.et_password)
        tvErrorEmail    = findViewById(R.id.tv_error_email)
        tvErrorPassword = findViewById(R.id.tv_error_password)
    }

    private fun showError(editText: EditText?, errorTextView: TextView, message: String) {
        editText?.setBackgroundResource(R.drawable.bg_input_error)
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    private fun hideError(editText: EditText?, errorTextView: TextView) {
        editText?.setBackgroundResource(R.drawable.bg_input_field)
        errorTextView.visibility = View.GONE
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etEmail.hasFocus())    hideError(etEmail,    tvErrorEmail)
                if (etPassword.hasFocus()) hideError(etPassword, tvErrorPassword)
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etEmail.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
    }
}
