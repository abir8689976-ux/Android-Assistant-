package com.example.data.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

interface VoiceOutputService {
    val isInitialized: Boolean
    fun speak(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    )
    fun stop()
    fun shutdown()
}

class VoiceOutputServiceImpl(private val context: Context) : VoiceOutputService, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    override var isInitialized: Boolean = false
        private set

    private var activeUtteranceId: String? = null
    private var pendingSpeech: (() -> Unit)? = null
    private var onStartCallback: (() -> Unit)? = null
    private var onDoneCallback: (() -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Throwable) {
            isInitialized = false
            tts = null
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        onStartCallback?.invoke()
                    }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        onDoneCallback?.invoke()
                        cleanupCallbacks()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        onErrorCallback?.invoke("Text-to-speech synthesis failed.")
                        cleanupCallbacks()
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (utteranceId == activeUtteranceId) {
                        onErrorCallback?.invoke("TTS error code: $errorCode")
                        cleanupCallbacks()
                    }
                }
            })

            pendingSpeech?.invoke()
            pendingSpeech = null
        } else {
            isInitialized = false
            pendingSpeech = null
        }
    }

    override fun speak(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) {
            onDone()
            return
        }

        if (!isInitialized) {
            if (tts != null) {
                pendingSpeech = {
                    speak(cleanText, onStart, onDone, onError)
                }
                return
            }
            onError("Text-to-speech engine is unavailable on this system.")
            return
        }

        this.onStartCallback = onStart
        this.onDoneCallback = onDone
        this.onErrorCallback = onError

        val utteranceId = UUID.randomUUID().toString()
        this.activeUtteranceId = utteranceId

        // Language script selection: Bengali if contains Bengali unicode characters
        val isBengali = cleanText.any { it in '\u0980'..'\u09FF' }
        val targetLocale = if (isBengali) {
            Locale.forLanguageTag("bn-BD")
        } else {
            Locale.US
        }

        try {
            val langResult = tts?.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale if specific locale unsupported
                tts?.setLanguage(Locale.getDefault())
            }

            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            onError("TTS error: ${e.localizedMessage ?: "Unknown"}")
            cleanupCallbacks()
        }
    }

    override fun stop() {
        pendingSpeech = null
        try {
            tts?.stop()
        } catch (_: Exception) {}
        cleanupCallbacks()
    }

    override fun shutdown() {
        pendingSpeech = null
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
        cleanupCallbacks()
    }

    private fun cleanupCallbacks() {
        activeUtteranceId = null
        onStartCallback = null
        onDoneCallback = null
        onErrorCallback = null
    }
}
