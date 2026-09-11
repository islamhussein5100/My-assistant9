package com.example.mayaassistant

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Best-effort screen-off wake listener.
 *
 * This keeps a microphone foreground service alive after the user starts it
 * while the app is visible. It uses Android SpeechRecognizer to listen for
 * "Hey Maya" / "হেই মায়া" and then dispatches the following command.
 *
 * Important: this is NOT the same as an OEM/system hotword DSP. Android and
 * phone manufacturers can suspend/kill background microphone work, and newer
 * Android versions impose additional restrictions. The official
 * VoiceInteractionService remains the preferred system-assistant path.
 */
class WakeWordService : Service(), RecognitionListener, TextToSpeech.OnInitListener {
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private val handler = Handler(Looper.getMainLooper())
    private var listening = false

    private val restart = object : Runnable {
        override fun run() {
            if (!listening) startListeningLoop()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification = Notification.Builder(this, CHANNEL)
            .setContentTitle("Maya Assistant")
            .setContentText("Hey Maya screen-off listener চালু আছে")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(this)
        tts = TextToSpeech(this, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListeningLoop()
        }
        return START_STICKY
    }

    private fun startListeningLoop() {
        if (!::recognizer.isInitialized || listening) return
        listening = true
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try { recognizer.startListening(i) } catch (_: Exception) {
            listening = false
            handler.postDelayed(restart, 1200)
        }
    }

    override fun onResults(results: Bundle?) {
        listening = false
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
        val lower = text.lowercase(Locale.getDefault())
        val wake = lower.contains("hey maya") || lower.contains("হেই মায়া") ||
                lower.contains("হে মায়া") || lower.contains("হে মায়া") || lower.contains("হেই মায়া")
        if (wake) {
            val command = lower.substringAfter("hey maya", "").trim()
                .substringAfter("হেই মায়া", "").trim()
                .substringAfter("হেই মায়া", "").trim()
                .substringAfter("হে মায়া", "").trim()
                .substringAfter("হে মায়া", "").trim()
            if (command.isNotBlank()) execute(command) else speak("জি, বলুন।")
        }
        handler.postDelayed(restart, 350)
    }

    private fun execute(command: String) {
        val result = AssistantCore.handle(this, command)
        speak(result.reply)
        try { result.action?.invoke() } catch (_: Exception) { speak("এই কাজটা এখন করা গেল না।") }
    }

    private fun speak(text: String) {
        if (::tts.isInitialized) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "maya-wake")
    }

    override fun onError(error: Int) {
        listening = false
        handler.postDelayed(restart, 800)
    }
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale("bn", "BD")
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Maya screen-off voice", NotificationManager.IMPORTANCE_LOW))
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::recognizer.isInitialized) recognizer.destroy()
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL = "maya_wake"
        private const val NOTIFICATION_ID = 42
    }
}
