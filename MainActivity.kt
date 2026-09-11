package com.example.mayaassistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private lateinit var heard: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.statusText)
        heard = findViewById(R.id.heardText)
        val mic: ImageButton = findViewById(R.id.micButton)
        val defaultBtn: Button = findViewById(R.id.defaultAssistantButton)

        tts = TextToSpeech(this, this)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "শুনছি…" }
            override fun onBeginningOfSpeech() { status.text = "বলুন…" }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "ভাবছি…" }
            override fun onError(error: Int) { status.text = "আবার মাইক চাপুন" }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )?.firstOrNull() ?: return

                heard.text = "তুমি: $text"
                val result = AssistantCore.handle(this@MainActivity, text)
                speak(result.reply)
                result.action?.invoke()
                status.text = "মায়া প্রস্তুত"
            }
        })

        mic.setOnClickListener { listen() }

        findViewById<Button>(R.id.wakeButton).setOnClickListener { startWakeListener() }
        findViewById<Button>(R.id.stopWakeButton).setOnClickListener { stopWakeListener() }

        defaultBtn.setOnClickListener {
            try {
                startActivity(Intent("android.settings.VOICE_INPUT_SETTINGS"))
            } catch (_: Exception) {
                startActivity(Intent("android.settings.SETTINGS"))
            }
        }
    }

    private fun startWakeListener() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return
        }
        val intent = Intent(this, WakeWordService::class.java)
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
        } catch (_: Exception) { startService(intent) }
        status.text = "Hey Maya screen-off mode চালু"
        // Ask the user to exempt Maya from battery optimization when supported.
        try {
            val pm = getSystemService(PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                startActivity(Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, android.net.Uri.parse("package:$packageName")))
            }
        } catch (_: Exception) {}
    }

    private fun stopWakeListener() {
        stopService(Intent(this, WakeWordService::class.java))
        status.text = "Screen-off mode বন্ধ"
    }

    private fun listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(intent)
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "maya-v2")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val r = tts.setLanguage(Locale("bn", "BD"))
            if (r == TextToSpeech.LANG_MISSING_DATA ||
                r == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale.US
            }
        }
    }

    override fun onDestroy() {
        recognizer.destroy()
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
