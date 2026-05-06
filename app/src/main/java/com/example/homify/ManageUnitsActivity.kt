package com.example.homify

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


/**
 * ManageUnitsActivity — lists all property units with delete functionality.
 * Receives data from DashboardActivity via Intent extras.
 * Uses RecyclerView + UnitAdapter for dynamic rendering.
 */
class ManageUnitsActivity : AppCompatActivity() {

    private lateinit var adapter: UnitAdapter
    private lateinit var units: MutableList<Units>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_units)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.manage_units_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Receive extras from DashboardActivity
        val totalUnitsExtra = intent.getStringExtra("TOTAL_UNITS") ?: "4,210"

        // ── Sample unit data matching design screens ──
        units = mutableListOf(
            Units(
                id = 1,
                name = "Skyline Modern Studio",
                landlord = "Sarah Jenkins",
                details = "Central District • 450 sqft • Fully Furnished",
                price = "$1,200/mo",

                imageResId = android.R.drawable.ic_menu_gallery
            ),
            Units(
                id = 2,
                name = "Vintage Shared Suite",
                landlord = "Marcus Thorne",
                details = "North University Area • Shared Bath • Utilities Inc.",
                price = "$850/mo",

                imageResId = android.R.drawable.ic_menu_gallery
            ),
            Units(
                id = 3,
                name = "Industrial Brick Loft",
                landlord = "Elena Rodriguez",
                details = "Downtown Arts District • 2 Bed • 2 Bath",
                price = "$2,450/mo",

                imageResId = android.R.drawable.ic_menu_gallery
            )
        )

        // ── RecyclerView Setup ──
        val rvUnits: RecyclerView = findViewById(R.id.rv_units)
        // التعديل السليم عشان ميبقاش فيه خط أحمر
        adapter = UnitAdapter(units) { unit, position ->
            showDeleteUnitDialog(unit, position)
            true // الـ true دي هي اللي بتقفل الـ Lambda صح بدل "الحل السحري"
        }

        rvUnits.layoutManager = LinearLayoutManager(this)
        rvUnits.adapter = adapter

        // ── Floating Action Button ──
        val fab: FloatingActionButton = findViewById(R.id.fab_add_unit)
        fab.setOnClickListener {
            Toast.makeText(this, "Add new unit — coming soon", Toast.LENGTH_SHORT).show()
        }

        // ── Bottom Navigation ──
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
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
                R.id.nav_profile -> {
                    Toast.makeText(this, getString(R.string.nav_profile), Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
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
    private fun showDeleteUnitDialog(units: Units, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_delete))
            .setMessage(getString(R.string.confirm_delete_unit_msg))
            .setPositiveButton(getString(R.string.btn_confirm)) { dialog, _ ->
                // السطر ده هو اللي بيمسح فعلياً
                adapter.removeUnit(position)
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
