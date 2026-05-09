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
import androidx.constraintlayout.widget.Group
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

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvNationalId: TextView
    private lateinit var tvTotalUnits: TextView
    private lateinit var groupLandlordOnly: Group

    private var viewUserId: Int = -1
    private var loggedInUserId: Int = -1
    private var isViewingOtherUser: Boolean = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedPath = copyImageToInternalStorage(it)
            if (savedPath != null) {
                saveImagePathToPrefs(savedPath)
                userViewModel.updateProfileImage(loggedInUserId, savedPath)
                loadMyProfileImage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Homify)
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
        setContentView(R.layout.activity_profile)

        initializeViews()

        val database = AppDatabase.getDatabase(this)
        val factory = ViewModelFactory(
            application = application,
            userDao = database.userDao(),
            unitDao = database.unitDao(),
            profileDao = database.profileDao()
        )
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
        unitViewModel = ViewModelProvider(this, factory)[UnitViewModel::class.java]

        val sharedPref = getSharedPreferences(getString(R.string.userprefs), Context.MODE_PRIVATE)
        loggedInUserId = sharedPref.getInt(getString(R.string.userid), -1)

        val intentViewUserId = intent.getIntExtra("VIEW_USER_ID", -1)
        if (intentViewUserId != -1 && intentViewUserId != loggedInUserId) {
            viewUserId = intentViewUserId
            isViewingOtherUser = true
        } else {
            viewUserId = loggedInUserId
            isViewingOtherUser = false
        }

        if (viewUserId != -1) {
            setupObservers(viewUserId)
        }

        setupButtons(sharedPref)

        if (isViewingOtherUser) {
            findViewById<FloatingActionButton>(R.id.fabEditPhoto).visibility = View.GONE
            findViewById<Button>(R.id.btnLogout).visibility = View.GONE
            loadOtherUserImage(viewUserId)
        } else {
            loadMyProfileImage()
        }
    }

    private fun initializeViews() {
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvNationalId = findViewById(R.id.tvNationalId)
        tvTotalUnits = findViewById(R.id.tvTotalUnits)
        groupLandlordOnly = findViewById(R.id.groupLandlordOnly)
    }

    private fun setupObservers(userId: Int) {
        userViewModel.getUserData(userId).observe(this) { user ->
            user?.let {
                tvFullName.text = "${it.firstName} ${it.lastName}"
                tvEmail.text = it.email

                if (it.role.name.equals(getString(R.string.landlord), ignoreCase = true)) {
                    groupLandlordOnly.visibility = View.VISIBLE
                    updateUnitsCount(it.userId)
                } else {
                    groupLandlordOnly.visibility = View.GONE
                }
            }
        }

        userViewModel.getTenantProfile(userId).observe(this) { profile ->
            profile?.let {
                tvPhone.text = it.phoneNumber
                tvNationalId.text = it.nationalId
            }
        }

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
        findViewById<ImageButton>(R.id.btn_open_menu).setOnClickListener {
            val sideMenu = sideMenuFragment()
            sideMenu.show(supportFragmentManager, getString(R.string.sidemenu))
        }

        // ✅ زرار الصورة مرة واحدة بس
        findViewById<FloatingActionButton>(R.id.fabEditPhoto).setOnClickListener {
            if (!isViewingOtherUser) {
                pickImageLauncher.launch("image/*")
            }
        }

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
        if (!isViewingOtherUser) {
            loadMyProfileImage()
        }
    }

    // ✅ تحميل صورة نفسك — من SharedPrefs أو DB كـ fallback
    private fun loadMyProfileImage() {
        val ivProfileImage = findViewById<ImageView>(R.id.ivProfile)
        val sharedPref = getSharedPreferences(getString(R.string.userprefs), Context.MODE_PRIVATE)
        val localPath = sharedPref.getString(getString(R.string.profile_image_path), null)

        if (localPath != null && File(localPath).exists()) {
            loadProfileImage(localPath)
        } else if (loggedInUserId != -1) {
            // fallback من الـ DB
            userViewModel.getUserData(loggedInUserId).observe(this) { user ->
                val dbPath = user?.profileImagePath
                if (dbPath != null && File(dbPath).exists()) {
                    saveImagePathToPrefs(dbPath)
                    loadProfileImage(dbPath)
                } else {
                    ivProfileImage.setImageResource(R.drawable.ic_default_profile)
                }
            }
        } else {
            ivProfileImage.setImageResource(R.drawable.ic_default_profile)
        }
    }

    // ✅ تحميل صورة landlord من الـ DB
    private fun loadOtherUserImage(userId: Int) {
        val ivProfileImage = findViewById<ImageView>(R.id.ivProfile)
        userViewModel.getUserData(userId).observe(this) { user ->
            val dbPath = user?.profileImagePath
            if (dbPath != null && File(dbPath).exists()) {
                loadProfileImage(dbPath)
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_default_profile)
            }
        }
    }

    private fun loadProfileImage(imagePath: String) {
        val ivProfileImage = findViewById<ImageView>(R.id.ivProfile)
        val imgFile = File(imagePath)
        if (imgFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            ivProfileImage.setImageBitmap(bitmap)
        } else {
            ivProfileImage.setImageResource(R.drawable.ic_default_profile)
        }
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

    private fun saveImagePathToPrefs(path: String) {
        val sharedPref = getSharedPreferences(getString(R.string.userprefs), Context.MODE_PRIVATE)
        sharedPref.edit().putString(getString(R.string.profile_image_path), path).apply()
    }
}