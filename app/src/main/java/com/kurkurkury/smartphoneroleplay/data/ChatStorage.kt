package com.kurkurkury.smartphoneroleplay.data

import android.content.Context
import com.kurkurkury.smartphoneroleplay.model.ChatMessage

class ChatStorage(context: Context) {
    private val prefs = context.getSharedPreferences("smartphone_roleplay_chat", Context.MODE_PRIVATE)

    fun save(characterId: String, messages: List<ChatMessage>) {
        val text = messages.joinToString(separator = "\n") { message ->
            listOf(
                message.timestampMillis.toString(),
                message.sender.escape(),
                message.text.escape()
            ).joinToString("|")
        }
        prefs.edit().putString("chat_$characterId", text).apply()
    }

    fun load(characterId: String): List<ChatMessage> {
        val raw = prefs.getString("chat_$characterId", null) ?: return emptyList()
        return raw.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size < 3) return@mapNotNull null
                ChatMessage(
                    timestampMillis = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                    sender = parts[1].unescape(),
                    text = parts.drop(2).joinToString("|").unescape()
                )
            }
    }

    fun clear(characterId: String) {
        prefs.edit().remove("chat_$characterId").apply()
    }

    private fun String.escape(): String = this
        .replace("%", "%25")
        .replace("|", "%7C")
        .replace("\n", "%0A")

    private fun String.unescape(): String = this
        .replace("%0A", "\n")
        .replace("%7C", "|")
        .replace("%25", "%")
}
