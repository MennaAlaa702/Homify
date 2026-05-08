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
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
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

        val scrollView = findViewById<android.widget.ScrollView>(R.id.scrollView)
        val container = scrollView.getChildAt(0) as? android.view.ViewGroup ?: return
        val lowerQuery = query.lowercase()

        // دالة البحث العميق جوه كل الـ Views
        fun findTextInViewGroup(viewGroup: android.view.ViewGroup): android.view.View? {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)

                if (child is android.widget.TextView) {
                    if (child.text.toString().lowercase().contains(lowerQuery)) {
                        return child
                    }
                } else if (child is android.view.ViewGroup) {
                    val found = findTextInViewGroup(child)
                    if (found != null) return found
                }
            }
            return null
        }

        val foundView = findTextInViewGroup(container)

        if (foundView != null) {
            // حساب المسافة بين العنصر وبداية الـ ScrollView
            val rect = android.graphics.Rect()
            foundView.getDrawingRect(rect)
            container.offsetDescendantRectToMyCoords(foundView, rect)

            // عمل السكرول (نطرح 150 عشان النص ميبقاش لازق فوق)
            scrollView.smoothScrollTo(0, rect.top - 150)

            // اختيار اختياري: نغير لون النص لحظياً عشان اليوزر يعرف إيه اللي لُقط
            foundView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            foundView.postDelayed({
                foundView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }, 1500)

        } else {
            // التوست ده يظهر فقط لو فعلاً ملقيناش حاجة
            Toast.makeText(this, "No match found for '$query'", Toast.LENGTH_SHORT).show()
        }
        // شيلنا التوست اللي كان بره الـ if والـ else ومبوظ الدنيا
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