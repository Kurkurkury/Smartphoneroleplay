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
import com.kurkurkury.smartphoneroleplay.data.DemoReplyEngine
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter

class MainActivity : Activity() {
    private lateinit var messages: LinearLayout
    private lateinit var input: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var subtitle: TextView

    private val characters = CharacterRepository.defaultCharacters
    private var currentCharacterIndex = 0
    private val currentCharacter: RoleplayCharacter
        get() = characters[currentCharacterIndex]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val switchButton = Button(this).apply {
            text = "Charakter wechseln"
            setOnClickListener { switchCharacter() }
        }
        root.addView(switchButton)

        scrollView = ScrollView(this)
        messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(messages)
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
        updateCharacterHeader()
        addMessage(currentCharacter.name, currentCharacter.greeting)
    }

    private fun switchCharacter() {
        currentCharacterIndex = (currentCharacterIndex + 1) % characters.size
        messages.removeAllViews()
        updateCharacterHeader()
        addMessage(currentCharacter.name, currentCharacter.greeting)
    }

    private fun updateCharacterHeader() {
        subtitle.text = "Charakter: ${currentCharacter.name} - ${currentCharacter.description}"
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        addMessage("Du", text)
        addMessage(currentCharacter.name, DemoReplyEngine.reply(currentCharacter, text))
    }

    private fun addMessage(sender: String, text: String) {
        val bubble = TextView(this).apply {
            this.text = "$sender: $text"
            textSize = 16f
            setPadding(16, 14, 16, 14)
        }
        messages.addView(bubble)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
