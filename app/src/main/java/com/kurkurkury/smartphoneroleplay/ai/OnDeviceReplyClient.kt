package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class OnDeviceReplyClient(context: Context) : AiReplyClient {
    private val engineController = AiEngineController(context)
    private val fallback = DemoAiReplyClient()

    override fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val base = fallback.generateReply(character, history, userMessage)
        return "$base\n\n[${engineController.currentMode().label}: stabiler Demo-Fallback aktiv. Native llama.cpp-Inferenz ist deaktiviert, bis eine robuste Android-Engine angebunden ist.]"
    }
}
