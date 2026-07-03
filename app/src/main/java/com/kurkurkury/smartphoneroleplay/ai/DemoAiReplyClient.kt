package com.kurkurkury.smartphoneroleplay.ai

import com.kurkurkury.smartphoneroleplay.data.DemoReplyEngine
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class DemoAiReplyClient : AiReplyClient {
    override fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val baseReply = DemoReplyEngine.reply(character, userMessage)
        val contextSize = history.size
        return if (contextSize > 8) {
            "$baseReply\n\n${character.name} erinnert sich an den bisherigen Verlauf und bleibt in der Szene."
        } else {
            baseReply
        }
    }
}
