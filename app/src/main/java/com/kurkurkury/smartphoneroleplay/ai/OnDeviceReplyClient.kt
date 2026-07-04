package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class OnDeviceReplyClient(context: Context) : AiReplyClient {
    private val fileManager = OnDeviceModelFileManager(context)
    private val fallback = DemoAiReplyClient()

    override fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val base = fallback.generateReply(character, history, userMessage)
        val modelInfo = if (fileManager.modelExists()) {
            "Lokales Modell erkannt: ${fileManager.modelFile().length() / 1024 / 1024} MB. Native Diagnose ist vom normalen Senden getrennt, damit die App stabil bleibt."
        } else {
            "Kein lokales Modell importiert. Stabiler Demo-Modus aktiv."
        }
        return "$base\n\n[$modelInfo]"
    }
}
