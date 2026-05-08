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

class PropertyDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // جلب البيانات من الـ Intent
        val title = intent.getStringExtra("TITLE") ?: "The Green House"
        val price = intent.getStringExtra("PRICE") ?: "650"
        val address = intent.getStringExtra("ADDRESS") ?: "Headingley, St 5"
        val desc = intent.getStringExtra("DESC") ?: "A cozy studio near the university..."
        val imageRes = intent.getIntExtra("IMAGE", R.drawable.home3)
        val mapLink = intent.getStringExtra("MAP_LINK") ?: "https://maps.google.com"
        val beds = intent.getStringExtra("BEDS") ?: "1"
        val baths = intent.getStringExtra("BATHS") ?: "1"
        val area = intent.getStringExtra("AREA") ?: "110"

        // ربط الواجهة بالبيانات
        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailPrice).text = "$price EGP"
        findViewById<TextView>(R.id.tvDetailLocation).text = address
        findViewById<TextView>(R.id.tvDescription).text = desc
        findViewById<ImageView>(R.id.ivDetailImage).setImageResource(imageRes)

        setupFeatureBox(R.id.featureBed, R.drawable.bed, "$beds Bedroom")
        setupFeatureBox(R.id.featureBath, R.drawable.ic_bathroom, "$baths Bathroom")
        setupFeatureBox(R.id.featureArea, R.drawable.area, "$area m²")

        // إعداد الخريطة
        val webView = findViewById<WebView>(R.id.mapWebView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        val formattedAddress = address.replace(" ", "+")
        val htmlData = """
            <html><body style="margin:0;padding:0;">
                <iframe width="100%" height="100%" style="border:0;" 
                    src="https://www.google.com/maps?q=$formattedAddress&output=embed">
                </iframe>
            </body></html>
        """.trimIndent()
        webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)

        // الأزرار والتنقل (Intents)
        findViewById<TextView>(R.id.btnOpenMaps).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapLink))
            startActivity(intent)
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

    private fun setupFeatureBox(viewId: Int, iconId: Int, text: String) {
        val view = findViewById<View>(viewId)
        if (view != null) {
            view.findViewById<ImageView>(R.id.ivFeatureIcon).setImageResource(iconId)
            view.findViewById<TextView>(R.id.tvFeatureValue).text = text
        }
    }
}