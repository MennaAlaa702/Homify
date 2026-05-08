package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.activity.viewModels
import data.viewmodel.AdminViewModel
import data.viewmodel.ViewModelFactory
import androidx.lifecycle.ViewModelProvider
/**
 * ManageUnitsActivity — lists all property units with delete functionality.
 * Receives data from DashboardActivity via Intent extras.
 * Uses RecyclerView + UnitAdapter for dynamic rendering.
 */
class ManageUnitsActivity : AppCompatActivity() {

    private lateinit var adapter: UnitAdapter
    private var units: MutableList<Units> = mutableListOf()

    private lateinit var adminViewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_units)

        // Setup Toolbar
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
        val totalUnitsExtra = intent.getStringExtra("TOTAL_UNITS") ?: "4,210"

        // ── Sample unit data matching design screens ──
        // ── DB: جلب كل الوحدات ──
        // ── DB ──
        val db = (application as HomifyApp).database
        val factory = ViewModelFactory(
            application = application,
            userDao = db.userDao(),
            unitDao = db.unitDao()
        )
        adminViewModel = ViewModelProvider(this, factory).get(AdminViewModel::class.java)

// ── RecyclerView Setup ── (بـ list فاضية في الأول)
        val rvUnits: RecyclerView = findViewById(R.id.rv_units)
        adapter = UnitAdapter(units) { unit, position ->  // ← units مش mutableListOf()
            showDeleteUnitDialog(unit, position)
            true
        }
        rvUnits.layoutManager = LinearLayoutManager(this)
        rvUnits.adapter = adapter

// ── observe مرة واحدة بس ──
        adminViewModel.allUnits.observe(this) { unitList ->
            units.clear()
            units.addAll(unitList.map { unit ->
                Units(
                    id       = unit.id,
                    name     = unit.title,
                    landlord = "Landlord #${unit.landlordId}",
                    details  = "${unit.governorate} • ${unit.size} m² • ${unit.unitType.name}",
                    price    = "${unit.price.toInt()} EGP/mo"
                )
            })
            adapter.notifyDataSetChanged()
        }
//        rvUnits.layoutManager = LinearLayoutManager(this)
//        rvUnits.adapter = adapter

        // ── Floating Action Button ──
        val fab: FloatingActionButton = findViewById(R.id.fab_add_unit)
        fab.setOnClickListener {
            val intent = Intent(this, AddUnitActivity::class.java)
            startActivity(intent)
        }

        // كود الdb
    }

    /**
     * Show an AlertDialog to confirm unit deletion before removing.
     */
    /*private fun showDeleteUnitDialog(unit: Unit, position: Int): Unit {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_delete))
            .setMessage(getString(R.string.confirm_delete_unit_msg))
            .setPositiveButton(getString(R.string.btn_confirm)) { dialog, _ ->
                adapter.removeUnit(position)
                units.remove(unit)
                Toast.makeText(this, getString(R.string.toast_unit_deleted), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        TODO("Provide the return value")
    }*/
    /**
     * Show an AlertDialog to confirm unit deletion before removing.
     */
    /**
     * Show an AlertDialog to confirm unit deletion before removing.
     */
    /**
     * Show an AlertDialog to confirm unit deletion before removing.
     */
    private fun showDeleteUnitDialog(unit: Units, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_delete))
            .setMessage(getString(R.string.confirm_delete_unit_msg))
            .setPositiveButton(getString(R.string.btn_confirm)) { dialog, _ ->
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
