package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    // تعريف الحقول
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etNationalId: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var cbTerms: CheckBox

    // تعريف رسائل الخطأ (TextViews)
    private lateinit var tvErrorFirstName: TextView
    private lateinit var tvErrorLastName: TextView
    private lateinit var tvErrorEmail: TextView
    private lateinit var tvErrorNationalId: TextView
    private lateinit var tvErrorRole: TextView
    private lateinit var tvErrorPassword: TextView
    private lateinit var tvErrorConfirmPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        // 1. ربط المتغيرات بالـ Views
        initializeViews()

        // 2. تفعيل زرار الرجوع
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // 3. تجهيز الـ Spinner
        val roles = arrayOf("Select your role", "Tenant", "Landlord")
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        // 4. مراقبة الكتابة والاختيار (عشان نمسح الإيرور لو اليوزر بدأ يعدل)
        setupTextWatchers()
        setupSpinnerWatcher()

// 5. زرار التسجيل وعملية الـ Validation
        findViewById<Button>(R.id.btn_register).setOnClickListener {
            if (validateForm()) {

                // التعديل هنا: غيرنا الـ MainActivity لـ LandlordHomeActivity
                val intent = Intent(this, LandlordHomeActivity::class.java)
                startActivity(intent)

                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 6. التعديل الجديد: اللينك بتاع الـ Log In اللي تحت في آخر الشاشة
        val tvLoginLink = findViewById<TextView>(R.id.tv_login_link)
        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // بنقفل الشاشة دي عشان لو داس باك ميرجعش للـ Register وهو عامل Log In
        }
    }

    private fun initializeViews() {
        // الحقول
        etFirstName = findViewById(R.id.et_first_name)
        etLastName = findViewById(R.id.et_last_name)
        etEmail = findViewById(R.id.et_email)
        etNationalId = findViewById(R.id.et_national_id)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        spinnerRole = findViewById(R.id.spinner_role)
        cbTerms = findViewById(R.id.cb_terms)

        // رسائل الخطأ
        tvErrorFirstName = findViewById(R.id.tv_error_first_name)
        tvErrorLastName = findViewById(R.id.tv_error_last_name)
        tvErrorEmail = findViewById(R.id.tv_error_email)
        tvErrorNationalId = findViewById(R.id.tv_error_national_id)
        tvErrorRole = findViewById(R.id.tv_error_role)
        tvErrorPassword = findViewById(R.id.tv_error_password)
        tvErrorConfirmPassword = findViewById(R.id.tv_error_confirm_password)
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val nationalId = etNationalId.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // 1. First Name Validation
        if (firstName.isEmpty()) {
            showError(etFirstName, tvErrorFirstName, getString(R.string.err_first_name_req))
            isValid = false
        }

        // 2. Last Name Validation
        if (lastName.isEmpty()) {
            showError(etLastName, tvErrorLastName, getString(R.string.err_last_name_req))
            isValid = false
        }

        // 3. Email Validation
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvErrorEmail, getString(R.string.err_email_invalid))
            isValid = false
        }

        // 4. National ID Validation
        if (nationalId.isEmpty()) {
            showError(etNationalId, tvErrorNationalId, getString(R.string.err_nid_req))
            isValid = false
        } else if (nationalId.length != 14) {
            showError(etNationalId, tvErrorNationalId, getString(R.string.err_nid_length))
            isValid = false
        }

        // 5. Role Validation (الـ Spinner ملوش Border بيحمر، بنظهر الرسالة بس)
        if (spinnerRole.selectedItemPosition == 0) {
            tvErrorRole.text = getString(R.string.err_role_req)
            tvErrorRole.visibility = View.VISIBLE
            isValid = false
        }

        // 6. Password Validation
        if (password.isEmpty()) {
            showError(etPassword, tvErrorPassword, getString(R.string.err_pass_req))
            isValid = false
        }

        // 7. Confirm Password Validation
        if (confirmPassword.isEmpty()) {
            showError(etConfirmPassword, tvErrorConfirmPassword, getString(R.string.err_confirm_pass_req))
            isValid = false
        } else if (password != confirmPassword) {
            showError(etConfirmPassword, tvErrorConfirmPassword, getString(R.string.err_pass_match))
            isValid = false
        }

        // 8. Terms Validation
        if (!cbTerms.isChecked) {
            Toast.makeText(this, "You must agree to the Terms of Service", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
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
                // لما اليوزر يكتب في أي حقل، بنخفي الإيرور بتاعه
                if (etFirstName.hasFocus()) hideError(etFirstName, tvErrorFirstName)
                if (etLastName.hasFocus()) hideError(etLastName, tvErrorLastName)
                if (etEmail.hasFocus()) hideError(etEmail, tvErrorEmail)
                if (etNationalId.hasFocus()) hideError(etNationalId, tvErrorNationalId)
                if (etPassword.hasFocus()) hideError(etPassword, tvErrorPassword)
                if (etConfirmPassword.hasFocus()) hideError(etConfirmPassword, tvErrorConfirmPassword)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etFirstName.addTextChangedListener(watcher)
        etLastName.addTextChangedListener(watcher)
        etEmail.addTextChangedListener(watcher)
        etNationalId.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)
        etConfirmPassword.addTextChangedListener(watcher)
    }

    private fun setupSpinnerWatcher() {
        spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // لو اختار حاجة غير الصفر (يعني اختار دور بجد)، نخفي الإيرور
                if (position > 0) {
                    tvErrorRole.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}