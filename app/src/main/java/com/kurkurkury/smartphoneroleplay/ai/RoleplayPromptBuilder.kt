package com.kurkurkury.smartphoneroleplay.ai

import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

object RoleplayPromptBuilder {
    fun buildSystemPrompt(character: RoleplayCharacter): String {
        return """
            Du bist ${character.name}.
            Beschreibung: ${character.description}
            Persoenlichkeit: ${character.personality}
            Antworte im Roleplay-Stil als diese Figur.
            Bleibe in der Szene, schreibe lebendig, aber nicht zu lang.
        """.trimIndent()
    }

    fun buildContext(history: List<ChatMessage>, limit: Int = 12): String {
        return history.takeLast(limit).joinToString(separator = "\n") { message ->
            "${message.sender}: ${message.text}"
        }
    }
}
