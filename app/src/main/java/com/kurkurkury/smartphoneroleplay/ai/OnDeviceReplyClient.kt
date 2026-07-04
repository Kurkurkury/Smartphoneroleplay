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
        val status = fileManager.modelStatusMessage()
        return "$base\n\n[Lokaler Modus vorbereitet: $status]"
    }
}
