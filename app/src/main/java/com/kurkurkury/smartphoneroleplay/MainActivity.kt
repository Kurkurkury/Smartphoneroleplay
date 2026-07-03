package com.kurkurkury.smartphoneroleplay

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
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

    private val backgroundColor = Color.rgb(10, 15, 24)
    private val panelColor = Color.rgb(18, 25, 38)
    private val primaryColor = Color.rgb(126, 87, 194)
    private val userBubbleColor = Color.rgb(94, 53, 177)
    private val aiBubbleColor = Color.rgb(31, 41, 55)
    private val textColor = Color.rgb(241, 245, 249)
    private val mutedTextColor = Color.rgb(148, 163, 184)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = ChatStorage(this)
        characterStorage = CustomCharacterStorage(this)
        characters.addAll(CharacterRepository.defaultCharacters)
        characters.addAll(characterStorage.load())
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 20)
            setBackgroundColor(backgroundColor)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
            background = rounded(panelColor, 28f)
        }

        val title = TextView(this).apply {
            text = "Smartphone Roleplay"
            textSize = 26f
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        header.addView(title)

        subtitle = TextView(this).apply {
            textSize = 14f
            setTextColor(mutedTextColor)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        header.addView(subtitle)
        root.addView(header)

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 18, 0, 18)
        }

        buttonRow.addView(actionButton("Figur") { switchCharacter() }, LinearLayout.LayoutParams(0, 62, 1f).apply { setMargins(0, 0, 10, 0) })
        buttonRow.addView(actionButton("Neu") { createCharacterFromInput() }, LinearLayout.LayoutParams(0, 62, 1f).apply { setMargins(10, 0, 10, 0) })
        buttonRow.addView(actionButton("Leeren") { clearChat() }, LinearLayout.LayoutParams(0, 62, 1f).apply { setMargins(10, 0, 0, 0) })
        root.addView(buttonRow)

        scrollView = ScrollView(this).apply {
            setPadding(0, 0, 0, 0)
        }
        messagesView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 8)
        }
        scrollView.addView(messagesView)
        root.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        val inputCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 12, 16, 12)
            background = rounded(panelColor, 28f)
        }

        input = EditText(this).apply {
            hint = "Nachricht schreiben..."
            setHintTextColor(mutedTextColor)
            setTextColor(textColor)
            textSize = 16f
            setSingleLine(false)
            minLines = 1
            maxLines = 4
            background = null
        }
        inputCard.addView(input, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val sendButton = TextView(this).apply {
            text = "Senden"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(20, 0, 20, 0)
            background = rounded(primaryColor, 24f)
            setOnClickListener { sendMessage() }
        }
        inputCard.addView(sendButton, LinearLayout.LayoutParams(190, 62).apply { setMargins(14, 0, 0, 0) })
        root.addView(inputCard)

        setContentView(root)
        loadCurrentChat()
    }

    private fun actionButton(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(8, 0, 8, 0)
            background = rounded(Color.rgb(37, 50, 72), 22f)
            setOnClickListener { onClick() }
        }
    }

    private fun switchCharacter() {
        saveCurrentChat()
        currentCharacterIndex = (currentCharacterIndex + 1) % characters.size
        loadCurrentChat()
    }

    private fun createCharacterFromInput() {
        val raw = input.text.toString().trim()
        if (raw.isEmpty()) {
            addSystemMessage("Neuer Charakter: Name; Beschreibung; Begruessung")
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
        subtitle.text = "${currentCharacter.name} • ${currentCharacter.description}"
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
            val isUser = message.sender == "Du"
            val isSystem = message.sender == "System"
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = if (isUser) Gravity.END else Gravity.START
                setPadding(0, 8, 0, 8)
            }

            val bubble = TextView(this).apply {
                text = if (isSystem) message.text else "${message.sender}\n${message.text}"
                textSize = if (isSystem) 13f else 16f
                setTextColor(if (isSystem) mutedTextColor else textColor)
                setPadding(22, 16, 22, 16)
                background = rounded(
                    when {
                        isSystem -> Color.rgb(15, 23, 42)
                        isUser -> userBubbleColor
                        else -> aiBubbleColor
                    },
                    26f
                )
            }
            row.addView(bubble, LinearLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.78f).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
            messagesView.addView(row)
        }
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }
}
