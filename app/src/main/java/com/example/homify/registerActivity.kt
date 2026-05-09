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
import data.entities.User
import data.entities.UserRole
import data.entities.LandlordProfile
import data.entities.TenantProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class registerActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etNationalId: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etPhone: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var cbTerms: CheckBox

    private lateinit var tvErrorFirstName: TextView
    private lateinit var tvErrorLastName: TextView
    private lateinit var tvErrorEmail: TextView
    private lateinit var tvErrorNationalId: TextView
    private lateinit var tvErrorRole: TextView
    private lateinit var tvErrorPassword: TextView
    private lateinit var tvErrorConfirmPassword: TextView
    private lateinit var tvErrorPhone: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, onboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        val roles = arrayOf("Select your role", "Tenant", "Landlord")
        spinnerRole.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        setupTextWatchers()
        setupSpinnerWatcher()

        findViewById<Button>(R.id.btn_register).setOnClickListener {
            if (validateForm()) {
                registerUser()
            }
        }

        findViewById<TextView>(R.id.tv_login_link).setOnClickListener {
            startActivity(Intent(this, loginActivity::class.java))
            finish()
        }
    }

    // ════════════════════════════════════════════
    //  حفظ المستخدم في قاعدة البيانات
    // ════════════════════════════════════════════
    private fun registerUser() {
        val db       = (application as homifyApp).database
        val userDao  = db.userDao()
        val profileDao = db.profileDao()

        val firstName       = etFirstName.text.toString().trim()
        val lastName        = etLastName.text.toString().trim()
        val email           = etEmail.text.toString().trim()
        val nationalId      = etNationalId.text.toString().trim()
        val phone           = etPhone.text.toString().trim()
        val password        = etPassword.text.toString()
        val selectedRole    = spinnerRole.selectedItem.toString()

        val role = if (selectedRole.equals("Landlord", ignoreCase = true))
            UserRole.landlord else UserRole.tenant

        // تشغيل في الـ background thread عشان Room مش بيشتغل على الـ Main thread
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. تحقق إن الإيميل مش مكرر
                val existing = userDao.getUserByEmail(email)
                if (existing != null) {
                    withContext(Dispatchers.Main) {
                        showError(etEmail, tvErrorEmail, "Email already registered")
                    }
                    return@launch
                }

                // 2. احفظ المستخدم في جدول users
                val newUser = User(
                    firstName  = firstName,
                    lastName   = lastName,
                    email      = email,
                    national_id = nationalId,
                    password   = password,
                    role       = role
                )
                val userId = userDao.insertUser(newUser).toInt()

                // 3. احفظ البروفايل المناسب حسب الدور
                if (role == UserRole.landlord) {
                    profileDao.insertLandlordProfile(
                        LandlordProfile(
                            userId      = userId,
                            firstName   = firstName,
                            lastName    = lastName,
                            nationalId  = nationalId,
                            email       = email,
                            phoneNumber = phone
                        )
                    )
                } else {
                    profileDao.insertTenantProfile(
                        TenantProfile(
                            userId      = userId,
                            firstName   = firstName,
                            lastName    = lastName,
                            phoneNumber = phone,
                            email       = email,
                            nationalId  = nationalId
                        )
                    )
                }

                // 4. احفظ بيانات الجلسة في SharedPreferences
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                sharedPref.edit().apply {
                    putInt("userId", userId)
                    putString("username", "$firstName $lastName")
                    putString("email", email)
                    putString("phone", phone)
                    putString("nationalId", nationalId)
                    putString("role", selectedRole)
                    apply()
                }

                // 5. انتقل للشاشة الصح بناءً على الدور
                withContext(Dispatchers.Main) {
                    val intent = if (role == UserRole.landlord) {
                        Intent(this@registerActivity, landlordHomeActivity::class.java)
                    } else {
                        Intent(this@registerActivity, tenantHome::class.java)
                    }
                    startActivity(intent)
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@registerActivity,
                        "Registration failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ════════════════════════════════════════════
    //  Validation
    // ════════════════════════════════════════════
    private fun validateForm(): Boolean {
        var isValid = true
        val firstName       = etFirstName.text.toString().trim()
        val lastName        = etLastName.text.toString().trim()
        val email           = etEmail.text.toString().trim()
        val nationalId      = etNationalId.text.toString().trim()
        val phone           = etPhone.text.toString().trim()
        val password        = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // === First Name Validation ===
        if (firstName.isEmpty()) {
            showError(etFirstName, tvErrorFirstName, getString(R.string.err_first_name_req))
            isValid = false
        } else if (firstName.length < 3) {
            showError(etFirstName, tvErrorFirstName, "First name must be at least 3 characters")
            isValid = false
        }

        // === Last Name Validation ===
        if (lastName.isEmpty()) {
            showError(etLastName, tvErrorLastName, getString(R.string.err_last_name_req))
            isValid = false
        } else if (lastName.length < 3) {
            showError(etLastName, tvErrorLastName, "Last name must be at least 3 characters")
            isValid = false
        }

        // === Email Validation ===
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvErrorEmail, getString(R.string.err_email_invalid)); isValid = false
        }

        // === National ID Validation ===
        if (nationalId.isEmpty()) {
            showError(etNationalId, tvErrorNationalId, getString(R.string.err_nid_req)); isValid = false
        } else if (nationalId.length != 14) {
            showError(etNationalId, tvErrorNationalId, getString(R.string.err_nid_length)); isValid = false
        }

        // === Role Validation ===
        if (spinnerRole.selectedItemPosition == 0) {
            tvErrorRole.text = getString(R.string.err_role_req); tvErrorRole.visibility = View.VISIBLE; isValid = false
        }

        // === Phone Validation ===
        if (phone.isEmpty()) {
            tvErrorPhone.text = getString(R.string.err_role_req); tvErrorPhone.visibility = View.VISIBLE; isValid = false
        } else if (phone.length != 11) {
            tvErrorPhone.text = "Phone must be 11 digits"; tvErrorPhone.visibility = View.VISIBLE; isValid = false
        } else {
            tvErrorPhone.visibility = View.GONE
        }

        // === Strong Password Validation ===
        // Pattern: 8+ chars, at least 1 uppercase, 1 lowercase, 1 number, and 1 special character
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_\\-]).{8,}$".toRegex()

        if (password.isEmpty()) {
            showError(etPassword, tvErrorPassword, getString(R.string.err_pass_req))
            isValid = false
        } else if (!password.matches(passwordPattern)) {
            showError(etPassword, tvErrorPassword, "Weak password! Use 8+ chars, upper/lower case, numbers & symbols")
            isValid = false
        }

        // === Confirm Password Validation ===
        if (confirmPassword.isEmpty()) {
            showError(etConfirmPassword, tvErrorConfirmPassword, getString(R.string.err_confirm_pass_req)); isValid = false
        } else if (password != confirmPassword) {
            showError(etConfirmPassword, tvErrorConfirmPassword, getString(R.string.err_pass_match)); isValid = false
        }

        // === Terms Checkbox ===
        if (!cbTerms.isChecked) {
            Toast.makeText(this, "You must agree to the Terms of Service", Toast.LENGTH_SHORT).show(); isValid = false
        }

        return isValid
    }

    private fun initializeViews() {
        etFirstName         = findViewById(R.id.et_first_name)
        etLastName          = findViewById(R.id.et_last_name)
        etEmail             = findViewById(R.id.et_email)
        etNationalId        = findViewById(R.id.et_national_id)
        etPassword          = findViewById(R.id.et_password)
        etPhone             = findViewById(R.id.et_phone)
        etConfirmPassword   = findViewById(R.id.et_confirm_password)
        spinnerRole         = findViewById(R.id.spinner_role)
        cbTerms             = findViewById(R.id.cb_terms)
        tvErrorFirstName    = findViewById(R.id.tv_error_first_name)
        tvErrorLastName     = findViewById(R.id.tv_error_last_name)
        tvErrorEmail        = findViewById(R.id.tv_error_email)
        tvErrorNationalId   = findViewById(R.id.tv_error_national_id)
        tvErrorRole         = findViewById(R.id.tv_error_role)
        tvErrorPhone        = findViewById(R.id.tv_error_phone)
        tvErrorPassword     = findViewById(R.id.tv_error_password)
        tvErrorConfirmPassword = findViewById(R.id.tv_error_confirm_password)
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
                if (etFirstName.hasFocus())       hideError(etFirstName, tvErrorFirstName)
                if (etLastName.hasFocus())        hideError(etLastName,  tvErrorLastName)
                if (etEmail.hasFocus())           hideError(etEmail,     tvErrorEmail)
                if (etNationalId.hasFocus())      hideError(etNationalId, tvErrorNationalId)
                if (etPassword.hasFocus())        hideError(etPassword,  tvErrorPassword)
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
                if (position > 0) tvErrorRole.visibility = View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}