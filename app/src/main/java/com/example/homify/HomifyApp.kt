package com.example.homify

import android.app.Application
import data.AppDatabase

class HomifyApp : Application() {

    // instance واحدة من الداتابيز طول عمر التطبيق
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}