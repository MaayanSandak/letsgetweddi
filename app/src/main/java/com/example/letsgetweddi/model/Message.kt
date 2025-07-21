package com.example.letsgetweddi.model

data class Message(
    val senderId: String? = null,
    val text: String? = null,
    val content: String? = null,
    val timestamp: Long = 0
)
