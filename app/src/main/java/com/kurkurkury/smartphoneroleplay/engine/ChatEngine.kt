package com.kurkurkury.smartphoneroleplay.engine

import com.kurkurkury.smartphoneroleplay.ai.AiReplyClient
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class ChatEngine(
    private val aiReplyClient: AiReplyClient
) {
    fun createReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): ChatMessage {
        val replyText = aiReplyClient.generateReply(character, history, userMessage)
        return ChatMessage(character.name, replyText)
    }
}
