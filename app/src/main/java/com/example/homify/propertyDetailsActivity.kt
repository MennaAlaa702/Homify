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

        isDarkMode = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) applyDarkModeProgrammatically() else applyLightModeSettings()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val unitId = intent.getIntExtra(getString(R.string.Unit_Id), -1)
        if (unitId != -1) loadUnitDetails(unitId) else loadFromIntentFallback()
    }

    // ════════════════════════════════════════════
    //  دالة مشتركة لتحميل الصورة (drawable / http / file)
    // ════════════════════════════════════════════
    private fun loadImage(imagePath: String, imageView: ImageView) {
        val firstImage = imagePath.split(",").firstOrNull()?.trim() ?: ""
        when {
            // drawable من الـ resources (drawable://home2 مثلاً)
            firstImage.startsWith("drawable://") -> {
                val drawableName = firstImage.removePrefix("drawable://")
                val resId = resources.getIdentifier(drawableName, "drawable", packageName)
                imageView.setImageResource(if (resId != 0) resId else R.drawable.home3)
            }
            // رابط إنترنت
            firstImage.startsWith("http") -> {
                Glide.with(this)
                    .load(firstImage)
                    .placeholder(R.drawable.home3)
                    .error(R.drawable.home3)
                    .centerCrop()
                    .into(imageView)
            }
            // ملف محلي
            firstImage.isNotEmpty() && File(firstImage).exists() -> {
                Glide.with(this)
                    .load(File(firstImage))
                    .placeholder(R.drawable.home3)
                    .error(R.drawable.home3)
                    .centerCrop()
                    .into(imageView)
            }
            else -> imageView.setImageResource(R.drawable.home3)
        }
    }

    private fun loadUnitDetails(unitId: Int) {
        val database = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val unit = database.unitDao().getUnitById(unitId)
            unit?.let {
                findViewById<TextView>(R.id.tvDetailTitle).text = it.title
                findViewById<TextView>(R.id.tvDetailPrice).text = "${it.price} EGP"
                findViewById<TextView>(R.id.tvDetailLocation).text = "${it.address}, ${it.governorate}"
                findViewById<TextView>(R.id.tvDescription).text = it.description
                landlordUserId = it.landlordId

                findViewById<View>(R.id.bg_card_landlord).setOnClickListener {
                    val intent = Intent(this@propertyDetailsActivity, profileActivity::class.java)
                    intent.putExtra(getString(R.string.view_user_id), landlordUserId)
                    startActivity(intent)
                }

                setupFeatureBox(R.id.featureBed, R.drawable.bed, "${it.bedrooms} Bedroom")
                setupFeatureBox(R.id.featureBath, R.drawable.ic_bathroom, "${it.bathrooms} Bathroom")
                setupFeatureBox(R.id.featureArea, R.drawable.area, "${it.size} m²")

                setupMap(it.address, "https://maps.google.com/maps?q=${it.address.replace(" ", "+")}")

                val landlordProfile = database.profileDao().getLandlordByUserId(it.landlordId)
                setupCallButton(landlordProfile?.phoneNumber ?: "")

                val firstName = if (landlordProfile?.firstName.isNullOrEmpty()) "admin" else landlordProfile?.firstName
                val lastName  = if (landlordProfile?.lastName.isNullOrEmpty())  "profile" else landlordProfile?.lastName
                findViewById<TextView>(R.id.tvLandlordName).text = "$firstName $lastName"

                // ✅ تحميل الصورة بنفس المنطق بتاع الـ Card
                loadImage(it.images, findViewById(R.id.ivDetailImage))
            }
        }
    }

    private fun loadFromIntentFallback() {
        val title    = intent.getStringExtra(getString(R.string.TITLE))    ?: getString(R.string.the_green_house)
        val price    = intent.getStringExtra(getString(R.string.PRICE))    ?: getString(R.string._650)
        val address  = intent.getStringExtra(getString(R.string.Address))  ?: getString(R.string.shalaby_st_10)
        val desc     = intent.getStringExtra(getString(R.string.desc))     ?: getString(R.string.a_cozy_studio)
        val mapLink  = intent.getStringExtra(getString(R.string.map_link)) ?: getString(R.string.https_maps_google_com)
        val beds     = intent.getStringExtra(getString(R.string.Bedrooms))     ?: "1"
        val baths    = intent.getStringExtra(getString(R.string.Bathrooms))    ?: "1"
        val area     = intent.getStringExtra(getString(R.string.area))     ?: "110"
        val imageUrl = intent.getStringExtra(getString(R.string.IMAGE))    ?: ""

        findViewById<TextView>(R.id.tvDetailTitle).text    = title
        findViewById<TextView>(R.id.tvDetailPrice).text    = "$price EGP"
        findViewById<TextView>(R.id.tvDetailLocation).text = address
        findViewById<TextView>(R.id.tvDescription).text    = desc

        setupFeatureBox(R.id.featureBed,  R.drawable.bed,         "$beds Bedroom")
        setupFeatureBox(R.id.featureBath, R.drawable.ic_bathroom,  "$baths Bathroom")
        setupFeatureBox(R.id.featureArea, R.drawable.area,         "$area m²")

        // ✅ نفس الدالة المشتركة
        loadImage(imageUrl, findViewById(R.id.ivDetailImage))

        setupMap(address, mapLink)
        setupCallButton("0123456789")
    }

    private fun setupMap(address: String, fallbackMapLink: String) {
        val webView = findViewById<WebView>(R.id.mapWebView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        val mapFilter = if (isDarkMode) "invert(90%) hue-rotate(180deg)" else "none"
        val bgHex     = if (isDarkMode) "#121212" else "#FFFFFF"

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            webView.settings.forceDark = if (isDarkMode) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF
        }

        findViewById<TextView>(R.id.btnOpenMaps).setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${address.replace(" ", "+")}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackMapLink)))
            }
        }
    }

    private fun setupCallButton(phoneNumber: String) {
        findViewById<Button>(R.id.btnCallLandlord).setOnClickListener {
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") }
            if (intent.resolveActivity(packageManager) != null) startActivity(intent)
            else Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyDarkModeProgrammatically() {
        findViewById<View>(android.R.id.content).rootView.setBackgroundColor("#121212".toColorInt())
        findViewById<TextView>(R.id.tvDetailTitle).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvDescription).setTextColor("#BBBBBB".toColorInt())
        findViewById<TextView>(R.id.tvDetailLocation).setTextColor("#BBBBBB".toColorInt())
        findViewById<TextView>(R.id.tvAboutTitle).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvLocationMapTitle).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvDetailPrice).setTextColor("#2196F3".toColorInt())
        updateFeatureCardStyle(R.id.featureBed,  "#333333", Color.WHITE)
        updateFeatureCardStyle(R.id.featureBath, "#333333", Color.WHITE)
        updateFeatureCardStyle(R.id.featureArea, "#333333", Color.WHITE)
        findViewById<androidx.cardview.widget.CardView>(R.id.bg_card_landlord)
            .setCardBackgroundColor("#252525".toColorInt())
        findViewById<TextView>(R.id.tvLandlordName).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvOwnerLabel).setTextColor("#888888".toColorInt())
    }

    private fun applyLightModeSettings() {
        updateFeatureCardStyle(R.id.featureBed,  "#F5F5F5", Color.BLACK)
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