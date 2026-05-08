package com.example.homify

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.card.MaterialCardView
import androidx.activity.viewModels
import data.viewmodel.UnitViewModel
import data.viewmodel.ViewModelFactory

class TenantHome : AppCompatActivity() {

    private lateinit var propertyAdapter: PropertyAdapter
    private var fullList = listOf<Property>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tenant_home)

        val btnFilter = findViewById<MaterialCardView>(R.id.btnFiltert)
        val recyclerView = findViewById<RecyclerView>(R.id.rvProperties)
        val tvUserWelcome = findViewById<TextView>(R.id.tvUserWelcome)
        val etSearch = findViewById<EditText>(R.id.searchtext)
// هاتي الزرار من الـ App Bar المضمن
        val btnMenu = findViewById<ImageButton>(R.id.btn_menu)

        btnMenu.setOnClickListener {
            val sideMenu = SideMenuFragment()
            sideMenu.show(supportFragmentManager, "SideMenuFragment")
        }
        // 1. اربطي صورة البروفايل من الـ App Bar
        val ivProfile = findViewById<ImageView>(R.id.iv_profile)

// 2. برمجى الانتقال لشاشة البروفايل عند الضغط عليها
        ivProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        val currentUserName = fetchUserNameFromSource()
        tvUserWelcome.text = "Hi $currentUserName !"

        btnFilter.setOnClickListener {
            showFilterOptions()
        }

        // ── DB: جلب كل الوحدات ──
        val db = (application as HomifyApp).database
        val factory = ViewModelFactory(
            application = application,
            unitDao = db.unitDao()
        )
        val unitViewModel: UnitViewModel by viewModels { factory }

        unitViewModel.allUnitsLatest.observe(this) { units ->
            fullList = units.map { unit ->
                Property(
                    id           = unit.id,
                    title        = unit.title,
                    price        = unit.price.toInt().toString(),
                    governorate  = unit.governorate,
                    address      = unit.address,
                    description  = unit.description,
                    imageUrl     = R.drawable.home,
                    bedrooms     = unit.bedrooms.toString(),
                    bathrooms    = unit.bathrooms.toString(),
                    size         = unit.size.toString(),
                    type         = unit.unitType.name,
                    amenities    = unit.amenities.split(",").map { it.trim() },
                    locationLink = unit.locationLink
                )
            }
            propertyAdapter.updateData(fullList)
        }

        propertyAdapter = PropertyAdapter(fullList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = propertyAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBySearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupChips()

        // كود الdb
    }

    private fun fetchUserNameFromSource(): String {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return sharedPref.getString("username", "Guest") ?: "Guest"
    }

    private fun showFilterOptions() {
        val filterAnchor = findViewById<MaterialCardView>(R.id.btnFiltert)
        val listPopupWindow = androidx.appcompat.widget.ListPopupWindow(this)
        listPopupWindow.anchorView = filterAnchor

        val items = arrayOf(
            getString(R.string.price_low_to_high),
            getString(R.string.price_high_to_low),
            getString(R.string.location_a_z)
        )

        val adapter = object : android.widget.ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                val text = view.findViewById<TextView>(android.R.id.text1)
                text.setTextColor(android.graphics.Color.BLACK)
                text.textSize = 16f
                return view
            }
        }

        listPopupWindow.setAdapter(adapter)
        listPopupWindow.width = 600
        listPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.menu_background))

        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> sortPropertiesByPrice(true)
                1 -> sortPropertiesByPrice(false)
                2 -> {
                    val sortedList = fullList.sortedBy { it.governorate }
                    propertyAdapter.updateData(sortedList)
                }
            }
            listPopupWindow.dismiss()
        }
        listPopupWindow.show()
    }

    private fun sortPropertiesByPrice(lowToHigh: Boolean) {
        val sortedList = if (lowToHigh) {
            fullList.sortedBy { it.price.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }
        } else {
            fullList.sortedByDescending { it.price.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }
        }
        propertyAdapter.updateData(sortedList)
    }

    private fun setupChips() {
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
        val bgColors = intArrayOf(ContextCompat.getColor(this, R.color.primary_blue), ContextCompat.getColor(this, R.color.search_bar_bg))
        val txtColors = intArrayOf(ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.text_gray_dark))

        val bgList = ColorStateList(states, bgColors)
        val txtList = ColorStateList(states, txtColors)

        val chipIds = listOf(R.id.chipAll, R.id.chipNearUni, R.id.chipStudio, R.id.chipVilla, R.id.chipApartment, R.id.chipShared)

        chipIds.forEach { id ->
            val chip = findViewById<Chip>(id)
            chip.chipBackgroundColor = bgList
            chip.setTextColor(txtList)

            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    filterPropertiesFromDB(buttonView.text.toString())
                }
            }
        }
        findViewById<Chip>(R.id.chipAll).isChecked = true
    }

    private fun filterPropertiesFromDB(type: String) {
        if (type == "All") {
            propertyAdapter.updateData(fullList)
        } else {
            val filtered = fullList.filter { it.type == type }
            propertyAdapter.updateData(filtered)
        }
    }

    private fun filterBySearch(query: String) {
        if (query.isEmpty()) {
            propertyAdapter.updateData(fullList)
        } else {
            val filtered = fullList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.governorate.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
            }
            propertyAdapter.updateData(filtered)
        }
    }
}