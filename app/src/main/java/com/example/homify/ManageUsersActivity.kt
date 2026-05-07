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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton


/**
 * ManageUsersActivity — lists all platform users with delete functionality.
 * Receives data from DashboardActivity via Intent extras.
 * Uses RecyclerView + UserAdapter for dynamic list rendering.
 */
class ManageUsersActivity : AppCompatActivity() {

    private lateinit var adapter: UserAdapter
    private lateinit var allUsers: MutableList<Users>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

// تفعيل سهم الرجوع
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

// برمجة زرار السهم
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Receive extras from DashboardActivity (Explicit Intent data)
        val totalUsers = intent.getStringExtra("TOTAL_USERS") ?: "2,842"
        val growth = intent.getStringExtra("GROWTH") ?: "+12%"

        // ── Sample user data ──
        allUsers = mutableListOf(
            Users(1, "Sarah Miller",  "sarah.m@university.edu",  android.R.drawable.ic_menu_myplaces),
            Users(2, "Marcus Chen",   "m.chen@homify.app",        android.R.drawable.ic_menu_myplaces),
            Users(3, "Leila Ahmed",   "leila.ahmed@campus.com",   android.R.drawable.ic_menu_myplaces),
            Users(4, "James O'Neill", "j.oneill@housing.org",     android.R.drawable.ic_menu_myplaces)
        )

        // ── RecyclerView Setup ──
        val rvUsers: RecyclerView = findViewById(R.id.rv_users)
        adapter = UserAdapter(allUsers.toMutableList()) { user, position ->
            showDeleteUserDialog(user, position)
        }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        // ── Quick Search ──
        val etSearch: EditText = findViewById(R.id.et_search_users)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString(), allUsers)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ── Add New User Button ──
        val btnAddUser: MaterialButton = findViewById(R.id.btn_add_new_user)
        btnAddUser.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    /**
     * Show an AlertDialog to confirm user deletion before removing.
     */
    private fun showDeleteUserDialog(users: Users, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_delete))
            .setMessage(getString(R.string.confirm_delete_user_msg))
            .setPositiveButton(getString(R.string.btn_confirm)) { dialog, _ ->
                adapter.removeUser(position)
                allUsers.remove(users)
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

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_search -> {
                Toast.makeText(this, getString(R.string.menu_search), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_settings -> {
                Toast.makeText(this, getString(R.string.settings_coming_soon), Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/
}
