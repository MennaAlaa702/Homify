package com.example.homify

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import data.viewmodel.AdminViewModel
import data.viewmodel.ViewModelFactory

/**
 * DashboardActivity — the main entry screen for Admin.
 * Shows platform metrics, quick access to Manage Users and Manage Units.
 */
class dashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // منع دوران الشاشة لو محتاجين نثبتها (من النسخة الأولى)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Setup Toolbar (App Bar)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        // ── Explicit Intent → ManageUsersActivity ──
        val btnManageUsers: MaterialButton = findViewById(R.id.btn_manage_users)
        btnManageUsers.setOnClickListener {
            val intent = Intent(this, manageUsersActivity::class.java)
            startActivity(intent)
        }

        // ── Explicit Intent → ManageUnitsActivity ──
        val btnManageUnits: MaterialButton = findViewById(R.id.btn_manage_units)
        btnManageUnits.setOnClickListener {
            val intent = Intent(this, manageUnitsActivity::class.java)
            startActivity(intent)
        }

        // ── DB: ربط الأرقام الحقيقية (من النسخة التانية) ──

        val db = (application as homifyApp).database

        val factory = ViewModelFactory(
            application = application,
            userDao = db.userDao(),
            unitDao = db.unitDao()
        )
        val adminViewModel: AdminViewModel by viewModels { factory }

        val tvUsers     = findViewById<TextView>(R.id.tv_users_value)
        val tvTenants   = findViewById<TextView>(R.id.tv_tenants_value)
        val tvLandlords = findViewById<TextView>(R.id.tv_landlords_value)
        val tvUnits     = findViewById<TextView>(R.id.tv_units_value)

        adminViewModel.getSystemStats().observe(this) { stats ->
            tvUsers.text     = stats[getString(R.string.totalusers)].toString()
            tvTenants.text   = stats[getString(R.string.totaltenants)].toString()
            tvLandlords.text = stats[getString(R.string.totallandlords)].toString()
            tvUnits.text     = stats[getString(R.string.totalunits)].toString()
        }
    }


    // دالة البحث الذكي العميقة (من النسخة الأولى - بتدور جوه كل العناصر)
    private fun searchInDashboard(query: String?) {
        if (query.isNullOrEmpty()) return

        val scrollView = findViewById<android.widget.ScrollView>(R.id.scrollView)
        val container = scrollView.getChildAt(0) as? ViewGroup ?: return
        val lowerQuery = query.lowercase()

        // دالة البحث العميق جوه كل الـ Views
        fun findTextInViewGroup(viewGroup: ViewGroup): View? {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)

                if (child is TextView) {
                    if (child.text.toString().lowercase().contains(lowerQuery)) {
                        return child
                    }
                } else if (child is ViewGroup) {
                    val found = findTextInViewGroup(child)
                    if (found != null) return found
                }
            }
            return null
        }

        val foundView = findTextInViewGroup(container)

        if (foundView != null) {
            // حساب المسافة بين العنصر وبداية الـ ScrollView
            val rect = Rect()
            foundView.getDrawingRect(rect)
            container.offsetDescendantRectToMyCoords(foundView, rect)

            // عمل السكرول (نطرح 150 عشان النص ميبقاش لازق فوق)
            scrollView.smoothScrollTo(0, rect.top - 150)

            // تغيير لون النص لحظياً عشان اليوزر يعرف إيه اللي لُقط
            foundView.setBackgroundColor(Color.TRANSPARENT)
            foundView.postDelayed({
                foundView.setBackgroundColor(Color.TRANSPARENT)
            }, 1500)

        } else {
            // التوست ده يظهر فقط لو فعلاً ملقيناش حاجة
            Toast.makeText(this, getString(R.string.no_match_found, query), Toast.LENGTH_SHORT).show()
        }
    }

    // ── Options Menu (App Bar) with Search + Settings ──
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = getString(R.string.search_in_dashboard)
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
                val sharedPref = getSharedPreferences(getString(R.string.userprefs), Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                // 2. التوجيه لشاشة البداية (Onboarding)
                val intent = Intent(this, onboardingActivity::class.java)
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