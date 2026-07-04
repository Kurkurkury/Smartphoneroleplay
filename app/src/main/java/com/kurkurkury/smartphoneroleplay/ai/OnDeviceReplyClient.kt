package com.kurkurkury.smartphoneroleplay.ai

import android.content.Context
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class OnDeviceReplyClient(context: Context) : AiReplyClient {
    private val fileManager = OnDeviceModelFileManager(context)
    private val fallback = DemoAiReplyClient()
    private val nativeBridge = NativeLlamaBridge()

    override fun generateReply(
        character: RoleplayCharacter,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val prompt = RoleplayPromptBuilder.buildSystemPrompt(character) +
            "\n\nBisheriger Verlauf:\n" +
            RoleplayPromptBuilder.buildContext(history) +
            "\n\nUser: " + userMessage +
            "\n" + character.name + ":"

        if (fileManager.modelExists()) {
            return nativeBridge.nativeGenerate(fileManager.modelFile().absolutePath, prompt)
        }

        val base = fallback.generateReply(character, history, userMessage)
        return "$base\n\n[${nativeBridge.nativeStatus()} ${fileManager.modelStatusMessage()}]"
    }
}
