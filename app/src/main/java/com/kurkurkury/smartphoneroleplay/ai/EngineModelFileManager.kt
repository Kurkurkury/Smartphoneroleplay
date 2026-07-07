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

    fun engineModelFile(): File {
        val dir = File(context.filesDir, ENGINE_MODEL_DIR)
        val existing = dir.listFiles()
            ?.filter { it.isFile && extensionOf(it.name) in SUPPORTED_EXTENSIONS && it.length() > 0 }
            ?.maxByOrNull { it.lastModified() }
        return existing ?: File(dir, "engine_model.task")
    }

    fun engineModelExists(): Boolean = engineModelFile().exists() && engineModelFile().length() > 0

    fun engineModelStatusMessage(): String {
        return if (engineModelExists()) {
            "Engine-Modellpaket gefunden: ${engineModelFile().name} (${engineModelFile().length() / 1024 / 1024} MB)"
        } else {
            "Kein Engine-Modellpaket importiert (.task/.litertlm erwartet)"
        }
    }

    fun importEngineModel(uri: Uri): ModelImportResult {
        return try {
            val originalName = displayName(uri)
            val extension = extensionOf(originalName)
            if (extension !in SUPPORTED_EXTENSIONS) {
                return ModelImportResult(false, "Engine-Modellimport nicht moeglich: Bitte .task oder .litertlm auswaehlen.")
            }

            val target = File(context.filesDir, "$ENGINE_MODEL_DIR/engine_model.$extension")
            target.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri).use { input ->
                if (input == null) return ModelImportResult(false, "Engine-Modellimport fehlgeschlagen: Datei konnte nicht geoeffnet werden.")
                target.outputStream().use { output -> input.copyTo(output) }
            }
            ModelImportResult(true, "Engine-Modell importiert: ${target.name} (${target.length() / 1024 / 1024} MB)")
        } catch (error: Throwable) {
            ModelImportResult(false, "Engine-Modellimport fehlgeschlagen: ${error.message ?: error.javaClass.simpleName}")
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
