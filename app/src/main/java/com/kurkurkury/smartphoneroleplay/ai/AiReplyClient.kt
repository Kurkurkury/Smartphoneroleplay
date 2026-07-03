package com.kurkurkury.smartphoneroleplay.ai

import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

interface AiReplyClient {
    fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String
}
