package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context

class AiEngineController(context: Context) {
    private val appContext = context.applicationContext
    private val fileManager = OnDeviceModelFileManager(appContext)
    private val engineModelFileManager = EngineModelFileManager(appContext)
    private val mode: AiEngineMode = AiEngineMode.safeDefault
    private val plannedEngines: List<AndroidLocalLlmEngine> = listOf(
        MediaPipePlannedEngine(appContext),
        MlcPlannedEngine()
    )

    fun currentMode(): AiEngineMode = mode

    fun statusText(): String {
        val ggufText = if (fileManager.modelExists()) {
            "GGUF-Modell importiert: ${fileManager.modelFile().length() / 1024 / 1024} MB"
        } else {
            "Kein GGUF-Modell importiert"
        }
        val runtimeText = if (engineModelFileManager.engineModelExists()) {
            "MediaPipe Runtime: BEREIT FUER TEST"
        } else {
            "MediaPipe Runtime: wartet auf .task/.litertlm Engine-Modell"
        }
        return "KI-Engine: MediaPipe Runtime\n$ggufText\n${engineModelFileManager.engineModelStatusMessage()}\n$runtimeText\n${mode.description}"
    }

    fun canUseNativeChat(): Boolean = engineModelFileManager.engineModelExists()

    fun diagnosticText(): String = buildString {
        appendLine("KI-Engine Diagnose")
        appendLine("Aktiver Chat-Modus: ${if (canUseNativeChat()) "MediaPipe Runtime" else "Demo-Fallback"}")
        appendLine("llama.cpp Decode: DEAKTIVIERT")
        appendLine("Grund: direkter llama.cpp Decode crasht auf dem Testgeraet nativ.")
        appendLine("")
        appendLine(statusText())
        appendLine("")
        appendLine("Modelltrennung:")
        appendLine("1. GGUF Import = alter llama.cpp Status-/Importtest")
        appendLine("2. Engine-Modell Import = MediaPipe .task/.litertlm Modellpaket")
        appendLine("")
        appendLine("Android-Engine-Pfade:")
        plannedEngines.forEachIndexed { index, engine ->
            appendLine("${index + 1}. ${engine.displayName}")
            appendLine("   Status: ${engine.status()}")
        }
        appendLine("")
        appendLine("Wenn ein kompatibles Engine-Modell importiert ist, versucht der normale Chat automatisch MediaPipe und faellt bei Fehler sauber auf Demo zurueck.")
    }
}
