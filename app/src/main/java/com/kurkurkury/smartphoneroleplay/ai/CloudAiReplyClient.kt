package com.kurkurkury.smartphoneroleplay.ai

import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class CloudAiReplyClient : AiReplyClient {
    override fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val systemPrompt = RoleplayPromptBuilder.buildSystemPrompt(character)
        val context = RoleplayPromptBuilder.buildContext(history)
        return "KI-Anbindung vorbereitet, aber noch nicht verbunden.\n\nSystem:\n$systemPrompt\n\nKontext:\n$context\n\nLetzte Nachricht: $userMessage"
    }
}
