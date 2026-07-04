package com.kurkurkury.smartphoneroleplay.ai

object OnDeviceLlamaConfig {
    const val modelFileName = "local-roleplay-model.gguf"
    const val modelFolderName = "models"
    const val contextSize = 2048
    const val maxNewTokens = 220
    const val temperature = 0.8f
    const val topP = 0.95f

    fun expectedRelativePath(): String = "$modelFolderName/$modelFileName"
}
