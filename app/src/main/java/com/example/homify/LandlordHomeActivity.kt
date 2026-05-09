package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.activity.viewModels
import data.viewmodel.UnitViewModel
import data.viewmodel.ViewModelFactory
import android.widget.TextView
class LandlordHomeActivity : AppCompatActivity() {

    private lateinit var propertyAdapter: PropertyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landlord_home)

        // ربط الـ Views
        val btnMenu  = findViewById<ImageButton>(R.id.btn_menu)
        val rvUnits  = findViewById<RecyclerView>(R.id.rv_landlord_units)
        val fabAdd   = findViewById<FloatingActionButton>(R.id.fab_add)
        val appBar = findViewById<View>(R.id.my_app_bar)
        val ivProfile = findViewById<ImageView>(R.id.iv_profile)

        // جلب الـ Session
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val landlordId = sharedPref.getInt("userId", -1)

// تحميل صورة البروفايل
        val imagePath = sharedPref.getString("profile_image_path", null)
        if (imagePath != null) {
            val imgFile = java.io.File(imagePath)
            if (imgFile.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.absolutePath)
                ivProfile.setImageBitmap(bitmap)
            }
        }

        // إعداد الـ DB والـ ViewModel
        val db      = (application as HomifyApp).database
        val factory = ViewModelFactory(
            application = application,
            unitDao     = db.unitDao()
        )
        val unitViewModel: UnitViewModel by viewModels { factory }

        // ✅ إعداد الـ RecyclerView مرة واحدة بس
        propertyAdapter = PropertyAdapter(emptyList())
        rvUnits.layoutManager = LinearLayoutManager(this)
        rvUnits.adapter       = propertyAdapter

        // ربط الـ TextViews
        val tvTotal  = findViewById<TextView>(R.id.tv_total_val)
        val tvAvail  = findViewById<TextView>(R.id.tv_avail_val)
        val tvRented = findViewById<TextView>(R.id.tv_rented_val)

        unitViewModel.getLandlordUnits(landlordId).observe(this) { units ->
            // الأرقام
            val total  = units.size
            val rented = "1"
            val avail  = units.size

            tvTotal.text  = total.toString()
            tvRented.text = rented.toString()
            tvAvail.text  = avail.toString()

            // الـ properties
            val properties = units.map { unit ->
                Property(
                    id           = unit.id,
                    title        = unit.title,
                    price        = unit.price.toInt().toString(),
                    governorate  = unit.governorate,
                    address      = unit.address,
                    description  = unit.description,
                    imageUrl     = unit.images.split(",").firstOrNull() ?: "",
                    bedrooms     = unit.bedrooms.toString(),
                    bathrooms    = unit.bathrooms.toString(),
                    size         = unit.size.toString(),
                    type         = unit.unitType.name,
                    amenities    = unit.amenities.split(",").map { it.trim() },
                    locationLink = unit.locationLink
                )
            }
            propertyAdapter.updateData(properties)
        }

        // زرار المنيو
        btnMenu.setOnClickListener {
            val sideMenu = SideMenuFragment()
            sideMenu.show(supportFragmentManager, "SideMenuFragment")
        }

        // زرار الإضافة (FAB)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddUnitActivity::class.java))
        }

        // صورة البروفايل
        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val appBar = findViewById<View>(R.id.my_app_bar)
        val ivProfile = appBar.findViewById<ImageView>(R.id.iv_profile)
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val imagePath = sharedPref.getString("profile_image_path", null)
        if (imagePath != null) {
            val imgFile = java.io.File(imagePath)
            if (imgFile.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.absolutePath)
                ivProfile.setImageBitmap(bitmap)
            }
        }
    }
}