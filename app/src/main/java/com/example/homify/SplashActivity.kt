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

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo        = findViewById<ImageView>(R.id.logo)
        val progress    = findViewById<View>(R.id.progressBar)
        val progressBg  = findViewById<View>(R.id.v_progress_bg)

        // أنيميشن اللوجو (نفس الكود القديم)
        logo.scaleX = 0f
        logo.scaleY = 0f
        logo.alpha  = 0f
        logo.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator())
            .start()

        // أنيميشن شريط التحميل (نفس الكود القديم)
        progressBg.post {
            val targetWidth = progressBg.width.toFloat()
            ValueAnimator.ofFloat(0f, targetWidth).apply {
                duration = 2000
                addUpdateListener {
                    progress.layoutParams.width = (it.animatedValue as Float).toInt()
                    progress.requestLayout()
                }
                start()
            }
        }

        // بعد انتهاء الأنيميشن، نقرر نروح فين
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2800)
    }

    // ════════════════════════════════════════════
    //  الجديد: نشوف لو في session محفوظة
    // ════════════════════════════════════════════
    private fun navigateToNextScreen() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId     = sharedPref.getInt("userId", -1)
        val role       = sharedPref.getString("role", null)

        // لو في userId محفوظ = المستخدم سبق وعمل login
        val intent = if (userId != -1 && role != null) {
            when {
                role.equals("Admin",    ignoreCase = true) -> Intent(this, DashboardActivity::class.java)
                role.equals("Landlord", ignoreCase = true) -> Intent(this, LandlordHomeActivity::class.java)
                else                                       -> Intent(this, TenantHome::class.java)
            }
        } else {
            // مفيش session = يروح للـ Onboarding
            Intent(this, OnboardingActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}