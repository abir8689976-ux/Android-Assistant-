package com.example.data.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

interface VoiceInputService {
    val rmsAudioLevel: StateFlow<Float>
    fun isAvailable(): Boolean
    fun startListening(
        languageCode: String? = null,
        onReady: () -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun stopListening()
    fun destroy()
}

class VoiceInputServiceImpl(private val context: Context) : VoiceInputService {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val _rmsAudioLevel = MutableStateFlow(0f)
    override val rmsAudioLevel: StateFlow<Float> = _rmsAudioLevel.asStateFlow()

    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    override fun startListening(
        languageCode: String?,
        onReady: () -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isAvailable()) {
            onError("Speech recognition service is not available on this device. Please use text input.")
            return
        }

        mainHandler.post {
            try {
                // Clean any previous instance
                try {
                    speechRecognizer?.destroy()
                } catch (_: Exception) {}
                speechRecognizer = null

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            onReady()
                        }

                        override fun onBeginningOfSpeech() {}

                        override fun onRmsChanged(rmsdB: Float) {
                            // Normalize -2dB..10dB to 0f..1f for visualizer orb
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                            _rmsAudioLevel.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _rmsAudioLevel.value = 0f
                        }

                        override fun onError(error: Int) {
                            _rmsAudioLevel.value = 0f
                            val message = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check microphone."
                                SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied."
                                SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition."
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout during speech recognition."
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly into the microphone."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Please try again."
                                SpeechRecognizer.ERROR_SERVER -> "Recognition server error. Please try again."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected within time limit."
                                else -> "Speech recognition error ($error)."
                            }
                            onError(message)
                        }

                        override fun onResults(results: Bundle?) {
                            _rmsAudioLevel.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()?.trim().orEmpty()
                            if (text.isNotEmpty()) {
                                onResult(text)
                            } else {
                                onError("No words detected. Please try again.")
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

                    val selectedLang = when (languageCode?.lowercase()) {
                        "bn", "bn-bd", "bangla", "bengali" -> "bn-BD"
                        "en", "en-us", "english" -> "en-US"
                        else -> Locale.getDefault().toLanguageTag()
                    }
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLang)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLang)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _rmsAudioLevel.value = 0f
                onError("Unable to initialize speech recognizer: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    override fun stopListening() {
        mainHandler.post {
            _rmsAudioLevel.value = 0f
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
        }
    }

    override fun destroy() {
        mainHandler.post {
            _rmsAudioLevel.value = 0f
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
        }
    }
}
