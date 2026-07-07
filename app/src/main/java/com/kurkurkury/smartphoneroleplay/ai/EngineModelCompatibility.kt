package com.kurkurkury.smartphoneroleplay.ai

data class EngineModelCompatibility(
    val supported: Boolean,
    val extension: String,
    val engineName: String,
    val message: String
) {
    companion object {
        fun fromFileName(fileName: String): EngineModelCompatibility {
            val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
            return when (extension) {
                "task" -> EngineModelCompatibility(
                    supported = true,
                    extension = extension,
                    engineName = "MediaPipe LLM",
                    message = ".task Modellpaket erkannt. Wird ueber MediaPipe LLM getestet."
                )
                "litertlm" -> EngineModelCompatibility(
                    supported = true,
                    extension = extension,
                    engineName = "LiteRT-LM",
                    message = ".litertlm Modellpaket erkannt. Wird als LiteRT-LM/MediaPipe-kompatibler Enginepfad behandelt."
                )
                "gguf" -> EngineModelCompatibility(
                    supported = false,
                    extension = extension,
                    engineName = "llama.cpp / GGUF",
                    message = "GGUF gehoert zum alten llama.cpp-Pfad und kann nicht als Engine-Modell verwendet werden."
                )
                "zip" -> EngineModelCompatibility(
                    supported = false,
                    extension = extension,
                    engineName = "Archiv",
                    message = "ZIP wird nicht direkt als Engine-Modell akzeptiert. Bitte das eigentliche .task/.litertlm Modellpaket auswaehlen."
                )
                else -> EngineModelCompatibility(
                    supported = false,
                    extension = extension.ifBlank { "unbekannt" },
                    engineName = "Unbekannt",
                    message = "Nicht unterstuetztes Engine-Modellformat. Erwartet wird .task oder .litertlm."
                )
            }
        }
    }
}
