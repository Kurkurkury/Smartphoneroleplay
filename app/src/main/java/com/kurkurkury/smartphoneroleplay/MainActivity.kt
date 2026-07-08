package com.kurkurkury.smartphoneroleplay

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kurkurkury.smartphoneroleplay.ai.AiEngineController
import com.kurkurkury.smartphoneroleplay.ai.EngineModelFileManager
import com.kurkurkury.smartphoneroleplay.ai.MediaPipePlannedEngine
import com.kurkurkury.smartphoneroleplay.ai.NativeLlamaBridge
import com.kurkurkury.smartphoneroleplay.ai.OnDeviceModelFileManager
import com.kurkurkury.smartphoneroleplay.ai.OnDeviceReplyClient
import com.kurkurkury.smartphoneroleplay.data.CharacterRepository
import com.kurkurkury.smartphoneroleplay.data.ChatStorage
import com.kurkurkury.smartphoneroleplay.data.CustomCharacterStorage
import com.kurkurkury.smartphoneroleplay.engine.ChatEngine
import com.kurkurkury.smartphoneroleplay.model.ChatMessage
import com.kurkurkury.smartphoneroleplay.model.RoleplayCharacter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var messagesView: LinearLayout
    private lateinit var input: EditText
    private lateinit var sendButton: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var subtitle: TextView
    private lateinit var modelStatus: TextView
    private lateinit var characterChip: TextView
    private lateinit var storage: ChatStorage
    private lateinit var characterStorage: CustomCharacterStorage
    private lateinit var chatEngine: ChatEngine
    private lateinit var replyClient: OnDeviceReplyClient
    private lateinit var modelFileManager: OnDeviceModelFileManager
    private lateinit var engineModelFileManager: EngineModelFileManager
    private lateinit var engineController: AiEngineController

    private val modelPickerRequestCode = 9124
    private val engineModelPickerRequestCode = 9125
    private val characters = mutableListOf<RoleplayCharacter>()
    private var currentCharacterIndex = 0
    private var replyInProgress = false
    private var destroyed = false
    private val chatMessages = mutableListOf<ChatMessage>()
    private val currentCharacter: RoleplayCharacter
        get() = characters[currentCharacterIndex]

    private val backgroundColor = Color.rgb(6, 10, 18)
    private val panelColor = Color.rgb(15, 23, 36)
    private val elevatedPanelColor = Color.rgb(20, 30, 46)
    private val buttonColor = Color.rgb(38, 52, 77)
    private val primaryColor = Color.rgb(139, 92, 246)
    private val accentColor = Color.rgb(34, 211, 238)
    private val userBubbleColor = Color.rgb(109, 40, 217)
    private val aiBubbleColor = Color.rgb(30, 41, 59)
    private val systemBubbleColor = Color.rgb(17, 24, 39)
    private val textColor = Color.rgb(248, 250, 252)
    private val mutedTextColor = Color.rgb(148, 163, 184)
    private val subtleBorderColor = Color.rgb(51, 65, 85)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = ChatStorage(this)
        characterStorage = CustomCharacterStorage(this)
        modelFileManager = OnDeviceModelFileManager(this)
        engineModelFileManager = EngineModelFileManager(this)
        engineController = AiEngineController(this)
        replyClient = OnDeviceReplyClient(this)
        chatEngine = ChatEngine(replyClient)
        characters.addAll(CharacterRepository.defaultCharacters)
        characters.addAll(characterStorage.load())
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(12))
            setBackgroundColor(backgroundColor)
        }

        root.addView(createHeader())
        root.addView(createActionGrid())
        root.addView(createChatSurface(), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ).apply { setMargins(0, dp(12), 0, dp(12)) })
        root.addView(createInputBar())

        setContentView(root)
        loadCurrentChat()
    }

    override fun onDestroy() {
        destroyed = true
        replyClient.close()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == modelPickerRequestCode && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            addSystemMessage("GGUF-Modell wird importiert. Das kann bei grossen Dateien laenger dauern...")
            val result = modelFileManager.importModel(uri)
            addSystemMessage(result.message)
            updateCharacterHeader()
        }
        if (requestCode == engineModelPickerRequestCode && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            addSystemMessage("Engine-Modellpaket wird importiert. Das kann bei grossen Dateien laenger dauern...")
            val result = engineModelFileManager.importEngineModel(uri)
            addSystemMessage(result.message)
            updateCharacterHeader()
        }
    }

    private fun createHeader(): LinearLayout {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = rounded(elevatedPanelColor, 30f, subtleBorderColor, 1)
        }
        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val appMark = TextView(this).apply {
            text = "SR"; textSize = 15f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            setTextColor(Color.WHITE); background = rounded(primaryColor, 18f)
        }
        topRow.addView(appMark, LinearLayout.LayoutParams(dp(44), dp(44)).apply { setMargins(0, 0, dp(12), 0) })
        val titleBlock = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = TextView(this).apply {
            text = "Smartphone Roleplay"; textSize = 25f; setTextColor(textColor); typeface = Typeface.DEFAULT_BOLD; includeFontPadding = false
        }
        titleBlock.addView(title)
        subtitle = TextView(this).apply { textSize = 13f; setTextColor(mutedTextColor); setPadding(0, dp(5), 0, 0); maxLines = 2 }
        titleBlock.addView(subtitle)
        topRow.addView(titleBlock, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(topRow)
        val statusRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(14), 0, 0) }
        characterChip = TextView(this).apply {
            textSize = 12f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setTextColor(textColor)
            setPadding(dp(12), 0, dp(12), 0); background = rounded(Color.rgb(37, 50, 72), 18f)
        }
        statusRow.addView(characterChip, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(34)).apply { setMargins(0, 0, dp(8), 0) })
        modelStatus = TextView(this).apply {
            textSize = 12f; gravity = Gravity.CENTER_VERTICAL; setTextColor(mutedTextColor); setPadding(dp(12), 0, dp(12), 0)
            background = rounded(Color.rgb(10, 18, 31), 18f, subtleBorderColor, 1); maxLines = 1
        }
        statusRow.addView(modelStatus, LinearLayout.LayoutParams(0, dp(34), 1f))
        header.addView(statusRow)
        return header
    }

    private fun createActionGrid(): LinearLayout {
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(12), 0, 0) }
        val buttonRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        buttonRow.addView(actionButton("Figur", "wechseln") { switchCharacter() }, LinearLayout.LayoutParams(0, dp(58), 1f).apply { setMargins(0, 0, dp(8), 0) })
        buttonRow.addView(actionButton("Neu", "erstellen") { createCharacterFromInput() }, LinearLayout.LayoutParams(0, dp(58), 1f).apply { setMargins(0, 0, dp(8), 0) })
        buttonRow.addView(actionButton("Leeren", "reset") { clearChat() }, LinearLayout.LayoutParams(0, dp(58), 1f))
        container.addView(buttonRow)
        container.addView(actionButton("GGUF-Modell", "llama.cpp Diagnose") { openModelPicker() }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)).apply { setMargins(0, dp(8), 0, 0) })
        container.addView(actionButton("Engine-Modell", "MediaPipe Import") { openEngineModelPicker() }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)).apply { setMargins(0, dp(8), 0, 0) })
        container.addView(actionButton("KI-Test", "Engine Diagnose") { runNativeDiagnostic() }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)).apply { setMargins(0, dp(8), 0, 0) })
        return container
    }

    private fun createChatSurface(): ScrollView {
        scrollView = ScrollView(this).apply { isFillViewport = true; background = rounded(Color.rgb(8, 13, 23), 28f, Color.rgb(18, 27, 43), 1) }
        messagesView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(14), dp(12), dp(14)) }
        scrollView.addView(messagesView)
        return scrollView
    }

    private fun createInputBar(): LinearLayout {
        val inputCard = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(10), dp(10), dp(10)); background = rounded(panelColor, 28f, subtleBorderColor, 1) }
        input = EditText(this).apply {
            hint = "Nachricht schreiben..."; setHintTextColor(mutedTextColor); setTextColor(textColor); textSize = 16f
            setSingleLine(false); minLines = 1; maxLines = 4; background = null; includeFontPadding = false
        }
        inputCard.addView(input, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        sendButton = TextView(this).apply {
            text = "Senden"; textSize = 15f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setTextColor(Color.WHITE)
            setPadding(dp(18), 0, dp(18), 0); background = rounded(primaryColor, 24f); setOnClickListener { sendMessage() }
        }
        inputCard.addView(sendButton, LinearLayout.LayoutParams(dp(104), dp(52)).apply { setMargins(dp(10), 0, 0, 0) })
        return inputCard
    }

    private fun openModelPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "*/*" }
        startActivityForResult(intent, modelPickerRequestCode)
    }

    private fun openEngineModelPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "*/*" }
        startActivityForResult(intent, engineModelPickerRequestCode)
    }

    private fun runNativeDiagnostic() {
        val bridge = NativeLlamaBridge()
        addSystemMessage(engineController.diagnosticText())
        if (engineModelFileManager.engineModelExists()) {
            addSystemMessage("MediaPipe Selbsttest wird gestartet...")
            val characterSnapshot = currentCharacter
            Thread {
                val result = MediaPipePlannedEngine(applicationContext).generate(
                    modelPath = engineModelFileManager.engineModelFile().absolutePath,
                    character = characterSnapshot,
                    history = emptyList(),
                    userMessage = "Antworte nur mit OK."
                )
                runOnUiThread {
                    if (!destroyed) addSystemMessage("MediaPipe Selbsttest\nStatus: ${if (result.ok) "OK" else "FEHLER"}\nAntwort: ${result.text}")
                }
            }.start()
        }
        if (modelFileManager.modelExists()) {
            addSystemMessage(bridge.diagnostic(modelFileManager.modelFile().absolutePath).text)
        } else {
            addSystemMessage("Native Import-Status\nLibrary/Status: ${bridge.status()}\nGGUF-Modellpfad vorhanden: NEIN\nOptional: GGUF nur fuer alten llama.cpp Statuscheck importieren.")
        }
    }

    private fun actionButton(label: String, caption: String, onClick: () -> Unit): LinearLayout {
        val button = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(8), dp(6), dp(8), dp(6))
            background = rounded(buttonColor, 18f, Color.rgb(59, 76, 108), 1); isClickable = true; isFocusable = true; setOnClickListener { onClick() }
        }
        button.addView(TextView(this).apply { text = label; textSize = 15f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setTextColor(Color.WHITE); includeFontPadding = false })
        button.addView(TextView(this).apply { text = caption; textSize = 10f; gravity = Gravity.CENTER; setTextColor(mutedTextColor); includeFontPadding = false; setPadding(0, dp(3), 0, 0) })
        return button
    }

    private fun switchCharacter() {
        if (replyInProgress) { addSystemMessage("Bitte warten: Die KI-Antwort laeuft noch."); return }
        saveCurrentChat(); currentCharacterIndex = (currentCharacterIndex + 1) % characters.size; loadCurrentChat()
    }

    private fun createCharacterFromInput() {
        if (replyInProgress) { addSystemMessage("Bitte warten: Die KI-Antwort laeuft noch."); return }
        val raw = input.text.toString().trim()
        if (raw.isEmpty()) { addSystemMessage("Neuer Charakter: Name; Beschreibung; Begruessung"); return }
        val parts = raw.split(";").map { it.trim() }
        val customCharacter = RoleplayCharacter(
            id = "custom_${System.currentTimeMillis()}",
            name = parts.getOrNull(0).orEmpty().ifBlank { "Neuer Charakter" },
            description = parts.getOrNull(1).orEmpty().ifBlank { "Eigener Roleplay-Charakter" },
            greeting = parts.getOrNull(2).orEmpty().ifBlank { "Hi, ich bin ${parts.getOrNull(0).orEmpty().ifBlank { "Neuer Charakter" }}. Starte eine Szene." },
            personality = "benutzerdefiniert"
        )
        characters.add(customCharacter); characterStorage.save(characters.drop(CharacterRepository.defaultCharacters.size)); input.setText(""); saveCurrentChat(); currentCharacterIndex = characters.lastIndex; loadCurrentChat()
    }

    private fun loadCurrentChat() {
        chatMessages.clear(); chatMessages.addAll(storage.load(currentCharacter.id))
        if (chatMessages.isEmpty()) chatMessages.add(ChatMessage(currentCharacter.name, currentCharacter.greeting))
        renderChat(); updateCharacterHeader()
    }

    private fun saveCurrentChat() { storage.save(currentCharacter.id, chatMessages) }

    private fun clearChat() {
        if (replyInProgress) { addSystemMessage("Bitte warten: Die KI-Antwort laeuft noch."); return }
        storage.clear(currentCharacter.id); chatMessages.clear(); chatMessages.add(ChatMessage(currentCharacter.name, currentCharacter.greeting)); renderChat(); saveCurrentChat()
    }

    private fun updateCharacterHeader() {
        subtitle.text = currentCharacter.description
        characterChip.text = "${currentCharacter.name} • ${currentCharacter.personality.take(24)}"
        modelStatus.text = when {
            replyInProgress -> "KI antwortet..."
            engineModelFileManager.engineModelExists() -> "Engine-Modell • ${engineModelFileManager.engineModelFile().length() / 1024 / 1024} MB"
            modelFileManager.modelExists() -> "GGUF Diagnose • ${modelFileManager.modelStatusMessage().removePrefix("Lokales Modell gefunden: ")}"
            else -> "Demo-Modus • kein Engine-Modell importiert"
        }
    }

    private fun setReplyInProgress(active: Boolean) {
        replyInProgress = active
        input.isEnabled = !active
        sendButton.isEnabled = !active
        sendButton.alpha = if (active) 0.55f else 1.0f
        sendButton.text = if (active) "..." else "Senden"
        updateCharacterHeader()
    }

    private fun sendMessage() {
        val text = input.text.toString().trim()
        if (text.isEmpty() || replyInProgress) return
        val characterSnapshot = currentCharacter
        input.setText("")
        chatMessages.add(ChatMessage("Du", text))
        val historySnapshot = chatMessages.toList()
        renderChat(); scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        saveCurrentChat()
        setReplyInProgress(true)
        Thread {
            val reply = chatEngine.createReply(characterSnapshot, historySnapshot, text)
            runOnUiThread {
                if (destroyed) return@runOnUiThread
                chatMessages.add(reply)
                renderChat()
                saveCurrentChat()
                setReplyInProgress(false)
            }
        }.start()
    }

    private fun addSystemMessage(text: String) { chatMessages.add(ChatMessage("System", text)); renderChat(); saveCurrentChat() }

    private fun renderChat() {
        messagesView.removeAllViews()
        if (chatMessages.size <= 1) messagesView.addView(sceneHintCard())
        chatMessages.forEach { messagesView.addView(messageRow(it)) }
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun sceneHintCard(): LinearLayout {
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(14), dp(16), dp(14)); background = rounded(Color.rgb(11, 18, 32), 24f, Color.rgb(35, 48, 75), 1) }
        card.addView(TextView(this).apply { text = "Szenenstart"; textSize = 14f; typeface = Typeface.DEFAULT_BOLD; setTextColor(accentColor) })
        card.addView(TextView(this).apply { text = "Schreibe einen Ort, eine Stimmung oder eine erste Handlung. ${currentCharacter.name} bleibt in der Rolle und spielt weiter."; textSize = 13f; setTextColor(mutedTextColor); setPadding(0, dp(6), 0, 0) })
        card.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, dp(10)) }
        return card
    }

    private fun messageRow(message: ChatMessage): LinearLayout {
        val isUser = message.sender == "Du"; val isSystem = message.sender == "System"
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = if (isUser) Gravity.END else Gravity.START; setPadding(0, dp(7), 0, dp(7)) }
        if (!isUser && !isSystem) row.addView(avatarFor(message.sender), LinearLayout.LayoutParams(dp(38), dp(38)).apply { setMargins(0, dp(2), dp(8), 0) })
        val stack = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = if (isUser) Gravity.END else Gravity.START }
        stack.addView(TextView(this).apply { text = if (isSystem) "System • ${formatTime(message.timestampMillis)}" else "${message.sender} • ${formatTime(message.timestampMillis)}"; textSize = 11f; setTextColor(mutedTextColor); gravity = if (isUser) Gravity.END else Gravity.START; setPadding(dp(4), 0, dp(4), dp(4)) })
        val bubble = TextView(this).apply {
            text = message.text; textSize = if (isSystem) 13f else 16f; setTextColor(if (isSystem) mutedTextColor else textColor); setPadding(dp(16), dp(12), dp(16), dp(12))
            background = rounded(if (isSystem) systemBubbleColor else if (isUser) userBubbleColor else aiBubbleColor, 24f, if (isUser) primaryColor else if (isSystem) subtleBorderColor else Color.rgb(51, 65, 85), 1)
        }
        stack.addView(bubble, LinearLayout.LayoutParams((resources.displayMetrics.widthPixels * if (isSystem) 0.84f else 0.72f).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT))
        row.addView(stack); return row
    }

    private fun avatarFor(name: String): TextView = TextView(this).apply { text = name.take(1).uppercase(Locale.getDefault()); textSize = 14f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setTextColor(Color.WHITE); background = rounded(primaryColor, 19f) }
    private fun formatTime(timestampMillis: Long): String = timeFormat.format(Date(timestampMillis))
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Float, strokeColor: Int? = null, strokeDp: Int = 0): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius.toInt()).toFloat(); if (strokeColor != null && strokeDp > 0) setStroke(dp(strokeDp), strokeColor) }
}
