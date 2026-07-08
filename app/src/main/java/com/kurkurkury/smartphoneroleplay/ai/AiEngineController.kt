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
        val nativeText = if (nativeBridge.isAvailable && nativeBridge.isNativeChatGenerationEnabled) {
            "llama.cpp Runtime: AKTIV"
        } else {
            "llama.cpp Runtime: NICHT AKTIV - ${nativeBridge.status()}"
        }
        val mediaPipeText = if (engineModelFileManager.engineModelExists()) {
            "MediaPipe Runtime: BEREIT FUER TEST"
        } else {
            "MediaPipe Runtime: wartet auf .task/.litertlm Engine-Modell"
        }
        return "KI-Engine: GGUF bevorzugt\n$ggufText\n$nativeText\n${engineModelFileManager.engineModelStatusMessage()}\n$mediaPipeText"
    }

    fun canUseNativeChat(): Boolean = fileManager.modelExists() && nativeBridge.isAvailable && nativeBridge.isNativeChatGenerationEnabled

    fun diagnosticText(): String = buildString {
        appendLine("KI-Engine Diagnose")
        appendLine("Aktiver Chat-Modus: ${if (canUseNativeChat()) "GGUF llama.cpp" else if (engineModelFileManager.engineModelExists()) "MediaPipe Runtime" else "Demo-Fallback"}")
        appendLine("GGUF llama.cpp: ${if (nativeBridge.isAvailable) "Library geladen" else "Library nicht geladen"}")
        appendLine("GGUF Chat: ${if (nativeBridge.isNativeChatGenerationEnabled) "AKTIV" else "DEAKTIVIERT"}")
        appendLine("")
        appendLine(statusText())
        appendLine("")
        appendLine("Modellprioritaet:")
        appendLine("1. GGUF Import = bevorzugter lokaler Chatpfad ueber llama.cpp")
        appendLine("2. Engine-Modell Import = MediaPipe .task/.litertlm Fallbackpfad")
        appendLine("3. Demo-Modus = Fallback ohne Modell")
        appendLine("")
        appendLine("Android-Engine-Pfade:")
        appendLine("1. llama.cpp native")
        appendLine("   Status: ${nativeBridge.status()}")
        plannedEngines.forEachIndexed { index, engine ->
            appendLine("${index + 2}. ${engine.displayName}")
            appendLine("   Status: ${engine.status()}")
        }
    }
}
