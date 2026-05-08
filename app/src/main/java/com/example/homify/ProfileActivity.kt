package com.example.homify

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedPath = copyImageToInternalStorage(it)
            if (savedPath != null) {
                saveImagePathToPrefs(savedPath)
                loadImageFromPrefs()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. إعداد الثيم وشاشة البداية أول حاجة
        val splashScreen = installSplashScreen()
        setTheme(R.style.Theme_Homify)
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
        // 2. أهم سطر: رسم الشاشة قبل ما نربط أي عنصر
        setContentView(R.layout.activity_profile)

// 1. تعريف زرار المنيو
        val btnOpenMenu: ImageButton = findViewById(R.id.btn_open_menu)

// 2. عند الضغط عليه يظهر الفريجمنت
        btnOpenMenu.setOnClickListener {
            val sideMenu = SideMenuFragment()
            sideMenu.show(supportFragmentManager, "SideMenu")
        }

        // باقي عناصر الشاشة الأساسية
        val tvFullName = findViewById<TextView>(R.id.tvFullName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvPhone = findViewById<TextView>(R.id.tvPhone)
        val tvNationalId = findViewById<TextView>(R.id.tvNationalId)
        val tvTotalUnits = findViewById<TextView>(R.id.tvTotalUnits)

        // استخدام الأنواع الصحيحة المتوافقة مع الـ XML المسطح الجديد
        val layoutLandlordOnly = findViewById<ConstraintLayout>(R.id.layoutLandlordOnly)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val btnEditPhoto = findViewById<FloatingActionButton>(R.id.fabEditPhoto)

        // =========================================================
        // 4. قراءة البيانات من الذاكرة (SharedPreferences)
        // =========================================================
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        tvFullName.text = sharedPref.getString("username", "User Name")
        tvEmail.text = sharedPref.getString("email", "email@example.com")
        tvPhone.text = sharedPref.getString("phone", "Not Provided")
        tvNationalId.text = sharedPref.getString("nationalId", "00000000000000")

        val userRole = sharedPref.getString("role", "tenant") // القيمة الافتراضية
        val unitsCount = sharedPref.getString("unitsCount", "0")

        // إظهار أو إخفاء قسم الـ Landlord بناءً على نوع المستخدم
        if (userRole.equals("landlord", ignoreCase = true)) {
            layoutLandlordOnly.visibility = View.VISIBLE
            tvTotalUnits.text = "Total Units: $unitsCount"
        } else {
            layoutLandlordOnly.visibility = View.GONE
        }

        // تحميل صورة البروفايل لو موجودة
        loadImageFromPrefs()

        // =========================================================
        // 5. برمجة الأزرار
        // =========================================================

        btnEditPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        logoutButton.setOnClickListener {
            // مسح بيانات الدخول
            sharedPref.edit().clear().apply()

            // التوجيه لشاشة البداية
            val intent = Intent(this, OnboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadImageFromPrefs()
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "profile_picture.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadImageFromPrefs() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val imagePath = sharedPref.getString("profile_image_path", null)
        val ivProfileImage = findViewById<ImageView>(R.id.ivProfile)

        if (imagePath != null) {
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                ivProfileImage.setImageBitmap(bitmap)
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_default_profile)
            }
        } else {
            // لو مفيش صورة محفوظة، اعرض الديفولت
            ivProfileImage.setImageResource(R.drawable.ic_default_profile)
        }
    }

    private fun saveImagePathToPrefs(path: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("profile_image_path", path).apply()
    }
}