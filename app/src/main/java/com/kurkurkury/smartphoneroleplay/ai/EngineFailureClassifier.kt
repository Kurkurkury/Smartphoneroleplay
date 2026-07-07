package com.kurkurkury.smartphoneroleplay.ai

object EngineFailureClassifier {
    fun classify(error: Throwable): String {
        val rawMessage = error.message.orEmpty()
        val message = rawMessage.lowercase()
        val typeName = error.javaClass.simpleName
        return when {
            error is OutOfMemoryError || message.contains("outofmemory") || message.contains("memory") ->
                "SPEICHERFEHLER: Das Modell ist fuer den verfuegbaren RAM vermutlich zu gross oder die Runtime konnte nicht genug Speicher reservieren."

            message.contains("model") && (message.contains("invalid") || message.contains("unsupported") || message.contains("format")) ->
                "MODELLFORMAT-FEHLER: Das importierte Engine-Modell scheint nicht kompatibel zu sein. Erwartet wird ein passendes .task/.litertlm Paket."

            message.contains("file") || message.contains("path") || message.contains("open") || message.contains("not found") ->
                "DATEIFEHLER: Das Engine-Modell konnte nicht korrekt gelesen werden. Import erneut pruefen."

            message.contains("delegate") || message.contains("gpu") || message.contains("accelerator") ->
                "RUNTIME-BESCHLEUNIGER-FEHLER: Die Runtime konnte GPU/Delegate nicht wie erwartet verwenden. CPU-Fallback oder andere Runtime-Konfiguration noetig."

            message.contains("token") || message.contains("decode") || message.contains("generate") ->
                "GENERIERUNGSFEHLER: Modell wurde moeglicherweise geladen, aber Antworterzeugung ist fehlgeschlagen."

            rawMessage.isBlank() ->
                "UNBEKANNTER RUNTIME-FEHLER: ${typeName} ohne Detailmeldung."

            else ->
                "RUNTIME-FEHLER: ${typeName}: $rawMessage"
        }
    }

    fun emptyResponse(): String =
        "LEERE ANTWORT: Die Runtime lief ohne sichtbare Ausgabe. Prompt, Tokenlimit oder Modellkompatibilitaet pruefen."
}
