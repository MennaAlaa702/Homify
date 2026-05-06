package com.example.homify

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatDelegate


class SideMenuFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // بنربط ملف التصميم
        return inflater.inflate(R.layout.fragment_side_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // تعريف الزراير
        val menuHome = view.findViewById<TextView>(R.id.menu_home)
        val menuProfile = view.findViewById<TextView>(R.id.menu_profile)
        val menuDarkMode = view.findViewById<TextView>(R.id.menu_dark_mode)
        val menuLightMode = view.findViewById<TextView>(R.id.menu_light_mode)
        val menuLanguage = view.findViewById<TextView>(R.id.menu_language)
        val menuLogout = view.findViewById<TextView>(R.id.menu_logout)

        // برمجة الزراير (كل زرار بيعمل حاجة)
        menuHome.setOnClickListener {
            // بما إننا في الـ Home أصلاً هنقفل القائمة بس
            dismiss()
        }

        menuProfile.setOnClickListener {
            // startActivity(Intent(requireContext(), ProfileActivity::class.java))
            Toast.makeText(context, "Profile Clicked", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Change Language Clicked", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        menuLogout.setOnClickListener {
            // بيقفل الشاشة دي ويفتح شاشة اللوجين
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dismiss()
        }
    }

    // الدالة دي هي اللي بتظبط المقاس وتخليه يظهر من الجنب مش في النص
    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (window != null) {
            // بنخلي العرض ياخد 75% من الشاشة، والطول الشاشة كلها
            val width = (resources.displayMetrics.widthPixels * 0.75).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT)

            // بنخلي القائمة تظهر من اليمين (عشان أيقونة المنيو عندك في اليمين)
            window.setGravity(Gravity.END)

            // خلفية شفافة عشان حواف القائمة تبان مظبوطة
            window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

}