package com.kurkurkury.smartphoneroleplay.ai

class NativeLlamaBridge {
    companion object {
        private val loadFailure: Throwable? = try {
            System.loadLibrary("smartphoneroleplay_native")
            null
        } catch (error: Throwable) {
            error
        }
    }

    val isAvailable: Boolean
        get() = loadFailure == null

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

    fun generate(modelPath: String, prompt: String): NativeGenerationResult {
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
