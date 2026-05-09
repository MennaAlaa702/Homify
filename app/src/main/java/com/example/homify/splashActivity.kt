package com.example.homify

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class splashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR

        val logo = findViewById<ImageView>(R.id.logo)
        val progress = findViewById<View>(R.id.progressBar)
        // ضفنا السطر ده عشان نمسك الخلفية الشفافة بتاعة الشريط
        val progressBg = findViewById<View>(R.id.v_progress_bg)

        // إخفاء اللوجو في البداية عشان الأنيميشن
        logo.scaleX = 0f
        logo.scaleY = 0f
        logo.alpha = 0f

        // 1. أنيميشن اللوجو (بياخد 800 ملي ثانية)
        logo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator())
            .start()

        // 2. أنيميشن شريط التحميل
        progressBg.post {
            // التعديل هنا: أخدنا عرض الخلفية (progressBg) مش عرض الشاشة كلها
            val targetWidth = progressBg.width.toFloat()

            val animator = ValueAnimator.ofFloat(0f, targetWidth)

            animator.duration = 2000
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                progress.layoutParams.width = value.toInt()
                progress.requestLayout()
            }
            animator.start()
        }

        // 3. الانتقال للشاشة التانية بعد ما الأنيميشن يخلص (800 + 2000 = 2800)
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val userId = sharedPref.getInt("userId", -1)
            val role = sharedPref.getString("role", null)

            val intent = if (userId != -1 && role != null) {
                when (role.lowercase()) {
                    "landlord" -> Intent(this, landlordHomeActivity::class.java)
                    "admin"    -> Intent(this, dashboardActivity::class.java)
                    else       -> Intent(this, tenantHome::class.java)
                }
            } else {
                Intent(this, onboardingActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 2800)
    }
}