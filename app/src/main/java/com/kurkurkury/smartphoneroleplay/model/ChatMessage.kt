package com.kurkurkury.smartphoneroleplay.model

data class ChatMessage(
    val sender: String,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
