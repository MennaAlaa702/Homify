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

        fullList = listOf(
            Property(
                "The Green House",
                "650",
                "Leeds",
                "Headingley, St 5",
                "A cozy studio near the university with all amenities included.",
                R.drawable.home3,
                "1", "1", "50",
                "Studio",
                listOf("Gym", "Parking"),
                "https://maps.google.com/?q=53.8197,-1.5776"
            ),
            Property(
                "Urban Nest Apartment",
                "1200",
                "Cairo",
                "Maadi, Road 9",
                "Modern apartment with a great view and balcony.",
                R.drawable.home2,
                "2", "1", "110",
                "Apartment",
                listOf("WiFi", "Garden"),
                "https://maps.google.com/?q=29.9602,31.2569"
            ),
            Property(
                "Luxury Villa",
                "5000",
                "New Cairo",
                "90th Street, Villa 12",
                "Large villa with a private garden and swimming pool.",
                R.drawable.home,
                "4", "3", "350",
                "Villa",
                listOf("Pool", "Garden", "Parking"),
                "https://maps.google.com/?q=30.0074,31.4913"
            ),
            Property(
                "Shared Student Room",
                "300",
                "Alexandria",
                "Sidi Gaber, Nile St",
                "Affordable shared room for students, close to the library.",
                R.drawable.kitchen,
                "1", "1", "20",
                "Shared Room",
                listOf("WiFi", "Laundry"),
                "https://maps.google.com/?q=31.2201,29.9426"
            )
        )

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