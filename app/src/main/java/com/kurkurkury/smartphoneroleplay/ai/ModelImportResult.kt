package com.kurkurkury.smartphoneroleplay.ai

data class ModelImportResult(
    val ok: Boolean,
    val message: String,
    val bytesCopied: Long = 0L
)
