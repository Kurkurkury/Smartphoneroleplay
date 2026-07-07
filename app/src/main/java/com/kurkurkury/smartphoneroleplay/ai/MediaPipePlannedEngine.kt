package com.kurkurkury.smartphoneroleplay.ai

import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class MediaPipePlannedEngine : AndroidLocalLlmEngine {
    override val id: String = "mediapipe-planned"
    override val displayName: String = "MediaPipe LLM geplant"

    override fun isAvailable(): Boolean = false

    override fun status(): String {
        return "MediaPipe LLM Adapter vorhanden, Runtime noch nicht aktiviert. Benoetigt kompatibles MediaPipe/AI-Edge Modell statt GGUF."
    }

    override fun generate(
        modelPath: String,
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): EngineGenerationResult {
        return EngineGenerationResult(
            ok = false,
            text = "MediaPipe LLM ist vorbereitet, aber noch nicht als Runtime aktiviert. Aktueller sicherer Modus bleibt Demo-Fallback.",
            engineId = id
        )
    }
}
