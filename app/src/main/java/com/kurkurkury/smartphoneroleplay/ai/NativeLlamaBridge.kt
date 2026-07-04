package com.kurkurkury.smartphoneroleplay.ai

import java.io.File

class NativeLlamaBridge {
    companion object {
        private const val ENABLE_NATIVE_CHAT_GENERATION = false

        private val loadFailure: Throwable? = try {
            System.loadLibrary("smartphoneroleplay_native")
            null
        } catch (error: Throwable) {
            error
        }
    }

    val isAvailable: Boolean
        get() = loadFailure == null

    val isNativeChatGenerationEnabled: Boolean
        get() = ENABLE_NATIVE_CHAT_GENERATION

    fun status(): String {
        if (!isAvailable) {
            return "Native KI ist nicht aktiv: ${loadFailure?.javaClass?.simpleName ?: "Bibliothek fehlt"}. Demo-Modus laeuft stabil."
        }

        return try {
            nativeStatus()
        } catch (error: Throwable) {
            "Native KI konnte nicht abgefragt werden: ${error.message ?: error.javaClass.simpleName}. Demo-Modus bleibt aktiv."
        }
    }

    fun diagnostic(modelPath: String): NativeGenerationResult {
        val modelFile = File(modelPath)
        val lines = mutableListOf<String>()
        lines += "Native Diagnose"
        lines += "Library geladen: ${if (isAvailable) "JA" else "NEIN"}"
        lines += "Native Status: ${status()}"
        lines += "Modellpfad vorhanden: ${if (modelFile.exists()) "JA" else "NEIN"}"
        lines += "Modellgroesse: ${if (modelFile.exists()) "${modelFile.length() / 1024 / 1024} MB" else "unbekannt"}"
        lines += "Chat-Native-Modus: ${if (ENABLE_NATIVE_CHAT_GENERATION) "AKTIV" else "SICHER DEAKTIVIERT"}"
        lines += "Hinweis: Die App nutzt bis zur stabilen Modell-Ladepruefung den Demo-Fallback, damit sie nicht abstuerzt."
        return NativeGenerationResult(ok = isAvailable && modelFile.exists(), text = lines.joinToString("\n"))
    }

    fun generate(modelPath: String, prompt: String): NativeGenerationResult {
        if (!ENABLE_NATIVE_CHAT_GENERATION) {
            return NativeGenerationResult(
                ok = false,
                text = "Native Chat-Generierung ist im Diagnose-Build sicher deaktiviert. Modell bleibt importiert; Demo-Fallback verhindert App-Absturz."
            )
        }

        if (!isAvailable) {
            return NativeGenerationResult(
                ok = false,
                text = status()
            )
        }

        return try {
            val text = nativeGenerate(modelPath, prompt).trim()
            if (text.isBlank() || text.startsWith("Fehler:")) {
                NativeGenerationResult(false, text.ifBlank { "Native KI hat keine Ausgabe erzeugt." })
            } else {
                NativeGenerationResult(true, text)
            }
        } catch (error: Throwable) {
            NativeGenerationResult(
                ok = false,
                text = "Native KI-Generierung fehlgeschlagen: ${error.message ?: error.javaClass.simpleName}."
            )
        }
    }

    private external fun nativeStatus(): String

    private external fun nativeGenerate(modelPath: String, prompt: String): String
}

data class NativeGenerationResult(
    val ok: Boolean,
    val text: String
)
