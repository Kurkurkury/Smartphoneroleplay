package com.kurkurkury.smartphoneroleplay.ai

import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class MlcPlannedEngine : AndroidLocalLlmEngine {
    override val id: String = "mlc-planned"
    override val displayName: String = "MLC LLM geplant"

    override fun isAvailable(): Boolean = false

    override fun status(): String {
        return "MLC LLM Adapter vorhanden, Runtime noch nicht aktiviert. Benoetigt MLC-kompiliertes Modellpaket statt direkter GGUF-Datei."
    }

    override fun generate(
        modelPath: String,
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): EngineGenerationResult {
        return EngineGenerationResult(
            ok = false,
            text = "MLC LLM ist vorbereitet, aber noch nicht als Runtime aktiviert. Aktueller sicherer Modus bleibt Demo-Fallback.",
            engineId = id
        )
    }
}
