package com.example.letsgetweddi.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val userType: String = "unknown" // bride/groom
)
