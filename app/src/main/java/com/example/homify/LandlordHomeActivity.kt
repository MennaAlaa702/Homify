package com.example.homify

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LandlordHomeActivity : AppCompatActivity() {

    // 1. تعريف الأدابتر والقائمة (زي ما عملتي في الـ Tenant)
    private lateinit var propertyAdapter: PropertyAdapter
    private var myPropertiesList = listOf<Property>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landlord_home)

        // ================= 2. ربط الـ Views =================
        val btnMenu = findViewById<ImageButton>(R.id.btn_menu)
        val rvUnits = findViewById<RecyclerView>(R.id.rv_landlord_units)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        val ivProfile = findViewById<ImageView>(R.id.iv_profile)

        // ================= 3. تجهيز الداتا الوهمية (Dummy Data) =================
        // دي العقارات اللي هتظهر للـ Landlord (تقدري تغيريها براحتك)
        myPropertiesList = listOf(
            Property(
                "Modern Sunny Studio", "850", "Minia", "Shalaby, St 10",
                "Beautiful studio for students.", R.drawable.home,
                "1", "1", "45", "Studio", listOf("WiFi"), "link"
            ),
            Property(
                "Family Apartment", "2500", "Minia", "Lotus District",
                "Spacious apartment near the Nile.", R.drawable.home2,
                "3", "2", "150", "Apartment", listOf("Parking", "Elevator"), "link"
            )
        )

        // ================= 4. إعداد الـ RecyclerView =================
        propertyAdapter = PropertyAdapter(myPropertiesList)
        rvUnits.layoutManager = LinearLayoutManager(this)
        rvUnits.adapter = propertyAdapter

        // ================= 5. برمجة الزراير (القديمة زي ما هي) =================

        // زرار المنيو
        btnMenu.setOnClickListener {
            val sideMenu = SideMenuFragment()
            sideMenu.show(supportFragmentManager, "SideMenuFragment")
        }

        // زرار الإضافة (FAB)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddUnitActivity::class.java)
            startActivity(intent)
        }

        // صورة البروفايل
        ivProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}