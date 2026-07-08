package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class OnDeviceReplyClient(context: Context) : AiReplyClient, AutoCloseable {
    private val appContext = context.applicationContext
    private val ggufModelFileManager = OnDeviceModelFileManager(appContext)
    private val engineModelFileManager = EngineModelFileManager(appContext)
    private val nativeBridge = NativeLlamaBridge()
    private val fallback = DemoAiReplyClient()
    private val engine = MediaPipePlannedEngine(appContext)

    override fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        if (ggufModelFileManager.modelExists()) {
            val result = nativeBridge.generate(
                modelPath = ggufModelFileManager.modelFile().absolutePath,
                prompt = buildPrompt(character, history, userMessage)
            )
            if (result.ok) return result.text
            return "[GGUF ENGINE ERROR]\n${result.text}"
        }

        if (engineModelFileManager.engineModelExists()) {
            val result = engine.generate(
                modelPath = engineModelFileManager.engineModelFile().absolutePath,
                character = character,
                history = history,
                userMessage = userMessage
            )
            if (result.ok) return result.text
            return "[MEDIAPIPE ENGINE ERROR]\n${result.text}"
        }

        val base = fallback.generateReply(character, history, userMessage)
        return "$base\n\n[DEMO MODE: Kein GGUF- oder .task/.litertlm Engine-Modell importiert.]"
    }

    override fun close() {
        engine.close()
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
