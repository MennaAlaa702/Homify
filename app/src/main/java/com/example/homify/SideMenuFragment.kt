package com.example.homify

import android.content.Context
import android.content.Intent
import android.graphics.Color
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

class SideMenuFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_side_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHome = view.findViewById<TextView>(R.id.menu_home)
        val menuProfile = view.findViewById<TextView>(R.id.menu_profile)
        val menuDarkMode = view.findViewById<TextView>(R.id.menu_dark_mode)
        val menuLightMode = view.findViewById<TextView>(R.id.menu_light_mode)
        val menuLanguage = view.findViewById<TextView>(R.id.menu_language)
        val menuLogout = view.findViewById<TextView>(R.id.menu_logout)

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "Guest") ?: "Guest"
        val tvUsername = view.findViewById<TextView>(R.id.tv_menu_name)
        tvUsername?.text = username
        val ivMenuProfile = view.findViewById<ImageView>(R.id.iv_menu_profile)
        val imagePath = sharedPref.getString("profile_image_path", null)
        if (imagePath != null) {
            val imgFile = java.io.File(imagePath)
            if (imgFile.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.absolutePath)
                ivMenuProfile.setImageBitmap(bitmap)
            }
        }
        // 1. برمجة زرار الـ Home بناءً على الـ Role
        menuHome.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val role = sharedPref.getString("role", "Tenant") // بيفترض إنه Tenant لو ملهاش قيمة

            val intent = if (role.equals("Landlord", ignoreCase = true)) {
                Intent(requireContext(), LandlordHomeActivity::class.java)
            } else {
                Intent(requireContext(), TenantHome::class.java)
            }

            // بننضف الـ Stack عشان ميبقاش في كذا نسخة من الـ Home مفتوحة
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            dismiss()
        }

        // 2. برمجة زرار الـ Profile
        menuProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        menuDarkMode.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            dismiss()
        }

        menuLightMode.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            dismiss()
        }

        menuLanguage.setOnClickListener {
            // هنا ممكن نبرمج تغيير اللغة لاحقاً
            dismiss()
        }

        // 3. برمجة زرار الـ Logout (بيودي لـ Onboarding)
        menuLogout.setOnClickListener {
            val intent = Intent(requireContext(), OnboardingActivity::class.java)
            // نستخدم Flags عشان اليوزر ميعرفش يعمل Back للـ Home بعد ما خرج
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (window != null) {
            val width = (resources.displayMetrics.widthPixels * 0.75).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT)
            window.setGravity(Gravity.END)

            // تعديل مهم: بنستخدم اللون الذكي بتاع الكروت عشان المنيو متبقاش بيضا في الدارك مود
            window.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.card_background, null)))
        }
    }
}