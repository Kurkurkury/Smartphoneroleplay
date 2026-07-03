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
import com.kurkurkury.smartphoneroleplay.data.DemoReplyEngine
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class MainActivity : Activity() {
    private lateinit var messagesView: LinearLayout
    private lateinit var input: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var subtitle: TextView
    private lateinit var storage: ChatStorage

    private val characters = CharacterRepository.defaultCharacters
    private var currentCharacterIndex = 0
    private val chatMessages = mutableListOf<ChatMessage>()
    private val currentCharacter: RoleplayCharacter
        get() = characters[currentCharacterIndex]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = ChatStorage(this)

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

        val clearButton = Button(this).apply {
            text = "Chat leeren"
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
            hint = "Schreibe deine Roleplay-Nachricht..."
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
