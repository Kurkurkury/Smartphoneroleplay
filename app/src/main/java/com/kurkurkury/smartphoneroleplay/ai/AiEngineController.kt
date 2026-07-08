package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context

class AiEngineController(context: Context) {
    private val appContext = context.applicationContext
    private val fileManager = OnDeviceModelFileManager(appContext)
    private val engineModelFileManager = EngineModelFileManager(appContext)
    private val nativeBridge = NativeLlamaBridge()
    private val plannedEngines: List<AndroidLocalLlmEngine> = listOf(
        MediaPipePlannedEngine(appContext),
        MlcPlannedEngine()
    )

    fun statusText(): String {
        val ggufText = if (fileManager.modelExists()) {
            "GGUF-Modell importiert: ${fileManager.modelFile().length() / 1024 / 1024} MB"
        } else {
            "Kein GGUF-Modell importiert"
        }
        val nativeText = if (nativeBridge.isAvailable) {
            "llama.cpp Runtime: Library geladen, direkter Chat im Hauptprozess gesperrt"
        } else {
            "llama.cpp Runtime: NICHT AKTIV - ${nativeBridge.status()}"
        }
        val mediaPipeText = if (engineModelFileManager.engineModelExists()) {
            "MediaPipe Runtime: BEREIT FUER TEST"
        } else {
            "MediaPipe Runtime: wartet auf .task/.litertlm Engine-Modell"
        }
        return "KI-Engine: stabiler Safe-Modus\n$ggufText\n$nativeText\n${engineModelFileManager.engineModelStatusMessage()}\n$mediaPipeText"
    }

    fun canUseNativeChat(): Boolean = false

    fun diagnosticText(): String = buildString {
        appendLine("KI-Engine Diagnose")
        appendLine("Aktiver Chat-Modus: ${if (engineModelFileManager.engineModelExists()) "MediaPipe Runtime" else "Demo/GGUF Safe Mode"}")
        appendLine("GGUF llama.cpp: ${if (nativeBridge.isAvailable) "Library geladen" else "Library nicht geladen"}")
        appendLine("GGUF Chat im Hauptprozess: GESPERRT nach Realgeraet-Crash")
        appendLine("")
        appendLine(statusText())
        appendLine("")
        appendLine("Modellprioritaet:")
        appendLine("1. MediaPipe Engine-Modell, wenn .task/.litertlm importiert ist")
        appendLine("2. Demo/GGUF Safe Mode, wenn nur GGUF importiert ist")
        appendLine("3. Demo-Modus ohne Modell")
        appendLine("")
        appendLine("Android-Engine-Pfade:")
        appendLine("1. llama.cpp native")
        appendLine("   Status: ${nativeBridge.status()}")
        appendLine("   Sicherheit: direkte Load/Decode-Tests laufen nicht mehr im Hauptprozess")
        plannedEngines.forEachIndexed { index, engine ->
            appendLine("${index + 2}. ${engine.displayName}")
            appendLine("   Status: ${engine.status()}")
        }
    }
}
