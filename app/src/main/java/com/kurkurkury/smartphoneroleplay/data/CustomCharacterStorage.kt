package com.kurkurkury.smartphoneroleplay.data

import android.content.Context
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class CustomCharacterStorage(context: Context) {
    private val prefs = context.getSharedPreferences("smartphone_roleplay_characters", Context.MODE_PRIVATE)

    fun save(characters: List<RoleplayCharacter>) {
        val raw = characters.joinToString(separator = "\n") { character ->
            listOf(
                character.id.escape(),
                character.name.escape(),
                character.description.escape(),
                character.greeting.escape(),
                character.personality.escape()
            ).joinToString("|")
        }
        prefs.edit().putString("custom_characters", raw).apply()
    }

    fun load(): List<RoleplayCharacter> {
        val raw = prefs.getString("custom_characters", null) ?: return emptyList()
        return raw.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size < 5) return@mapNotNull null
                RoleplayCharacter(
                    id = parts[0].unescape(),
                    name = parts[1].unescape(),
                    description = parts[2].unescape(),
                    greeting = parts[3].unescape(),
                    personality = parts.drop(4).joinToString("|").unescape()
                )
            }
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
