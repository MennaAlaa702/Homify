package com.homify.app.models

/**
 * Data model representing a platform user (tenant or admin).
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    // drawable resource id for avatar
    val avatarResId: Int = android.R.drawable.ic_menu_myplaces
)
