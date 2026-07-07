package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context

class AiEngineController(context: Context) {
    private val fileManager = OnDeviceModelFileManager(context)
    private val mode: AiEngineMode = AiEngineMode.safeDefault
    private val plannedEngines: List<AndroidLocalLlmEngine> = listOf(
        MediaPipePlannedEngine(),
        MlcPlannedEngine()
    )

    fun currentMode(): AiEngineMode = mode

    fun statusText(): String {
        val modelText = if (fileManager.modelExists()) {
            "GGUF-Modell importiert: ${fileManager.modelFile().length() / 1024 / 1024} MB"
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
        appendLine("Grund: direkter llama.cpp Decode crasht auf dem Testgeraet nativ.")
        appendLine("Sicherer Betrieb: Demo-Fallback bleibt aktiv.")
        appendLine("")
        appendLine(statusText())
        appendLine("")
        appendLine("Vorbereitete Android-Engine-Pfade:")
        plannedEngines.forEachIndexed { index, engine ->
            appendLine("${index + 1}. ${engine.displayName}")
            appendLine("   Status: ${engine.status()}")
        }
        appendLine("")
        appendLine("Wichtig: Dein aktuelles GGUF-Modell bleibt als Import-Test nuetzlich, aber MediaPipe/MLC brauchen voraussichtlich ein kompatibles Engine-Modellpaket statt direkter GGUF-Inferenz.")
    }
}
