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

class PropertyDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)

        val isDarkMode = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        // تطبيق الألوان حسب المود
        if (isDarkMode) {
            applyDarkModeProgrammatically()
        } else {
            applyLightModeSettings() // دالة للتأكد من نظافة اللايت مود
        }

        // --- جلب البيانات ---
        val title = intent.getStringExtra("TITLE") ?: "The Green House"
        val price = intent.getStringExtra("PRICE") ?: "650"
        val address = intent.getStringExtra("ADDRESS") ?: "Shalaby, St 10"
        val desc = intent.getStringExtra("DESC") ?: "A cozy studio near the university..."
        val imageRes = intent.getIntExtra("IMAGE", R.drawable.home3)
        val mapLink = intent.getStringExtra("MAP_LINK") ?: "https://maps.google.com"
        val beds = intent.getStringExtra("BEDS") ?: "1"
        val baths = intent.getStringExtra("BATHS") ?: "1"
        val area = intent.getStringExtra("AREA") ?: "110"

        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailPrice).text = "$price EGP"
        findViewById<TextView>(R.id.tvDetailLocation).text = address
        findViewById<TextView>(R.id.tvDescription).text = desc
        findViewById<ImageView>(R.id.ivDetailImage).setImageResource(imageRes)

        setupFeatureBox(R.id.featureBed, R.drawable.bed, "$beds Bedroom")
        setupFeatureBox(R.id.featureBath, R.drawable.ic_bathroom, "$baths Bathroom")
        setupFeatureBox(R.id.featureArea, R.drawable.area, "$area m²")

        // --- إعداد الخريطة الذكية ---
        val webView = findViewById<WebView>(R.id.mapWebView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        // حل مشكلة التحول: استخدام CSS شفاف وواضح لكل مود
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

        // التحكم في الـ ForceDark برمجياً لضمان التحول
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            webView.settings.forceDark = if (isDarkMode) WebSettings.FORCE_DARK_ON else WebSettings.FORCE_DARK_OFF
        }

        // --- الأزرار ---
        findViewById<TextView>(R.id.btnOpenMaps).setOnClickListener {
            // 1. يفضل استخدام geo intent بدل اللينكات العادية لضمان فتح تطبيق الخرائط
            // لو الـ mapLink فيه إحداثيات، استخلصيها، لو مفيش استخدمي العنوان
            val gmmIntentUri = Uri.parse("geo:0,0?q=${address.replace(" ", "+")}")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            // 2. إجبار السيستم على فتح تطبيق خرائط جوجل تحديداً
            mapIntent.setPackage("com.google.android.apps.maps")

            // 3. شرط الدكتور: التأكد من وجود تطبيق للخرائط
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // لو مفيش تطبيق خرائط، نفتح اللينك في المتصفح كخيار بديل أخير
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapLink))
                startActivity(webIntent)
            }
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.cardLandlord).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // --- تصحيح زرار الاتصال (Implicit Intent) ---
        val btnCall = findViewById<Button>(R.id.btnCallLandlord)
        btnCall.setOnClickListener {
            val phoneNumber = "0123456789"
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }

            // شرط الدكتور: التأكد من وجود تطبيق للاتصال (معالجة الأخطاء)
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

        // تفتيح لون الكروت الصغيرة في الدارك مود (#333333 أفتح من #1E1E1E)
        updateFeatureCardStyle(R.id.featureBed, "#333333", Color.WHITE)
        updateFeatureCardStyle(R.id.featureBath, "#333333", Color.WHITE)
        updateFeatureCardStyle(R.id.featureArea, "#333333", Color.WHITE)

        val cardLandlord = findViewById<androidx.cardview.widget.CardView>(R.id.cardLandlord)
        cardLandlord.setCardBackgroundColor("#252525".toColorInt()) // أفتح شوية
        findViewById<TextView>(R.id.tvLandlordName).setTextColor(Color.WHITE)
        findViewById<TextView>(R.id.tvOwnerLabel).setTextColor("#888888".toColorInt())
    }

    private fun applyLightModeSettings() {
        // التأكد من أن الكروت في اللايت مود ترجع لألوانها الأصلية (أبيض أو رمادي فاتح جداً)
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
        if (view != null) {
            view.findViewById<ImageView>(R.id.ivFeatureIcon).setImageResource(iconId)
            view.findViewById<TextView>(R.id.tvFeatureValue).text = text
        }
    }
}