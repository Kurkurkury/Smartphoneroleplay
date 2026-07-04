package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
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
            "Lokales Modell gefunden: ${file.absolutePath}"
        } else {
            "Lokales Modell fehlt. Lege die GGUF-Datei hier ab und benenne sie so: ${file.absolutePath}"
        }
    }
}
