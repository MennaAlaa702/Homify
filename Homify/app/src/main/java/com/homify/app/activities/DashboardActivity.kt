package com.homify.app.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.homify.app.R

/**
 * DashboardActivity — the main entry screen.
 * Shows platform metrics, quick access to Manage Users and Manage Units.
 * Contains explicit intents (to ManageUsersActivity & ManageUnitsActivity)

 */
class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Setup Toolbar (App Bar)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        // ── Explicit Intent → ManageUsersActivity ──
        val btnManageUsers: MaterialButton = findViewById(R.id.btn_manage_users)
        btnManageUsers.setOnClickListener {
            val intent = Intent(this, ManageUsersActivity::class.java)
            // Pass basic metric data via putExtra
            /*intent.putExtra("TOTAL_USERS", "2,842")
            intent.putExtra("GROWTH", "+12%")*/
            startActivity(intent)
        }

        // ── Explicit Intent → ManageUnitsActivity ──
        val btnManageUnits: MaterialButton = findViewById(R.id.btn_manage_units)
        btnManageUnits.setOnClickListener {
            val intent = Intent(this, ManageUnitsActivity::class.java)
            //intent.putExtra("TOTAL_UNITS", "4,210")
            startActivity(intent)
        }

        // ── Bottom Navigation ──
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    Toast.makeText(this, getString(R.string.menu_search), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_saved -> {
                    Toast.makeText(this, getString(R.string.nav_saved), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, getString(R.string.nav_profile), Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    // ── Options Menu (App Bar) with Search + Settings ──
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
       /* return when (item.itemId) {
            R.id.menu_search -> {
                Toast.makeText(this, getString(R.string.menu_search), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_settings -> {
                // ── Implicit Intent: open Homify website ──
                /*val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.homify.com"))
                // Check if any app can handle this intent before firing
                if (webIntent.resolveActivity(packageManager) != null) {
                    startActivity(webIntent)
                } else {
                    Toast.makeText(this, getString(R.string.settings_coming_soon), Toast.LENGTH_SHORT).show()
                }*/
                Toast.makeText(this, getString(R.string.settings_coming_soon), Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }*/
    }*/
}
