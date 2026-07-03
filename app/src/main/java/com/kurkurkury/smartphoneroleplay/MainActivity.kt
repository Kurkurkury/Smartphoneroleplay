package com.kurkurkury.smartphoneroleplay

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var messages: LinearLayout
    private lateinit var input: EditText
    private lateinit var scrollView: ScrollView

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

        val subtitle = TextView(this).apply {
            text = "Charakter: Reya"
            textSize = 16f
            gravity = Gravity.CENTER
        }
        root.addView(subtitle)

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
        addMessage("Reya", "Hi, ich bin Reya. Starte eine Szene und ich spiele mit dir weiter.")
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        addMessage("Du", text)
        addMessage("Reya", createDemoReply(text))
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

    private fun createDemoReply(userText: String): String {
        return when {
            userText.contains("wald", ignoreCase = true) -> "Reya schaut zwischen die Baeume und fluestert: Ich habe dort etwas gesehen."
            userText.contains("stadt", ignoreCase = true) -> "Reya zieht ihre Kapuze tiefer ins Gesicht und folgt dir durch die Stadt."
            userText.contains("kampf", ignoreCase = true) -> "Reya hebt die Hand. Noch nicht. Wir brauchen zuerst einen Plan."
            else -> "Reya nickt langsam und antwortet passend zur Szene: Erzaehl mir mehr."
        }
    }
}
