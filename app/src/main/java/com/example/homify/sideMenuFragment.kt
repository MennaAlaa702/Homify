package com.example.homify

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatDelegate
import java.io.File

class sideMenuFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_side_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. ربط العناصر (Views)
        val menuHome = view.findViewById<TextView>(R.id.menu_home)
        val menuProfile = view.findViewById<TextView>(R.id.menu_profile)
        val menuDarkMode = view.findViewById<TextView>(R.id.menu_dark_mode)
        val menuLightMode = view.findViewById<TextView>(R.id.menu_light_mode)
        val menuLogout = view.findViewById<TextView>(R.id.menu_logout)
        val tvUsername = view.findViewById<TextView>(R.id.tv_menu_name)
        val ivMenuProfile = view.findViewById<ImageView>(R.id.iv_menu_profile)

        // 2. جلب بيانات المستخدم الحقيقية من SharedPreferences (من النسخة التانية)
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // جلب الاسم
        val username = sharedPref.getString("username", "Guest") ?: "Guest"
        tvUsername?.text = username

        // جلب الصورة
        val imagePath = sharedPref.getString("profile_image_path", null)
        if (imagePath != null) {
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                ivMenuProfile?.setImageBitmap(bitmap)
            }
        }

        // =====================================================================
        //                            برمجة الأزرار
        // =====================================================================

        // زرار الـ Home (توجيه ذكي حسب نوع المستخدم)
        menuHome.setOnClickListener {
            val role = sharedPref.getString("role", "Tenant") ?: "Tenant"

            val intent = if (role.equals("Landlord", ignoreCase = true)) {
                Intent(requireContext(), landlordHomeActivity::class.java)
            } else if (role.equals("Admin", ignoreCase = true)) { // لو حابة تضيفي أدمن مستقبلاً
                Intent(requireContext(), dashboardActivity::class.java)
            } else {
                Intent(requireContext(), tenantHome::class.java)
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            dismiss()
        }

        // زرار الـ Profile
        menuProfile.setOnClickListener {
            val intent = Intent(requireContext(), profileActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        // أزرار المظهر (Dark / Light)
        menuDarkMode.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            dismiss()
        }

        menuLightMode.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            dismiss()
        }


        // زرار الـ Logout
        menuLogout.setOnClickListener {
            // مسح بيانات الجلسة الحالية
            sharedPref.edit().clear().apply()

            val intent = Intent(requireContext(), onboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (window != null) {
            // ضبط العرض ليكون 75% من الشاشة
            val width = (resources.displayMetrics.widthPixels * 0.75).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT)

            // المنيو تفتح من اليمين
            window.setGravity(Gravity.END)

            // استخدام لون الكارت الذكي كخلفية للمنيو (عشان تتجاوب مع الدارك مود)
            window.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.card_background, null)))
        }
    }
}