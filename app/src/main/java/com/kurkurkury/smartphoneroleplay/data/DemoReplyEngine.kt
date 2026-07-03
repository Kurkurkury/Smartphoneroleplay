package com.kurkurkury.smartphoneroleplay.data

import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

object DemoReplyEngine {
    fun reply(character: RoleplayCharacter, userText: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("wald") -> "${character.name} schaut zwischen die Baeume und fluestert: Ich habe dort etwas gesehen."
            lower.contains("stadt") -> "${character.name} zieht die Kapuze tiefer ins Gesicht und folgt dir durch die Stadt."
            lower.contains("kampf") -> "${character.name} hebt die Hand. Noch nicht. Wir brauchen zuerst einen Plan."
            lower.contains("raumschiff") || lower.contains("planet") -> "${character.name} prueft die Anzeigen. Unsere Route ist riskant, aber machbar."
            lower.contains("angst") -> "${character.name} bleibt neben dir und sagt leise: Ich gehe nicht weg."
            else -> "${character.name} nickt langsam und spielt die Szene weiter: Erzaehl mir mehr."
        }
    }
}
