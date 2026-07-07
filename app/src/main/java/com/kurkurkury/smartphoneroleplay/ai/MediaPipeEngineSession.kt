package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class MediaPipeEngineSession(
    private val context: Context,
    private val modelPath: String
) : AutoCloseable {
    private var inference: LlmInference? = null
    private var loadError: String? = null

    fun isLoaded(): Boolean = inference != null

    fun status(): String {
        return when {
            inference != null -> "MediaPipe Session geladen."
            loadError != null -> "MediaPipe Session Ladefehler: $loadError"
            else -> "MediaPipe Session noch nicht geladen."
        }
    }

    fun generate(prompt: String): EngineGenerationResult {
        val runtime = ensureLoaded()
        if (runtime == null) {
            return EngineGenerationResult(
                ok = false,
                text = loadError ?: "RUNTIME-FEHLER: MediaPipe Session konnte nicht geladen werden.",
                engineId = "mediapipe-runtime"
            )
        }

        return try {
            val result = runtime.generateResponse(prompt).trim()
            EngineGenerationResult(
                ok = result.isNotBlank(),
                text = result.ifBlank { EngineFailureClassifier.emptyResponse() },
                engineId = "mediapipe-runtime"
            )
        } catch (error: Throwable) {
            EngineGenerationResult(
                ok = false,
                text = EngineFailureClassifier.classify(error),
                engineId = "mediapipe-runtime"
            )
        }
    }

    private fun ensureLoaded(): LlmInference? {
        inference?.let { return it }
        return try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .setMaxTopK(40)
                .build()
            LlmInference.createFromOptions(context, options).also { inference = it }
        } catch (error: Throwable) {
            loadError = EngineFailureClassifier.classify(error)
            null
        }
    }

    override fun close() {
        inference?.close()
        inference = null
    }
}
