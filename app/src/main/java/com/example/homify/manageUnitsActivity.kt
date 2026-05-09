package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import data.viewmodel.AdminViewModel
import data.viewmodel.ViewModelFactory

/**
 * ManageUnitsActivity — lists all property units with delete functionality.
 * Receives data from DashboardActivity via Intent extras.
 * Uses RecyclerView + UnitAdapter for dynamic rendering.
 */
class manageUnitsActivity : AppCompatActivity() {

    private lateinit var adapter: unitAdapter
    private var units: MutableList<units> = mutableListOf()
    private lateinit var adminViewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_units)

        // 1. تثبيت اتجاه الشاشة (من النسخة الأولى)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // 2. Setup Toolbar وسهم الرجوع
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // تفعيل سهم الرجوع برمجياً
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // تحديد إيه اللي يحصل لما تدوسي على السهم
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // هيرجعك للأكتيفيتي اللي قبلها
        }

        // Receive extras from DashboardActivity
        val totalUnitsExtra = intent.getStringExtra(getString(R.string.total__units)) ?: getString(R.string._4_210)

        // 3. إعداد الـ DB والـ ViewModel (من النسخة التانية)
        val db = (application as homifyApp).database
        val factory = ViewModelFactory(
            application = application,
            userDao = db.userDao(),
            unitDao = db.unitDao()
        )
        adminViewModel = ViewModelProvider(this, factory)[AdminViewModel::class.java]

        // 4. RecyclerView Setup (مدمج)
        val rvUnits: RecyclerView = findViewById(R.id.rv_units)
        adapter = unitAdapter(units) { unit, position ->
            showDeleteUnitDialog(unit, position)
            true // الـ true دي هي اللي بتقفل الـ Lambda صح زي النسخة الأولى
        }
        rvUnits.layoutManager = LinearLayoutManager(this)
        rvUnits.adapter = adapter

        // 5. جلب كل الوحدات الحقيقية من الداتابيز ومراقبتها (Observe)
        adminViewModel.allUnits.observe(this) { unitList ->
            units.clear()
            units.addAll(unitList.map { unit ->
                units(
                    id       = unit.id,
                    name     = unit.title,
                    landlord = getString(R.string.landlord_Hash, unit.landlordId),
                    details  = "${unit.governorate} • ${unit.size} m² • ${unit.unitType.name}",
                    price    = getString(R.string.egp_mo, unit.price.toInt()),
                    imagePath = unit.images.split(",").firstOrNull()?.trim()
                )
            })
            adapter.notifyDataSetChanged() // تحديث الشاشة بعد جلب الداتا
        }

        // 6. Floating Action Button لفتح شاشة الإضافة
        val fab: FloatingActionButton = findViewById(R.id.fab_add_unit)
        fab.setOnClickListener {
            val intent = Intent(this, addUnitActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Show an AlertDialog to confirm unit deletion before removing.
     */
    private fun showDeleteUnitDialog(unit: units, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_delete))
            .setMessage(getString(R.string.confirm_delete_unit_msg))
            .setPositiveButton(getString(R.string.btn_confirm)) { dialog, _ ->
                // المسح الحقيقي من الداتابيز (الـ observer هيحدث الشاشة لوحده)
                adminViewModel.removeUnitById(unit.id)
                Toast.makeText(this, getString(R.string.toast_unit_deleted), Toast.LENGTH_SHORT).show()
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