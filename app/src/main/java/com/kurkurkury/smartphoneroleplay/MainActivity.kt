package com.kurkurkury.smartphoneroleplay

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kurkurkury.smartphoneroleplay.data.CharacterRepository
import com.kurkurkury.smartphoneroleplay.data.ChatStorage
import com.kurkurkury.smartphoneroleplay.data.CustomCharacterStorage
import com.kurkurkury.smartphoneroleplay.data.DemoReplyEngine
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class MainActivity : Activity() {
    private lateinit var messagesView: LinearLayout
    private lateinit var input: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var subtitle: TextView
    private lateinit var storage: ChatStorage
    private lateinit var characterStorage: CustomCharacterStorage

    private val characters = mutableListOf<RoleplayCharacter>()
    private var currentCharacterIndex = 0
    private val chatMessages = mutableListOf<ChatMessage>()
    private val currentCharacter: RoleplayCharacter
        get() = characters[currentCharacterIndex]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = ChatStorage(this)
        characterStorage = CustomCharacterStorage(this)
        characters.addAll(CharacterRepository.defaultCharacters)
        characters.addAll(characterStorage.load())

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 24)
        }

        val title = TextView(this).apply {
            text = "Smartphone Roleplay"
            textSize = 24f
            gravity = Gravity.CENTER
        }
        root.addView(title)

        subtitle = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
        }
        root.addView(subtitle)

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val switchButton = Button(this).apply {
            text = "Charakter"
            setOnClickListener { switchCharacter() }
        }
        buttonRow.addView(switchButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val createButton = Button(this).apply {
            text = "Neu"
            setOnClickListener { createCharacterFromInput() }
        }
        buttonRow.addView(createButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val clearButton = Button(this).apply {
            text = "Leeren"
            setOnClickListener { clearChat() }
        }
        buttonRow.addView(clearButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(buttonRow)

        scrollView = ScrollView(this)
        messagesView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(messagesView)
        root.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        input = EditText(this).apply {
            hint = "Nachricht oder neuer Charakter: Name; Beschreibung; Begruessung"
            singleLine = false
            minLines = 1
            maxLines = 4
        }
        inputRow.addView(input, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val sendButton = Button(this).apply {
            text = "Senden"
            setOnClickListener { sendMessage() }
        }
        inputRow.addView(sendButton)
        root.addView(inputRow)

        setContentView(root)
        loadCurrentChat()
    }

    private fun switchCharacter() {
        saveCurrentChat()
        currentCharacterIndex = (currentCharacterIndex + 1) % characters.size
        loadCurrentChat()
    }

    private fun createCharacterFromInput() {
        val raw = input.text.toString().trim()
        if (raw.isEmpty()) {
            addSystemMessage("Format: Name; Beschreibung; Begruessung")
            return
        }
        val parts = raw.split(";").map { it.trim() }
        val name = parts.getOrNull(0).orEmpty().ifBlank { "Neuer Charakter" }
        val description = parts.getOrNull(1).orEmpty().ifBlank { "Eigener Roleplay-Charakter" }
        val greeting = parts.getOrNull(2).orEmpty().ifBlank { "Hi, ich bin $name. Starte eine Szene." }
        val customCharacter = RoleplayCharacter(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            description = description,
            greeting = greeting,
            personality = "benutzerdefiniert"
        )
        characters.add(customCharacter)
        characterStorage.save(characters.drop(CharacterRepository.defaultCharacters.size))
        input.setText("")
        saveCurrentChat()
        currentCharacterIndex = characters.lastIndex
        loadCurrentChat()
    }

    private fun loadCurrentChat() {
        chatMessages.clear()
        chatMessages.addAll(storage.load(currentCharacter.id))
        if (chatMessages.isEmpty()) {
            chatMessages.add(ChatMessage(currentCharacter.name, currentCharacter.greeting))
        }
        renderChat()
        updateCharacterHeader()
    }

    private fun saveCurrentChat() {
        storage.save(currentCharacter.id, chatMessages)
    }

    private fun clearChat() {
        storage.clear(currentCharacter.id)
        chatMessages.clear()
        chatMessages.add(ChatMessage(currentCharacter.name, currentCharacter.greeting))
        renderChat()
        saveCurrentChat()
    }

    private fun updateCharacterHeader() {
        subtitle.text = "Charakter: ${currentCharacter.name} - ${currentCharacter.description}"
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        chatMessages.add(ChatMessage("Du", text))
        chatMessages.add(ChatMessage(currentCharacter.name, DemoReplyEngine.reply(currentCharacter, text)))
        renderChat()
        saveCurrentChat()
    }

    private fun addSystemMessage(text: String) {
        chatMessages.add(ChatMessage("System", text))
        renderChat()
    }

    private fun renderChat() {
        messagesView.removeAllViews()
        chatMessages.forEach { message ->
            val bubble = TextView(this).apply {
                this.text = "${message.sender}: ${message.text}"
                textSize = 16f
                setPadding(16, 14, 16, 14)
            }
            messagesView.addView(bubble)
        }
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
