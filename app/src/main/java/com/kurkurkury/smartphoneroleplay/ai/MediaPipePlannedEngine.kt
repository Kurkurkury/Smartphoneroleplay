package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class MediaPipePlannedEngine(
    private val context: Context? = null
) : AndroidLocalLlmEngine {
    override val id: String = "mediapipe-runtime"
    override val displayName: String = "MediaPipe LLM Runtime"

    override fun isAvailable(): Boolean = context != null

    override fun status(): String {
        return if (context == null) {
            "MediaPipe LLM Runtime ist eingebunden, aber fuer diesen Statuscheck noch nicht initialisiert."
        } else {
            "MediaPipe LLM Runtime eingebunden. Benoetigt kompatibles .task/.litertlm Engine-Modellpaket."
        }
    }

    override fun generate(
        modelPath: String,
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): EngineGenerationResult {
        val appContext = context ?: return EngineGenerationResult(
            ok = false,
            text = "MediaPipe Runtime nicht initialisiert.",
            engineId = id
        )

        val prompt = buildPrompt(character, history, userMessage)
        return try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .setMaxTopK(40)
                .build()
            LlmInference.createFromOptions(appContext, options).use { inference ->
                val result = inference.generateResponse(prompt).trim()
                EngineGenerationResult(
                    ok = result.isNotBlank(),
                    text = result.ifBlank { "MediaPipe Runtime hat keine Antwort erzeugt." },
                    engineId = id
                )
            }
        } catch (error: Throwable) {
            EngineGenerationResult(
                ok = false,
                text = "MediaPipe Runtime fehlgeschlagen: ${error.message ?: error.javaClass.simpleName}",
                engineId = id
            )
        }
    }

    private fun buildPrompt(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val recentHistory = history.takeLast(8).joinToString("\n") { message ->
            "${message.sender}: ${message.text}"
        }
        return """
            Du bist ${character.name}.
            Rolle: ${character.description}
            Persoenlichkeit: ${character.personality}

            Antworte kurz, natuerlich und bleibe strikt in der Rolle.

            Bisheriger Chat:
            $recentHistory

            Nutzer: $userMessage
            ${character.name}:
        """.trimIndent()
    }
}
