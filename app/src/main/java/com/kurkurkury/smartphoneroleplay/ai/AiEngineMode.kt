package com.kurkurkury.smartphoneroleplay.ai

enum class AiEngineMode(
    val label: String,
    val description: String
) {
    DEMO(
        label = "Demo-Fallback",
        description = "Stabiler Roleplay-Fallback ohne native Inferenz."
    ),
    LLAMA_CPP_DISABLED(
        label = "llama.cpp deaktiviert",
        description = "Native llama.cpp-Decode ist wegen Geraete-Crash gesperrt."
    ),
    MEDIAPIPE_PLANNED(
        label = "MediaPipe geplant",
        description = "Naechster stabiler Android-Engine-Pfad fuer lokale LLM-Inferenz."
    );

    companion object {
        val safeDefault: AiEngineMode = LLAMA_CPP_DISABLED
    }
}
