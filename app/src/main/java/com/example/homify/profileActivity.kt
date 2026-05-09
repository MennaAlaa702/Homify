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
import androidx.constraintlayout.widget.Group // مهم جداً عشان ميديناش إيرور
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import data.AppDatabase
import data.viewmodel.UserViewModel
import data.viewmodel.UnitViewModel
import data.viewmodel.ViewModelFactory
import java.io.File
import java.io.FileOutputStream

class profileActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var unitViewModel: UnitViewModel

    // تعريف الـ Views كـ Global عشان نستخدمهم في الـ observation
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvNationalId: TextView
    private lateinit var tvTotalUnits: TextView

    // 👇 ركزي هنا: رجعناها Group زي ما اتفقنا في تصميم الـ Flat Hierarchy
    private lateinit var groupLandlordOnly: Group

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
        // 1. إعداد الثيم وتثبيت الشاشة
        setTheme(R.style.Theme_Homify)
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
        setContentView(R.layout.activity_profile)

        // 2. تعريف العناصر (Initialize Views)
        initializeViews()

        // 3. إعداد الـ ViewModels والداتابيز
        val database = AppDatabase.getDatabase(this)
        val factory = ViewModelFactory(
            application = application,
            userDao = database.userDao(),
            unitDao = database.unitDao(),
            profileDao = database.profileDao()
        )
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
        unitViewModel = ViewModelProvider(this, factory)[UnitViewModel::class.java]

        // 4. جلب الـ ID من الـ Session ومراقبة البيانات الحقيقية
        val sharedPref = getSharedPreferences(getString(R.string.userprefs), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt(getString(R.string.userid), -1)

        if (userId != -1) {
            setupObservers(userId)
        }

        // 5. برمجة الأزرار (Buttons Logic)
        setupButtons(sharedPref)

        // تحميل الصورة
        loadImageFromPrefs()
    }

    private fun initializeViews() {
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvNationalId = findViewById(R.id.tvNationalId)
        tvTotalUnits = findViewById(R.id.tvTotalUnits)
        groupLandlordOnly = findViewById(R.id.groupLandlordOnly) // استخدام الـ Group
    }

    private fun setupObservers(userId: Int) {
        // مراقبة بيانات المستخدم الأساسية
        userViewModel.getUserData(userId).observe(this) { user ->
            user?.let {
                tvFullName.text = "${it.firstName} ${it.lastName}"
                tvEmail.text = it.email

                // التحقق من الـ Role (بناءً على الـ Enum اللي عندك)
                if (it.role.name.equals(getString(R.string.landlord), ignoreCase = true)) {
                    groupLandlordOnly.visibility = View.VISIBLE
                    updateUnitsCount(it.userId)
                } else {
                    groupLandlordOnly.visibility = View.GONE
                }
            }
        }

        // جلب بروفايل Tenant
        userViewModel.getTenantProfile(userId).observe(this) { profile ->
            profile?.let {
                tvPhone.text = it.phoneNumber
                tvNationalId.text = it.nationalId
            }
        }

        // جلب بروفايل Landlord (لو اليوزر Landlord الداتا هتيجي هنا)
        userViewModel.getLandlordProfile(userId).observe(this) { profile ->
            profile?.let {
                tvPhone.text = it.phoneNumber
                tvNationalId.text = it.nationalId
            }
        }
    }

    private fun updateUnitsCount(landlordId: Int) {
        unitViewModel.getUnitCount(landlordId).observe(this) { count ->
            tvTotalUnits.text = "Total Units: $count"
        }
    }

    private fun setupButtons(sharedPref: android.content.SharedPreferences) {
        // زرار المنيو الجانبي
        findViewById<ImageButton>(R.id.btn_open_menu).setOnClickListener {
            val sideMenu = sideMenuFragment()
            sideMenu.show(supportFragmentManager, getString(R.string.sidemenu))
        }

        // تغيير الصورة
        findViewById<FloatingActionButton>(R.id.fabEditPhoto).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // تسجيل الخروج
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(this, onboardingActivity::class.java)
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
        // دعم لنسخة الـ Strings الأولى لو موجودة، أو نصوص مباشرة
        val prefName = getString(R.string.userprefs).takeIf { it.isNotBlank() } ?: getString(R.string.userprefs)
        val imageKey = getString(R.string.profile_image_path).takeIf { it.isNotBlank() } ?: getString(R.string.profile_image_path)

        val sharedPref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val imagePath = sharedPref.getString(imageKey, null)
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
            ivProfileImage.setImageResource(R.drawable.ic_default_profile)
        }
    }

    private fun saveImagePathToPrefs(path: String) {
        val prefName = getString(R.string.userprefs).takeIf { it.isNotBlank() } ?: "UserPrefs"
        val imageKey = getString(R.string.profile_image_path).takeIf { it.isNotBlank() } ?: "profile_image_path"

        val sharedPref = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        sharedPref.edit().putString(imageKey, path).apply()
    }
}