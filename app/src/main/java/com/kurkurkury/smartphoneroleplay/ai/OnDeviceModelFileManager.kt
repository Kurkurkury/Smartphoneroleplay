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

    fun modelSizeBytes(): Long = if (modelFile().exists()) modelFile().length() else 0L

    fun isLargeForPhone(): Boolean = modelSizeBytes() >= 1024L * 1024L * 1024L

    fun modelStatusMessage(): String {
        val file = modelFile()
        return if (modelExists()) {
            val safety = if (isLargeForPhone()) " — gross fuer Smartphone-Safe-Mode" else " — klein genug fuer vorsichtige Tests"
            "Lokales GGUF-Modell gefunden: ${formatBytes(file.length())}$safety"
        } else {
            "Kein lokales GGUF-Modell importiert. Tippe auf GGUF-Modell und waehle eine .gguf-Datei."
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
                val sizeWarning = if (bytesCopied >= 1024L * 1024L * 1024L) {
                    "\nHinweis: Dieses Modell ist gross fuer Smartphones. Die App bleibt deshalb im GGUF Safe Mode. Fuer echte lokale Inferenz zuerst ein kleineres Modell unter ca. 1 GB testen."
                } else {
                    "\nHinweis: Kleine GGUF-Modelle sind fuer den naechsten isolierten Testpfad geeignet."
                }
                ModelImportResult(true, "GGUF-Modell importiert: ${formatBytes(bytesCopied)}$sizeWarning", bytesCopied)
            }
        } catch (error: Exception) {
            ModelImportResult(false, "Import fehlgeschlagen: ${error.message ?: "unbekannter Fehler"}")
        }
    }

    fun clearModel(): ModelImportResult {
        val target = modelFile()
        return if (!target.exists()) {
            ModelImportResult(true, "Kein GGUF-Modell vorhanden.")
        } else if (target.delete()) {
            ModelImportResult(true, "GGUF-Modell geloescht. Die App nutzt wieder MediaPipe oder Demo-Modus.")
        } else {
            ModelImportResult(false, "GGUF-Modell konnte nicht geloescht werden. App neu starten und erneut versuchen.")
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
