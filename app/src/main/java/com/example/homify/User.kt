package com.example.homify

data class User(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val nationalId: String = "",
    val userRole: String = "tenant",
    val unitsCount: String = "0"
)