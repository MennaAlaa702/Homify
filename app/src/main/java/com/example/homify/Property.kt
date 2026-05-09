package com.example.homify

data class Property(
    val id: Int = 0,
    val title: String,
    val price: String,
    val governorate: String,
    val address: String,
    val description: String,
    val imageUrl: String,
    val bedrooms: String,
    val bathrooms: String,
    val size: String,
    val type: String,
    val amenities: List<String>,
    val locationLink: String
)