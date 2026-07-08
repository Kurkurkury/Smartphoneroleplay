package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class OnDeviceReplyClient(context: Context) : AiReplyClient, AutoCloseable {
    private val appContext = context.applicationContext
    private val engineModelFileManager = EngineModelFileManager(appContext)
    private val fallback = DemoAiReplyClient()
    private val engine = MediaPipePlannedEngine(appContext)

    override fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        if (engineModelFileManager.engineModelExists()) {
            val result = engine.generate(
                modelPath = engineModelFileManager.engineModelFile().absolutePath,
                character = character,
                history = history,
                userMessage = userMessage
            )
            if (result.ok) return result.text
            return "[ENGINE ERROR]\n${result.text}"
        }

        val base = fallback.generateReply(character, history, userMessage)
        return "$base\n\n[DEMO MODE: Kein .task/.litertlm Engine-Modell importiert.]"
    }

    override fun close() {
        engine.close()
    }
}
