package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import android.net.Uri
import java.io.File

class OnDeviceModelFileManager(private val context: Context) {
    fun modelDirectory(): File {
        return File(context.getExternalFilesDir(null), OnDeviceLlamaConfig.modelFolderName).apply {
            if (!exists()) mkdirs()
        }
    }

    fun modelFile(): File {
        return File(modelDirectory(), OnDeviceLlamaConfig.modelFileName)
    }

    fun modelExists(): Boolean {
        val file = modelFile()
        return file.exists() && file.length() > 1024L * 1024L
    }

    fun modelStatusMessage(): String {
        val file = modelFile()
        return if (modelExists()) {
            "Lokales Modell gefunden: ${formatBytes(file.length())}"
        } else {
            "Kein lokales Modell importiert. Tippe auf Modell und waehle eine GGUF-Datei."
        }
    }

    fun importModel(uri: Uri): ModelImportResult {
        return try {
            val target = modelFile()
            var bytesCopied = 0L
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        bytesCopied += read.toLong()
                    }
                    output.flush()
                }
            } ?: return ModelImportResult(false, "Datei konnte nicht geoeffnet werden.")

            if (bytesCopied <= 1024L * 1024L) {
                target.delete()
                ModelImportResult(false, "Import fehlgeschlagen: Datei ist zu klein.", bytesCopied)
            } else {
                ModelImportResult(true, "Modell importiert: ${formatBytes(bytesCopied)}", bytesCopied)
            }
        } catch (error: Exception) {
            ModelImportResult(false, "Import fehlgeschlagen: ${error.message ?: "unbekannter Fehler"}")
        }
    }

    private fun formatBytes(bytes: Long): String {
        val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) {
            String.format("%.2f GB", gb)
        } else {
            val mb = bytes.toDouble() / (1024.0 * 1024.0)
            String.format("%.1f MB", mb)
        }
    }
}
