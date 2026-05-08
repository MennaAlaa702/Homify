package com.example.homify

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddUnitActivity : AppCompatActivity() {

    // ================= 1. تعريف المتغيرات (Views) =================
    private lateinit var etUnitTitle: EditText
    private lateinit var tvErrUnitTitle: TextView

    private lateinit var etDesc: EditText
    private lateinit var tvErrDesc: TextView

    private lateinit var spinnerType: Spinner
    private lateinit var tvErrType: TextView

    private lateinit var etPrice: EditText
    private lateinit var tvErrPrice: TextView

    private lateinit var etSize: EditText
    private lateinit var etBeds: EditText
    private lateinit var etBaths: EditText

    private lateinit var etAmenities: EditText
    private lateinit var tvErrAmenities: TextView

    private lateinit var etGovernorate: EditText
    private lateinit var tvErrGov: TextView

    private lateinit var etAddress: EditText
    private lateinit var tvErrAddress: TextView

    private lateinit var etLocation: EditText
    private lateinit var tvErrLocation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_unit)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
        // ================= 2. استدعاء الدوال التجهيزية =================
        initializeViews()
        setupSpinner()
        setupTextWatchers()

        // ================= 3. برمجة الزراير =================

        // زرار الرجوع اللي فوق
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // زرار Cancel اللي تحت
        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            finish()
        }

        // زرار Save Unit
        findViewById<Button>(R.id.btn_save).setOnClickListener {
            if (validateForm()) {
                // لو البيانات كلها صح، هيطلع رسالة نجاح ويقفل الصفحة
                // (لما تيجوا تربطوا بالباك إند، الكود بتاع الرفع هيتكتب هنا)
                Toast.makeText(this, "Unit Added Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // ================= 4. دالة ربط المتغيرات بملف الـ XML =================
    private fun initializeViews() {
        etUnitTitle = findViewById(R.id.et_unit_title)
        tvErrUnitTitle = findViewById(R.id.tv_err_unit_title)

        etDesc = findViewById(R.id.et_desc)
        tvErrDesc = findViewById(R.id.tv_err_desc)

        spinnerType = findViewById(R.id.spinner_type)
        tvErrType = findViewById(R.id.tv_err_type)

        etPrice = findViewById(R.id.et_price)
        tvErrPrice = findViewById(R.id.tv_err_price)

        etSize = findViewById(R.id.et_size)
        etBeds = findViewById(R.id.et_beds)
        etBaths = findViewById(R.id.et_baths)

        etAmenities = findViewById(R.id.et_amenities)
        tvErrAmenities = findViewById(R.id.tv_err_amenities)

        etGovernorate = findViewById(R.id.et_governorate)
        tvErrGov = findViewById(R.id.tv_err_gov)

        etAddress = findViewById(R.id.et_address)
        tvErrAddress = findViewById(R.id.tv_err_address)

        etLocation = findViewById(R.id.et_location)
        tvErrLocation = findViewById(R.id.tv_err_location)
    }

    // ================= 5. دالة تجهيز قائمة الأنواع (Spinner) =================
    private fun setupSpinner() {
        val types = arrayOf(
            "Select Unit Type",
            getString(R.string.category_near_uni),
            getString(R.string.category_studio),
            getString(R.string.category_villa),
            getString(R.string.category_apartment),
            getString(R.string.category_shared)
        )

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (position > 0) tvErrType.visibility = View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ================= 6. دالة التحقق من صحة البيانات (Validation) =================
    private fun validateForm(): Boolean {
        var isValid = true
        val reqMsg = getString(R.string.err_field_req)

        if (etUnitTitle.text.toString().trim().isEmpty()) { showError(etUnitTitle, tvErrUnitTitle, reqMsg); isValid = false }
        if (etDesc.text.toString().trim().isEmpty()) { showError(etDesc, tvErrDesc, reqMsg); isValid = false }

        if (spinnerType.selectedItemPosition == 0) {
            tvErrType.text = reqMsg
            tvErrType.visibility = View.VISIBLE
            isValid = false
        }

        if (etPrice.text.toString().trim().isEmpty()) { showError(etPrice, tvErrPrice, reqMsg); isValid = false }

        // Specifications (بنحمر البوردر بس من غير رسالة تحتهم عشان المساحة)
        if (etSize.text.toString().trim().isEmpty()) { etSize.setBackgroundResource(R.drawable.bg_input_error); isValid = false }
        if (etBeds.text.toString().trim().isEmpty()) { etBeds.setBackgroundResource(R.drawable.bg_input_error); isValid = false }
        if (etBaths.text.toString().trim().isEmpty()) { etBaths.setBackgroundResource(R.drawable.bg_input_error); isValid = false }

        if (etAmenities.text.toString().trim().isEmpty()) { showError(etAmenities, tvErrAmenities, reqMsg); isValid = false }
        if (etGovernorate.text.toString().trim().isEmpty()) { showError(etGovernorate, tvErrGov, reqMsg); isValid = false }
        if (etAddress.text.toString().trim().isEmpty()) { showError(etAddress, tvErrAddress, reqMsg); isValid = false }
        if (etLocation.text.toString().trim().isEmpty()) { showError(etLocation, tvErrLocation, reqMsg); isValid = false }

        return isValid
    }

    // ================= 7. دوال إظهار وإخفاء الأخطاء =================
    private fun showError(editText: EditText, errorTextView: TextView, message: String) {
        editText.setBackgroundResource(R.drawable.bg_input_error)
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    private fun hideError(editText: EditText, errorTextView: TextView?) {
        editText.setBackgroundResource(R.drawable.bg_input_field)
        errorTextView?.visibility = View.GONE
    }

    // ================= 8. دالة مراقبة الكتابة (عشان نمسح الإيرور أول ما اليوزر يكتب) =================
    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etUnitTitle.hasFocus()) hideError(etUnitTitle, tvErrUnitTitle)
                if (etDesc.hasFocus()) hideError(etDesc, tvErrDesc)
                if (etPrice.hasFocus()) hideError(etPrice, tvErrPrice)
                if (etSize.hasFocus()) hideError(etSize, null)
                if (etBeds.hasFocus()) hideError(etBeds, null)
                if (etBaths.hasFocus()) hideError(etBaths, null)
                if (etAmenities.hasFocus()) hideError(etAmenities, tvErrAmenities)
                if (etGovernorate.hasFocus()) hideError(etGovernorate, tvErrGov)
                if (etAddress.hasFocus()) hideError(etAddress, tvErrAddress)
                if (etLocation.hasFocus()) hideError(etLocation, tvErrLocation)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etUnitTitle.addTextChangedListener(watcher)
        etDesc.addTextChangedListener(watcher)
        etPrice.addTextChangedListener(watcher)
        etSize.addTextChangedListener(watcher)
        etBeds.addTextChangedListener(watcher)
        etBaths.addTextChangedListener(watcher)
        etAmenities.addTextChangedListener(watcher)
        etGovernorate.addTextChangedListener(watcher)
        etAddress.addTextChangedListener(watcher)
        etLocation.addTextChangedListener(watcher)
    }
}