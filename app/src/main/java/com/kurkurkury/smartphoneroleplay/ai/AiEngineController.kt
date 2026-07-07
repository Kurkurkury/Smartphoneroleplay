package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context

class AiEngineController(context: Context) {
    private val fileManager = OnDeviceModelFileManager(context)
    private val mode: AiEngineMode = AiEngineMode.safeDefault

    fun currentMode(): AiEngineMode = mode

    fun statusText(): String {
        val modelText = if (fileManager.modelExists()) {
            "Modell importiert: ${fileManager.modelFile().length() / 1024 / 1024} MB"
        } else {
            "Kein GGUF-Modell importiert"
        }
        return "KI-Engine: ${mode.label}\n$modelText\n${mode.description}"
    }

    fun canUseNativeChat(): Boolean = false

    fun diagnosticText(): String = buildString {
        appendLine("KI-Engine Diagnose")
        appendLine("Aktiver Modus: ${mode.label}")
        appendLine("Native Chat-Inferenz: DEAKTIVIERT")
        appendLine("Grund: llama.cpp Decode crasht auf dem Testgeraet nativ.")
        appendLine("Sicherer Betrieb: Demo-Fallback bleibt aktiv.")
        appendLine("Naechster Engine-Pfad: MediaPipe/MLC statt direktem llama.cpp Decode.")
        appendLine("")
        append(statusText())
    }
}
