package com.example.homify

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import data.AppDatabase
import java.io.File

class propertyDetailsActivity : AppCompatActivity() {

    private var isDarkMode = false
    private var landlordUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR


        // --- تحديد وتطبيق المود (Dark/Light) ---
        isDarkMode = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            applyDarkModeProgrammatically()
        } else {
            applyLightModeSettings()
        }

        // --- أزرار أساسية ---
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }



        // --- جلب البيانات (Database أو Fallback) ---
        val unitId = intent.getIntExtra(getString(R.string.Unit_Id), -1)

        if (unitId != -1) {
            loadUnitDetails(unitId)
        } else {
            loadFromIntentFallback()
        }
    }

    private fun loadUnitDetails(unitId: Int) {
        val database = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val unit = database.unitDao().getUnitById(unitId)
            unit?.let {
                // ربط البيانات الأساسية
                findViewById<TextView>(R.id.tvDetailTitle).text = it.title
                findViewById<TextView>(R.id.tvDetailPrice).text = "${it.price} EGP"
                findViewById<TextView>(R.id.tvDetailLocation).text = "${it.address}, ${it.governorate}"
                findViewById<TextView>(R.id.tvDescription).text = it.description
                landlordUserId = it.landlordId

                // ✅ سجّل الـ click listener هنا بعد ما landlordUserId اتحدد
                findViewById<View>(R.id.bg_card_landlord).setOnClickListener {
                    val intent = Intent(this@propertyDetailsActivity, profileActivity::class.java)
                    intent.putExtra("VIEW_USER_ID", landlordUserId)
                    startActivity(intent)
                }

                // تحديث صناديق المميزات
                setupFeatureBox(R.id.featureBed, R.drawable.bed, "${it.bedrooms} Bedroom")
                setupFeatureBox(R.id.featureBath, R.drawable.ic_bathroom, "${it.bathrooms} Bathroom")
                setupFeatureBox(R.id.featureArea, R.drawable.area, "${it.size} m²")

                // إعداد الخريطة
                setupMap(it.address, "https://maps.google.com/maps?q=${it.address.replace(" ", "+")}")

                //  نحفظ الـ landlordId عشان الكارت يستخدمه
                landlordUserId = it.landlordId

                // جلب بيانات المالك والاتصال
                val landlordProfile = database.profileDao().getLandlordByUserId(it.landlordId)
                val phone = landlordProfile?.phoneNumber ?: ""
                setupCallButton(phone)

                val landlordName = "${landlordProfile?.firstName} ${landlordProfile?.lastName}"
                findViewById<TextView>(R.id.tvLandlordName).text = landlordName

                // إعداد الصورة باستخدام Glide
                val firstImage = it.images.split(",").firstOrNull()?.trim() ?: ""
                val imageView = findViewById<ImageView>(R.id.ivDetailImage)
                if (firstImage.isNotEmpty()) {
                    val file = File(firstImage)
                    if (file.exists()) {
                        Glide.with(this@propertyDetailsActivity).load(file).into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.home3)
                    }
                } else {
                    imageView.setImageResource(R.drawable.home3)
                }
            }
        }
    }

    private fun loadFromIntentFallback() {
        val title = intent.getStringExtra("TITLE") ?: "The Green House"
        val price = intent.getStringExtra("PRICE") ?: "650"
        val address = intent.getStringExtra("ADDRESS") ?: "Shalaby, St 10"
        val desc = intent.getStringExtra("DESC") ?: "A cozy studio near the university..."
        val mapLink = intent.getStringExtra("MAP_LINK") ?: "https://maps.google.com"
        val beds = intent.getStringExtra("BEDS") ?: "1"
        val baths = intent.getStringExtra("BATHS") ?: "1"
        val area = intent.getStringExtra("AREA") ?: "110"

        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailPrice).text = "$price EGP"
        findViewById<TextView>(R.id.tvDetailLocation).text = address
        findViewById<TextView>(R.id.tvDescription).text = desc

        setupFeatureBox(R.id.featureBed, R.drawable.bed, "$beds Bedroom")
        setupFeatureBox(R.id.featureBath, R.drawable.ic_bathroom, "$baths Bathroom")
        setupFeatureBox(R.id.featureArea, R.drawable.area, "$area m²")

        // تحميل الصورة: محاولة قراءة رابط نصي، وإن لم يوجد، نستخدم الـ ID من الـ Resources
        val imagePath = intent.getStringExtra("IMAGE_PATH") ?: ""
        val imageView = findViewById<ImageView>(R.id.ivDetailImage)
        if (imagePath.isNotEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                Glide.with(this).load(file).into(imageView)
            } else {
                imageView.setImageResource(R.drawable.home3)
            }
        } else {
            val imageRes = intent.getIntExtra("IMAGE", R.drawable.home3)
            imageView.setImageResource(imageRes)
        }

        setupMap(address, mapLink)

        // زر الاتصال في حالة الـ Fallback
        setupCallButton("0123456789")
    }

    private fun setupMap(address: String, fallbackMapLink: String) {
        val webView = findViewById<WebView>(R.id.mapWebView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        // إعداد الخريطة الذكية لدعم الـ Dark Mode برمجياً و بـ CSS
        val mapFilter = if (isDarkMode) "invert(90%) hue-rotate(180deg)" else "none"
        val bgHex = if (isDarkMode) "#121212" else "#FFFFFF"

        val htmlData = """
            <html>
            <head>
                <style>
                    body { margin: 0; padding: 0; background-color: $bgHex; }
                    iframe { width: 100%; height: 100%; border: 0; filter: $mapFilter; }
                </style>
            </head>
            <body>
                <iframe src="https://maps.google.com/maps?q=${address.replace(" ", "+")}&output=embed"></iframe>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)

        // التحكم في الـ ForceDark للـ WebView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            webView.settings.forceDark = if (isDarkMode) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF
        }

        // فتح الخرائط الخارجية
        findViewById<TextView>(R.id.btnOpenMaps).setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${address.replace(" ", "+")}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackMapLink))
                startActivity(webIntent)
            }
        }
    }

    private fun setupCallButton(phoneNumber: String) {
        val btnCall = findViewById<Button>(R.id.btnCallLandlord)
        btnCall.setOnClickListener {
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // استخدام ACTION_DIAL كما حدد الدكتور لفتح لوحة الاتصال بأمان
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyDarkModeProgrammatically() {
        val rootView = findViewById<View>(android.R.id.content).rootView
        rootView.setBackgroundColor("#121212".toColorInt())

        findViewById<View>(R.id.ivDetailImage).parent.let {
            (it as? View)?.setBackgroundColor("#121212".toColorInt())
        }

        findViewById<TextView>(R.id.tvDetailTitle).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvDescription).setTextColor("#BBBBBB".toColorInt())
        findViewById<TextView>(R.id.tvDetailLocation).setTextColor("#BBBBBB".toColorInt())
        findViewById<TextView>(R.id.tvAboutTitle).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvLocationMapTitle).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvDetailPrice).setTextColor("#2196F3".toColorInt())

        updateFeatureCardStyle(R.id.featureBed, "#333333", Color.WHITE)
        updateFeatureCardStyle(R.id.featureBath, "#333333", Color.WHITE)
        updateFeatureCardStyle(R.id.featureArea, "#333333", Color.WHITE)

        val cardLandlord = findViewById<androidx.cardview.widget.CardView>(R.id.bg_card_landlord)
        cardLandlord.setCardBackgroundColor("#252525".toColorInt())
        findViewById<TextView>(R.id.tvLandlordName).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvOwnerLabel).setTextColor("#888888".toColorInt())
    }

    private fun applyLightModeSettings() {
        updateFeatureCardStyle(R.id.featureBed, "#F5F5F5", Color.BLACK)
        updateFeatureCardStyle(R.id.featureBath, "#F5F5F5", Color.BLACK)
        updateFeatureCardStyle(R.id.featureArea, "#F5F5F5", Color.BLACK)
    }

    private fun updateFeatureCardStyle(viewId: Int, bgColor: String, textColor: Int) {
        val view = findViewById<View>(viewId)
        view?.backgroundTintList = ColorStateList.valueOf(bgColor.toColorInt())
        view?.findViewById<TextView>(R.id.tvFeatureValue)?.setTextColor(textColor)
    }

    private fun setupFeatureBox(viewId: Int, iconId: Int, text: String) {
        val view = findViewById<View>(viewId)
        view?.let {
            it.findViewById<ImageView>(R.id.ivFeatureIcon).setImageResource(iconId)
            it.findViewById<TextView>(R.id.tvFeatureValue).text = text
        }
    }
}