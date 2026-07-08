package com.kurkurkury.smartphoneroleplay.ai

import java.io.File

class NativeLlamaBridge {
    companion object {
        private const val ENABLE_NATIVE_CHAT_GENERATION = true

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
            return "Native llama.cpp ist nicht aktiv: ${loadFailure?.javaClass?.simpleName ?: "Bibliothek fehlt"}. Demo-Modus laeuft stabil."
        }
        return try { nativeStatus() } catch (error: Throwable) {
            "Native llama.cpp konnte nicht abgefragt werden: ${error.message ?: error.javaClass.simpleName}. Demo-Modus bleibt aktiv."
        }
    }

    fun diagnostic(modelPath: String): NativeGenerationResult {
        val modelFile = File(modelPath)
        val lines = mutableListOf<String>()
        lines += "Native GGUF Diagnose"
        lines += "Engine-Strategie: GGUF wird direkt ueber llama.cpp native verwendet, wenn Library und Modell verfuegbar sind."
        lines += "Library geladen: ${if (isAvailable) "JA" else "NEIN"}"
        lines += "Native Status: ${status()}"
        lines += "Modellpfad vorhanden: ${if (modelFile.exists()) "JA" else "NEIN"}"
        lines += "Modellgroesse: ${if (modelFile.exists()) "${modelFile.length() / 1024 / 1024} MB" else "unbekannt"}"
        lines += "Chat-Native-Modus: ${if (ENABLE_NATIVE_CHAT_GENERATION) "AKTIV" else "DEAKTIVIERT"}"
        if (isAvailable && modelFile.exists()) {
            lines += ""
            lines += modelLoadDiagnostic(modelPath).text
            lines += ""
            lines += contextDiagnostic(modelPath).text
            lines += ""
            lines += sessionDecodeDiagnostic(modelPath).text
        }
        return NativeGenerationResult(ok = isAvailable && modelFile.exists(), text = lines.joinToString("\n"))
    }

    fun contextDiagnostic(modelPath: String): NativeGenerationResult {
        val modelFile = File(modelPath)
        if (!isAvailable) return NativeGenerationResult(false, status())
        if (!modelFile.exists()) return NativeGenerationResult(false, "Kontext-Test\nFehler: Modellpfad existiert nicht.")
        return try {
            val text = nativeContextDiagnostic(modelPath).trim()
            NativeGenerationResult(
                ok = text.contains("Kontext-Erstellung: OK"),
                text = text.ifBlank { "Kontext-Test\nFehler: Native Test lieferte keine Ausgabe." }
            )
        } catch (error: Throwable) {
            NativeGenerationResult(false, "Kontext-Test\nFehler: ${error.message ?: error.javaClass.simpleName}.")
        }
    }

    fun modelLoadDiagnostic(modelPath: String): NativeGenerationResult {
        val modelFile = File(modelPath)
        if (!isAvailable) return NativeGenerationResult(false, status())
        if (!modelFile.exists()) return NativeGenerationResult(false, "Modell-Load-Test\nFehler: Modellpfad existiert nicht.")
        return try {
            val text = nativeModelLoadDiagnostic(modelPath).trim()
            NativeGenerationResult(
                ok = text.contains("Modell-Load: OK"),
                text = text.ifBlank { "Modell-Load-Test\nFehler: Native Test lieferte keine Ausgabe." }
            )
        } catch (error: Throwable) {
            NativeGenerationResult(false, "Modell-Load-Test\nFehler: ${error.message ?: error.javaClass.simpleName}.")
        }
    }

    fun sessionDecodeDiagnostic(modelPath: String): NativeGenerationResult {
        val modelFile = File(modelPath)
        if (!isAvailable) return NativeGenerationResult(false, status())
        if (!modelFile.exists()) return NativeGenerationResult(false, "Session-Decode-Test\nFehler: Modellpfad existiert nicht.")
        return try {
            val text = nativeMiniInferenceDiagnostic(modelPath).trim()
            NativeGenerationResult(
                ok = text.contains("Session-Decode: OK"),
                text = text.ifBlank { "Session-Decode-Test\nFehler: Native Test lieferte keine Ausgabe." }
            )
        } catch (error: Throwable) {
            NativeGenerationResult(false, "Session-Decode-Test\nFehler: ${error.message ?: error.javaClass.simpleName}.")
        }
    }

    fun generate(modelPath: String, prompt: String): NativeGenerationResult {
        if (!ENABLE_NATIVE_CHAT_GENERATION) {
            return NativeGenerationResult(false, "Native llama.cpp-Generierung ist deaktiviert.")
        }
        if (!isAvailable) return NativeGenerationResult(false, status())
        val modelFile = File(modelPath)
        if (!modelFile.exists()) return NativeGenerationResult(false, "Native GGUF-Generierung fehlgeschlagen: Modellpfad existiert nicht.")
        return try {
            val text = nativeGenerate(modelPath, prompt).trim()
            if (text.isBlank() || text.startsWith("Fehler:")) NativeGenerationResult(false, text.ifBlank { "Native KI hat keine Ausgabe erzeugt." }) else NativeGenerationResult(true, text)
        } catch (error: Throwable) {
            NativeGenerationResult(false, "Native KI-Generierung fehlgeschlagen: ${error.message ?: error.javaClass.simpleName}.")
        }
    }

    private external fun nativeStatus(): String
    private external fun nativeContextDiagnostic(modelPath: String): String
    private external fun nativeModelLoadDiagnostic(modelPath: String): String
    private external fun nativeMiniInferenceDiagnostic(modelPath: String): String
    private external fun nativeGenerate(modelPath: String, prompt: String): String
}

data class NativeGenerationResult(
    val ok: Boolean,
    val text: String
)
