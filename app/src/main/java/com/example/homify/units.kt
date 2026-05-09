package com.example.homify

/**
 * Data model representing a property unit listing.
 */
data class units(
    val id: Int,
    val name: String,
    val landlord: String,
    val details: String,        // e.g. "Central District • 450 sqft • Fully Furnished"
    val price: String,          // e.g. "$1,200/mo"
    // val status: UnitStatus,
    val imagePath: String? = null,
    val imageResId: Int = android.R.drawable.ic_menu_gallery

)

/*enum class UnitStatus {
    PENDING_APPROVAL,
    ACTIVE,
    DRAFT
}*/
