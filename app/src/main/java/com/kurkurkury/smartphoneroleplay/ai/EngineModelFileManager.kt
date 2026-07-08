package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

class EngineModelFileManager(private val context: Context) {
    companion object {
        private const val ENGINE_MODEL_DIR = "engine_model"
        private val SUPPORTED_EXTENSIONS = setOf("task", "litertlm")
    }

    fun engineModelDirectory(): File {
        return File(context.filesDir, ENGINE_MODEL_DIR).apply { if (!exists()) mkdirs() }
    }

    fun engineModelFile(): File {
        val dir = engineModelDirectory()
        val existing = dir.listFiles()
            ?.filter { it.isFile && extensionOf(it.name) in SUPPORTED_EXTENSIONS && it.length() > 0 }
            ?.maxByOrNull { it.lastModified() }
        return existing ?: File(dir, "engine_model.task")
    }

    fun engineModelExists(): Boolean = engineModelFile().exists() && engineModelFile().length() > 0

    fun engineModelStatusMessage(): String {
        return if (engineModelExists()) {
            val file = engineModelFile()
            val compatibility = EngineModelCompatibility.fromFileName(file.name)
            "Engine-Modellpaket gefunden: ${file.name} (${file.length() / 1024 / 1024} MB)\n${compatibility.engineName}: ${compatibility.message}"
        } else {
            "Kein Engine-Modellpaket importiert (.task/.litertlm erwartet)"
        }
    }

    fun importEngineModel(uri: Uri): ModelImportResult {
        return try {
            val originalName = displayName(uri)
            val compatibility = EngineModelCompatibility.fromFileName(originalName)
            if (!compatibility.supported) {
                return ModelImportResult(false, "Engine-Modellimport nicht moeglich: ${compatibility.message}")
            }
            val extension = compatibility.extension

            val target = File(engineModelDirectory(), "engine_model.$extension")
            context.contentResolver.openInputStream(uri).use { input ->
                if (input == null) return ModelImportResult(false, "Engine-Modellimport fehlgeschlagen: Datei konnte nicht geoeffnet werden.")
                target.outputStream().use { output -> input.copyTo(output) }
            }
            ModelImportResult(true, "Engine-Modell importiert: ${target.name} (${target.length() / 1024 / 1024} MB)\n${compatibility.engineName}: ${compatibility.message}")
        } catch (error: Throwable) {
            ModelImportResult(false, "Engine-Modellimport fehlgeschlagen: ${error.message ?: error.javaClass.simpleName}")
        }
    }

    fun clearEngineModel(): ModelImportResult {
        val dir = engineModelDirectory()
        val files = dir.listFiles()?.filter { it.isFile } ?: emptyList()
        if (files.isEmpty()) return ModelImportResult(true, "Kein Engine-Modell vorhanden.")
        val failed = files.filterNot { it.delete() }
        return if (failed.isEmpty()) {
            ModelImportResult(true, "Engine-Modell geloescht. Die App nutzt wieder GGUF Safe Mode oder Demo-Modus.")
        } else {
            ModelImportResult(false, "Engine-Modell konnte nicht vollstaendig geloescht werden. App neu starten und erneut versuchen.")
        }
    }

    private fun displayName(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                val name = cursor.getString(index)
                if (!name.isNullOrBlank()) return name
            }
        }
        return uri.lastPathSegment ?: "engine_model"
    }

    private fun extensionOf(name: String): String {
        return name.substringAfterLast('.', missingDelimiterValue = "").lowercase()
    }
}
