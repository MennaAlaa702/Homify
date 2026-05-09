package com.example.homify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import data.viewmodel.AdminViewModel
import data.viewmodel.ViewModelFactory

/**
 * ManageUsersActivity — lists all platform users with delete functionality.
 * Receives data from DashboardActivity via Intent extras.
 * Uses RecyclerView + UserAdapter for dynamic list rendering.
 */
class manageUsersActivity : AppCompatActivity() {

    private lateinit var adapter: userAdapter
    private var allUsers: MutableList<users> = mutableListOf()
    private lateinit var adminViewModel: AdminViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        // 1. تثبيت اتجاه الشاشة (من النسخة الأولى)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // 2. إعداد الـ Toolbar وسهم الرجوع
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Receive extras from DashboardActivity (اختياري لو هتعرضيهم)
        val totalUsers = intent.getStringExtra(getString(R.string.total__units)) ?: getString(R.string._2_842)
        val growth = intent.getStringExtra(getString(R.string.growth)) ?: "+12%"

        // 3. إعداد الـ DB والـ ViewModel (من النسخة التانية)
        val db = (application as homifyApp).database
        val factory = ViewModelFactory(
            application = application,
            userDao = db.userDao(),
            unitDao = db.unitDao()
        )
        adminViewModel = ViewModelProvider(this, factory)[AdminViewModel::class.java]

        // 4. إعداد الـ RecyclerView
        val rvUsers: RecyclerView = findViewById(R.id.rv_users)
        adapter = userAdapter(allUsers.toMutableList()) { user, position ->
            showDeleteUserDialog(user, position)
        }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        // 5. جلب المستخدمين الحقيقيين من الداتابيز ومراقبتهم
        adminViewModel.allUsers.observe(this) { userList ->
            allUsers.clear()
            allUsers.addAll(userList.map { user ->
                users(
                    id          = user.userId,
                    name        = "${user.firstName} ${user.lastName}",
                    email       = user.email,
                    avatarResId = R.drawable.ic_person // صورة ديفولت لكل يوزر
                )
            })
            // تحديث الشاشة بعد جلب الداتا
            adapter.filter("", allUsers)
        }

        // 6. كود البحث السريع (Quick Search)
        val etSearch: EditText = findViewById(R.id.et_search_users)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString(), allUsers)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 7. زرار إضافة مستخدم جديد
        val btnAddUser: MaterialButton = findViewById(R.id.btn_add_new_user)
        btnAddUser.setOnClickListener {
            val intent = Intent(this, registerActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Show an AlertDialog to confirm user deletion before removing.
     */
    private fun showDeleteUserDialog(users: users, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_delete))
            .setMessage(getString(R.string.confirm_delete_user_msg))
            .setPositiveButton(getString(R.string.btn_confirm)) { dialog, _ ->
                // المسح الحقيقي من الداتابيز (والـ observer هيحدث الشاشة لوحده)
                adminViewModel.removeUserById(users.id)
                Toast.makeText(this, getString(R.string.toast_user_deleted), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // ── Options Menu ──
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
}