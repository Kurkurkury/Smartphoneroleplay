package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import android.net.Uri
import java.io.File

class EngineModelFileManager(private val context: Context) {
    companion object {
        private const val ENGINE_MODEL_DIR = "engine_model"
        private const val ENGINE_MODEL_FILE = "engine_model.bin"
    }

    fun engineModelFile(): File = File(context.filesDir, "$ENGINE_MODEL_DIR/$ENGINE_MODEL_FILE")

    fun engineModelExists(): Boolean = engineModelFile().exists() && engineModelFile().length() > 0

    fun engineModelStatusMessage(): String {
        return if (engineModelExists()) {
            "Engine-Modellpaket gefunden: ${engineModelFile().length() / 1024 / 1024} MB"
        } else {
            "Kein Engine-Modellpaket importiert"
        }
    }

    fun importEngineModel(uri: Uri): ModelImportResult {
        return try {
            val target = engineModelFile()
            target.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri).use { input ->
                if (input == null) return ModelImportResult(false, "Engine-Modellimport fehlgeschlagen: Datei konnte nicht geoeffnet werden.")
                target.outputStream().use { output -> input.copyTo(output) }
            }
            ModelImportResult(true, "Engine-Modell importiert: ${target.length() / 1024 / 1024} MB")
        } catch (error: Throwable) {
            ModelImportResult(false, "Engine-Modellimport fehlgeschlagen: ${error.message ?: error.javaClass.simpleName}")
        }
    }
}
