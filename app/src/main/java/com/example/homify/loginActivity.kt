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
import androidx.lifecycle.lifecycleScope
import data.entities.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class loginActivity : AppCompatActivity() {

    // تعريف الحقول
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    // تعريف رسائل الخطأ (TextViews)
    private lateinit var tvErrorEmail: TextView
    private lateinit var tvErrorPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // تثبيت اتجاه الشاشة من النسخة الأولى
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // 1. ربط المتغيرات بالـ Views
        initializeViews()

        // 2. مراقبة الكتابة (عشان نمسح الإيرور لو اليوزر بدأ يكتب)
        setupTextWatchers()

        // 3. زرار الرجوع اللي في الـ App Bar
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, onboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // 4. زرار اللوجين وعملية الـ Validation
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            if (validateForm()) {
                loginUser() // استدعاء دالة الداتابيز الحقيقية
            }
        }

        // 5. اللينك بتاع التسجيل
        findViewById<TextView>(R.id.tv_register_link).setOnClickListener {
            startActivity(Intent(this, registerActivity::class.java))
            finish()
        }
    }

    // ════════════════════════════════════════════
    //  التحقق من بيانات اللوجين من قاعدة البيانات (من النسخة التانية)
    // ════════════════════════════════════════════
    private fun loginUser() {
        val db      = (application as homifyApp).database
        val userDao = db.userDao()

        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            // حالة خاصة للأدمن (مش موجود في شاشة التسجيل)
            val isAdmin = email.equals(getString(R.string.admin_email), ignoreCase = true)

            val user = userDao.getUserByEmail(email)

            withContext(Dispatchers.Main) {
                when {
                    // ── أدمن ──
                    isAdmin && user != null && user.password == password -> {
                        saveSession(user.userId, "${user.firstName} ${user.lastName}", email, getString(R.string.admin))
                        Toast.makeText(this@loginActivity, getString(R.string.welcome_back), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@loginActivity, dashboardActivity::class.java))
                        finish()
                    }
                    // ── مستخدم عادي موجود وباسورده صح ──
                    user != null && user.password == password -> {
                        val roleStr = if (user.role == UserRole.landlord) getString(R.string.landlord) else getString(R.string.tenant)
                        saveSession(user.userId, "${user.firstName} ${user.lastName}", email, roleStr)

                        val intent = if (user.role == UserRole.landlord) {
                            Intent(this@loginActivity, landlordHomeActivity::class.java)
                        } else {
                            Intent(this@loginActivity, tenantHome::class.java)
                        }
                        Toast.makeText(this@loginActivity, getString(R.string.welcome_back), Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
                    }
                    // ── الإيميل مش موجود ──
                    user == null -> {
                        showError(etEmail, tvErrorEmail, getString(R.string.email_not_found))
                    }
                    // ── الباسورد غلط ──
                    else -> {
                        showError(etPassword, tvErrorPassword,
                            getString(R.string.incorrect_password))
                    }
                }
            }
        }
    }

    // ════════════════════════════════════════════
    //  حفظ بيانات الجلسة في SharedPreferences
    // ════════════════════════════════════════════
    private fun saveSession(userId: Int, username: String, email: String, role: String) {
        val sharedPref = getSharedPreferences(getString(R.string.userprefs), Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putInt(getString(R.string.userid), userId)
            putString(getString(R.string.username), username)
            putString(getString(R.string.email), email)
            putString(getString(R.string.role), role)
            apply()
        }
    }

    // ════════════════════════════════════════════
    //  Validation & UI Helpers
    // ════════════════════════════════════════════
    private fun validateForm(): Boolean {
        var isValid = true

        val email    = etEmail.text.toString().trim()
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

    private fun initializeViews() {
        etEmail         = findViewById(R.id.et_email)
        etPassword      = findViewById(R.id.et_password)
        tvErrorEmail    = findViewById(R.id.tv_error_email)
        tvErrorPassword = findViewById(R.id.tv_error_password)
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
                if (etEmail.hasFocus())    hideError(etEmail,    tvErrorEmail)
                if (etPassword.hasFocus()) hideError(etPassword, tvErrorPassword)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etEmail.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
    }
}