package com.homify.app.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.homify.app.R
import com.homify.app.adapters.UserAdapter
import com.homify.app.models.User

/**
 * ManageUsersActivity — lists all platform users with delete functionality.
 * Receives data from DashboardActivity via Intent extras.
 * Uses RecyclerView + UserAdapter for dynamic list rendering.
 */
class ManageUsersActivity : AppCompatActivity() {

    private lateinit var adapter: UserAdapter
    private lateinit var allUsers: MutableList<User>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.manage_users_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Receive extras from DashboardActivity (Explicit Intent data)
        val totalUsers = intent.getStringExtra("TOTAL_USERS") ?: "2,842"
        val growth = intent.getStringExtra("GROWTH") ?: "+12%"

        // ── Sample user data ──
        allUsers = mutableListOf(
            User(1, "Sarah Miller",  "sarah.m@university.edu",  android.R.drawable.ic_menu_myplaces),
            User(2, "Marcus Chen",   "m.chen@homify.app",        android.R.drawable.ic_menu_myplaces),
            User(3, "Leila Ahmed",   "leila.ahmed@campus.com",   android.R.drawable.ic_menu_myplaces),
            User(4, "James O'Neill", "j.oneill@housing.org",     android.R.drawable.ic_menu_myplaces)
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
            // In a real app this would open an Add User form/dialog
            Toast.makeText(this, getString(R.string.toast_user_added), Toast.LENGTH_SHORT).show()
        }

        // ── Bottom Navigation ──
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish() // return to Dashboard
                    true
                }
                R.id.nav_search -> {
                    Toast.makeText(this, getString(R.string.menu_search), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_saved -> {
                    Toast.makeText(this, getString(R.string.nav_saved), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    /**
     * Show an AlertDialog to confirm user deletion before removing.
     */
    private fun showDeleteUserDialog(user: User, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_delete))
            .setMessage(getString(R.string.confirm_delete_user_msg))
            .setPositiveButton(getString(R.string.btn_confirm)) { dialog, _ ->
                adapter.removeUser(position)
                allUsers.remove(user)
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
