package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton

/**
 * DashboardActivity — the main entry screen for Admin.
 * Shows platform metrics, quick access to Manage Users and Manage Units.
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
            startActivity(intent)
        }

        // ── Explicit Intent → ManageUnitsActivity ──
        val btnManageUnits: MaterialButton = findViewById(R.id.btn_manage_units)
        btnManageUnits.setOnClickListener {
            val intent = Intent(this, ManageUnitsActivity::class.java)
            startActivity(intent)
        }
    } // <--- القوس ده اللي كان ناقص ومطير الكود كله!


    // دالة البحث الذكي (بتدور على النص وتعمل سكرول عنده)
    private fun searchInDashboard(query: String?) {
        if (query.isNullOrEmpty()) return

        // بنمسك الـ ScrollView والحاوية اللي جواه
        val scrollView = findViewById<android.widget.ScrollView>(R.id.scrollView)
        val container = scrollView.getChildAt(0) as? androidx.constraintlayout.widget.ConstraintLayout ?: return

        val lowerQuery = query.lowercase()

        // بنلف على كل العناصر اللي جوه الشاشة واحد واحد
        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i)

            // لو العنصر ده عبارة عن نص (TextView)
            if (view is android.widget.TextView) {
                val text = view.text.toString().lowercase()

                // لو النص ده بيحتوي على الكلمة اللي اليوزر كتبها
                if (text.contains(lowerQuery)) {
                    // لقيناها! نعمل سكرول ناعم لحد مكانها
                    // (طرحنا 100 عشان نسيب مسافة فوق الكلمة ومتبقاش لازقة في سقف الشاشة)
                    scrollView.smoothScrollTo(0, view.top - 100)
                    return // نوقف تدوير خلاص
                }
            }
        }

        // لو لفينا على الشاشة كلها وملقيناش الكلمة
        Toast.makeText(this, "No match found for '$query'", Toast.LENGTH_SHORT).show()
    }
    // ── Options Menu (App Bar) with Search + Settings ──
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Search in dashboard..."
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                // لما يدوس بحث من الكيبورد
                searchInDashboard(query)
                searchView.clearFocus() // نقفل الكيبورد
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // (اختياري) بيبحث وهو بيكتب حرف بحرف من غير ما يدوس Enter
                if (!newText.isNullOrEmpty() && newText.length > 2) {
                    searchInDashboard(newText)
                }
                return true
            }
        })

        return true
    }

    // ── التعامل مع الضغطات جوه المنيو ──
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_dark_mode -> {
                // تفعيل الدارك مود
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                true
            }
            R.id.action_light_mode -> {
                // تفعيل اللايت مود
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                true
            }
            R.id.action_logout -> {
                // 1. مسح بيانات الدخول من الذاكرة
                val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                // 2. التوجيه لشاشة البداية (Onboarding)
                val intent = Intent(this, OnboardingActivity::class.java)
                // Flags عشان نمنع اليوزر إنه يرجع للـ Dashboard لو داس زرار Back في الموبايل
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}