package com.kurkurkury.smartphoneroleplay.ai

import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

interface AndroidLocalLlmEngine {
    val id: String
    val displayName: String

    fun isAvailable(): Boolean

    fun status(): String

    fun generate(
        modelPath: String,
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): EngineGenerationResult
}

data class EngineGenerationResult(
    val ok: Boolean,
    val text: String,
    val engineId: String
)
