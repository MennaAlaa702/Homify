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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landlord_home)
        val btnMenu = findViewById<ImageButton>(R.id.btn_menu)

        btnMenu.setOnClickListener {
            val sideMenu = SideMenuFragment()
            sideMenu.show(supportFragmentManager, "SideMenuFragment")
        }

        // ================= 1. ربط المتغيرات بالـ Views =================
        val rvUnits = findViewById<RecyclerView>(R.id.rv_landlord_units)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        val ivProfile = findViewById<ImageView>(R.id.iv_profile) // صورة البروفايل

        // تجهيز شكل الـ RecyclerView (هيفضل فاضي لحد ما الداتا تيجي)
        rvUnits.layoutManager = LinearLayoutManager(this)

        // TODO: لما تستلمي شغل التيم والداتا بيز، هتربطي الأدابتر بتاعهم هنا زي كده:
        // rvUnits.adapter = TeamAdapterName(databaseList)

        // ================= 2. برمجة الزراير (Navigation) =================

        // 1. زرار الإضافة (FAB) بيودي لـ AddUnitActivity
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddUnitActivity::class.java)
            startActivity(intent)
        }

        // 2. صورة البروفايل بتودي لـ MainActivity (مؤقتاً)
        ivProfile.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}