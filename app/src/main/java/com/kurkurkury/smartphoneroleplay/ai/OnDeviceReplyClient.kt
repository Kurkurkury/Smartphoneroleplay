package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class OnDeviceReplyClient(context: Context) : AiReplyClient {
    private val appContext = context.applicationContext
    private val engineModelFileManager = EngineModelFileManager(appContext)
    private val fallback = DemoAiReplyClient()

    override fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        if (engineModelFileManager.engineModelExists()) {
            val engine = MediaPipePlannedEngine(appContext)
            val result = engine.generate(
                modelPath = engineModelFileManager.engineModelFile().absolutePath,
                character = character,
                history = history,
                userMessage = userMessage
            )
            if (result.ok) return result.text
            return "[LOKALE KI FEHLER]\n${result.text}\n\nDer Demo-Modus wurde hier bewusst nicht als echte Antwort verwendet, damit klar bleibt: Die lokale Engine hat noch nicht erfolgreich geantwortet."
        }

        val base = fallback.generateReply(character, history, userMessage)
        return "$base\n\n[DEMO-MODUS: Kein .task/.litertlm Engine-Modell importiert.]"
    }
}
