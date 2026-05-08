package com.example.homify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import data.AppDatabase

class PropertyDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)

        // 1. استقبال الـ ID من الـ Intent (ده مفتاح الربط بالداتابيز)
        val unitId = intent.getIntExtra("UNIT_ID", -1)

        if (unitId != -1) {
            // جلب البيانات من الداتابيز
            loadUnitDetails(unitId)
        } else {
            // لو مفيش ID (حالة احتياطية) جرب جلب البيانات القديمة من Intent
            loadFromIntentFallback()
        }

        // 2. برمجة زرار الرجوع
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // 3. الانتقال لبروفايل المالك (ممكن تبعت معاه الـ ID لو حبيت)
        findViewById<View>(R.id.cardLandlord).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUnitDetails(unitId: Int) {
        val database = AppDatabase.getDatabase(this)

        // استخدام lifecycleScope لأن جلب البيانات من Room عملية suspend
        lifecycleScope.launch {
            val unit = database.unitDao().getUnitById(unitId)
            unit?.let {
                // ربط البيانات بالواجهة
                findViewById<TextView>(R.id.tvDetailTitle).text = it.title
                findViewById<TextView>(R.id.tvDetailPrice).text = "${it.price} EGP"
                findViewById<TextView>(R.id.tvDetailLocation).text = "${it.address}, ${it.governorate}"
                findViewById<TextView>(R.id.tvDescription).text = it.description

                // لو الصور عندك متخزنة كـ ID في الداتابيز (Drawable)
                // findViewById<ImageView>(R.id.ivDetailImage).setImageResource(it.imageRes)

                // تحديث صناديق المميزات
                setupFeatureBox(R.id.featureBed, R.drawable.bed, "${it.bedrooms} Bedroom")
                setupFeatureBox(R.id.featureBath, R.drawable.ic_bathroom, "${it.bathrooms} Bathroom")
                setupFeatureBox(R.id.featureArea, R.drawable.area, "${it.size} m²")

                // إعداد الخريطة والاتصال بناءً على بيانات الوحدة
                setupMap(it.address)


                val landlordProfile = database.profileDao().getLandlordByUserId(it.landlordId)
                val phone = landlordProfile?.phoneNumber ?: ""
                setupCallButton(phone)

                // اعرضي اسم المالك
                val landlordName = "${landlordProfile?.firstName} ${landlordProfile?.lastName}"
                findViewById<TextView>(R.id.tvLandlordName).text = landlordName
            }
        }


    }

    private fun setupMap(address: String) {
        val webView = findViewById<WebView>(R.id.mapWebView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        val mapUrl = "https://www.google.com/maps/search/?api=1&query=${address.replace(" ", "+")}"

        val htmlData = """
            <html><body style="margin:0;padding:0;">
                <iframe width="100%" height="100%" style="border:0;" 
                    src="https://maps.google.com/maps?q=${address}&output=embed">
                </iframe>
            </body></html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)

        // زر فتح تطبيق الخرائط الخارجي
        findViewById<TextView>(R.id.btnOpenMaps).setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            }
        }
    }

    private fun setupCallButton(phoneNumber: String) {
        val btnCall = findViewById<Button>(R.id.btnCallLandlord)
        btnCall.setOnClickListener {
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }
    }

    private fun setupFeatureBox(viewId: Int, iconId: Int, text: String) {
        val view = findViewById<View>(viewId)
        view?.let {
            it.findViewById<ImageView>(R.id.ivFeatureIcon).setImageResource(iconId)
            it.findViewById<TextView>(R.id.tvFeatureValue).text = text
        }
    }

    private fun loadFromIntentFallback() {
        // الكود القديم بتاعك كـ Backup لو مفيش ID
        val title = intent.getStringExtra("TITLE") ?: "Property"
        findViewById<TextView>(R.id.tvDetailTitle).text = title
        // ... وهكذا لباقي العناصر
    }
}