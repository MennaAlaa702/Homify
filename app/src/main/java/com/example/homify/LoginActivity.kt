package com.example.homify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    // تعريف الحقول
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    // تعريف رسائل الخطأ (TextViews)
    private lateinit var tvErrorEmail: TextView
    private lateinit var tvErrorPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
        // 1. ربط المتغيرات بالـ Views
        initializeViews()

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvRegisterLink = findViewById<TextView>(R.id.tv_register_link)

        // 2. مراقبة الكتابة (عشان نمسح الإيرور لو اليوزر بدأ يكتب)
        setupTextWatchers()

        // 3. زرار الرجوع اللي في الـ App Bar
        btnBack.setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // 4. زرار اللوجين وعملية الـ Validation والتوجيه الذكي
        btnLogin.setOnClickListener {
            if (validateForm()) {
                val email = etEmail.text.toString().trim()

                // بنقرأ الـ Role اللي اتسجل في الـ SharedPreferences
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                var userRole = sharedPref.getString("role", "Tenant") // بيفترض إنه Tenant لو ملقاش حاجة

                // خدعة عشان ندخل كـ Admin (لأن مفيش أدمن في شاشة التسجيل)
                if (email.equals("admin@gmail.com", ignoreCase = true)) {
                    userRole = "Admin"
                }

                // التوجيه بناءً على الرول
                val intent = when {
                    userRole.equals("Admin", ignoreCase = true) -> Intent(this, DashboardActivity::class.java)
                    userRole.equals("Landlord", ignoreCase = true) -> Intent(this, LandlordHomeActivity::class.java)
                    else -> Intent(this, TenantHome::class.java)
                }

                startActivity(intent)
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 5. اللينك بتاع التسجيل
        tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)

        tvErrorEmail = findViewById(R.id.tv_error_email)
        tvErrorPassword = findViewById(R.id.tv_error_password)
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        // 1. Email Validation
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvErrorEmail, getString(R.string.err_email_invalid))
            isValid = false
        }

        // 2. Password Validation
        if (password.isEmpty()) {
            showError(etPassword, tvErrorPassword, getString(R.string.err_pass_req))
            isValid = false
        }

        return isValid
    }

    // الدالة دي بتغير الخلفية للأحمر وبتظهر الرسالة
    private fun showError(editText: EditText?, errorTextView: TextView, message: String) {
        editText?.setBackgroundResource(R.drawable.bg_input_error)
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    // الدالة دي بترجع الخلفية للطبيعي وبتخفي الرسالة
    private fun hideError(editText: EditText?, errorTextView: TextView) {
        editText?.setBackgroundResource(R.drawable.bg_input_field)
        errorTextView.visibility = View.GONE
    }

    // بنراقب لو اليوزر بدأ يكتب عشان نخفي الإيرور
    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etEmail.hasFocus()) hideError(etEmail, tvErrorEmail)
                if (etPassword.hasFocus()) hideError(etPassword, tvErrorPassword)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etEmail.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
    }
}