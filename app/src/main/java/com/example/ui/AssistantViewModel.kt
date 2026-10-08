package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AssistantState
import com.example.data.model.CommandClassification
import com.example.data.model.CommandType
import com.example.data.model.ConversationMessage
import com.example.data.model.MessageRole
import com.example.data.service.ApiKeyStorage
import com.example.data.service.ApiKeyStorageImpl
import com.example.data.service.AppLauncherService
import com.example.data.service.AppLauncherServiceImpl
import com.example.data.service.CommandRouter
import com.example.data.service.CommandRouterImpl
import com.example.data.service.GeminiService
import com.example.data.service.GeminiServiceImpl
import com.example.data.service.VoiceInputService
import com.example.data.service.VoiceInputServiceImpl
import com.example.data.service.VoiceOutputService
import com.example.data.service.VoiceOutputServiceImpl
import com.example.domain.AssistantPersona
import com.example.domain.NormalizedResponse
import com.example.domain.ResponseProcessor
import com.example.domain.ResponseProcessorImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel @JvmOverloads constructor(
    application: Application,
    private val apiKeyStorage: ApiKeyStorage = ApiKeyStorageImpl(application.applicationContext),
    private val geminiService: GeminiService = GeminiServiceImpl(apiKeyStorage),
    private val commandRouter: CommandRouter = CommandRouterImpl(),
    private val appLauncherService: AppLauncherService = AppLauncherServiceImpl(application.applicationContext),
    private val voiceInputService: VoiceInputService = VoiceInputServiceImpl(application.applicationContext),
    private val voiceOutputService: VoiceOutputService = VoiceOutputServiceImpl(application.applicationContext),
    private val responseProcessor: ResponseProcessor = ResponseProcessorImpl()
) : AndroidViewModel(application) {

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _messages = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val messages: StateFlow<List<ConversationMessage>> = _messages.asStateFlow()

    private val _systemInstruction = MutableStateFlow(AssistantPersona.DEFAULT_SYSTEM_INSTRUCTION)
    val systemInstruction: StateFlow<String> = _systemInstruction.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("AUTO") // "AUTO", "BN", "EN"
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _customApiKey = MutableStateFlow(apiKeyStorage.getCustomApiKey())
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _statusDetail = MutableStateFlow("Tap microphone or enter text to begin")
    val statusDetail: StateFlow<String> = _statusDetail.asStateFlow()

    val rmsAudioLevel: StateFlow<Float> = voiceInputService.rmsAudioLevel
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    init {
        // Welcome message establishing persona and bilingual capability
        val welcomeMsg = ConversationMessage(
            role = MessageRole.ASSISTANT,
            text = "Aether AI online. Ask me anything in English or বাংলা, or give voice commands."
        )
        _messages.value = listOf(welcomeMsg)
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun updateSystemInstruction(newInstruction: String) {
        if (newInstruction.isNotBlank()) {
            _systemInstruction.value = newInstruction.trim()
        }
    }

    fun resetSystemInstruction() {
        _systemInstruction.value = AssistantPersona.DEFAULT_SYSTEM_INSTRUCTION
    }

    fun saveCustomApiKey(key: String) {
        apiKeyStorage.saveCustomApiKey(key)
        _customApiKey.value = apiKeyStorage.getCustomApiKey()
        _statusDetail.value = if (isBengaliMode()) "ব্যক্তিগত API Key সংরক্ষিত হয়েছে" else "Personal API Key saved"
    }

    fun clearCustomApiKey() {
        apiKeyStorage.clearCustomApiKey()
        _customApiKey.value = ""
        _statusDetail.value = if (isBengaliMode()) "ব্যক্তিগত API Key মুছে ফেলা হয়েছে" else "Personal API Key cleared"
    }

    /**
     * Triggered when user taps the microphone button.
     */
    fun onMicTapped() {
        when (_assistantState.value) {
            AssistantState.IDLE -> {
                startListening()
            }
            AssistantState.LISTENING -> {
                cancelListening()
            }
            AssistantState.SPEAKING -> {
                stopSpeaking()
            }
            AssistantState.PROCESSING -> {
                // Currently processing query, wait or ignore
            }
            AssistantState.ERROR -> {
                resetToIdle()
                startListening()
            }
        }
    }

    fun startListening() {
        if (!voiceInputService.isAvailable()) {
            transitionToError("Speech recognition is unavailable on this device. Please use text input.")
            return
        }

        _assistantState.value = AssistantState.LISTENING
        _statusDetail.value = if (isBengaliMode()) "কথা বলুন..." else "Listening for your voice..."

        val langCode = when (_selectedLanguage.value) {
            "BN" -> "bn-BD"
            "EN" -> "en-US"
            else -> null
        }

        voiceInputService.startListening(
            languageCode = langCode,
            onReady = {
                _statusDetail.value = if (isBengaliMode()) "শুনছি, বলুন..." else "Listening..."
            },
            onResult = { userQuery ->
                processUserInput(userQuery)
            },
            onError = { errorMsg ->
                transitionToError(errorMsg)
            }
        )
    }

    fun cancelListening() {
        voiceInputService.stopListening()
        resetToIdle()
    }

    fun stopSpeaking() {
        voiceOutputService.stop()
        resetToIdle()
    }

    fun resetToIdle() {
        _assistantState.value = AssistantState.IDLE
        _statusDetail.value = if (isBengaliMode()) "সহকারী প্রস্তুত" else "System ready"
    }

    /**
     * Core processing pipeline:
     * User Text -> Command Router -> Gemini or AppLauncher -> Response Processor -> TTS -> State Machine
     */
    fun processUserInput(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            resetToIdle()
            return
        }

        // Add user message to session transcript
        val userMsg = ConversationMessage(
            role = MessageRole.USER,
            text = trimmed
        )
        _messages.value = _messages.value + userMsg

        // Transition: IDLE/LISTENING -> PROCESSING
        _assistantState.value = AssistantState.PROCESSING
        _statusDetail.value = if (trimmed.any { it in '\u0980'..'\u09FF' }) {
            "বিশ্লেষণ করা হচ্ছে..."
        } else {
            "Processing query..."
        }

        viewModelScope.launch {
            // Step 1: Classify command through extensible Command Router
            val classification = commandRouter.classify(trimmed)

            when (classification) {
                is CommandClassification.NormalAiQuery -> {
                    handleAiQuery(classification.query)
                }
                is CommandClassification.OpenAppCommand -> {
                    handleOpenApp(classification)
                }
                is CommandClassification.UnsupportedDeviceCommand -> {
                    handleUnsupportedDevice(classification)
                }
            }
        }
    }

    private suspend fun handleAiQuery(query: String) {
        val result = geminiService.generateResponse(
            prompt = query,
            history = _messages.value,
            systemInstruction = _systemInstruction.value
        )

        result.onSuccess { rawResponse ->
            val normalized = responseProcessor.processGeminiResponse(rawResponse)
            deliverAssistantResponse(
                normalized = normalized,
                commandType = CommandType.NORMAL_AI_QUERY
            )
        }.onFailure { error ->
            val normalized = responseProcessor.processError(error)
            deliverAssistantResponse(
                normalized = normalized,
                commandType = CommandType.NORMAL_AI_QUERY
            )
        }
    }

    private fun handleOpenApp(command: CommandClassification.OpenAppCommand) {
        val isBengali = command.rawQuery.any { it in '\u0980'..'\u09FF' }
        val launchResult = appLauncherService.openApp(
            appKey = command.appKey,
            displayName = command.targetName,
            isBengali = isBengali
        )

        val normalized = responseProcessor.processAppLaunchResult(launchResult)
        deliverAssistantResponse(
            normalized = normalized,
            commandType = CommandType.OPEN_APP_COMMAND
        )
    }

    private fun handleUnsupportedDevice(command: CommandClassification.UnsupportedDeviceCommand) {
        val normalized = responseProcessor.processUnsupportedCommand(command.userMessage)
        deliverAssistantResponse(
            normalized = normalized,
            commandType = CommandType.UNSUPPORTED_DEVICE_COMMAND
        )
    }

    private fun deliverAssistantResponse(
        normalized: NormalizedResponse,
        commandType: CommandType
    ) {
        val assistantMsg = ConversationMessage(
            role = MessageRole.ASSISTANT,
            text = normalized.displayText,
            commandType = commandType,
            actionSuccess = normalized.actionSuccess,
            isError = normalized.isError
        )
        _messages.value = _messages.value + assistantMsg

        if (normalized.isError) {
            _assistantState.value = AssistantState.ERROR
            _statusDetail.value = normalized.displayText
            // Try speaking error or reset after brief readout
            voiceOutputService.speak(
                text = normalized.speechText,
                onStart = { /* speaking error */ },
                onDone = { /* finished speaking error */ },
                onError = { /* fallback */ }
            )
        } else {
            // Transition: PROCESSING -> SPEAKING
            _assistantState.value = AssistantState.SPEAKING
            _statusDetail.value = if (normalized.displayText.any { it in '\u0980'..'\u09FF' }) {
                "উত্তর দেওয়া হচ্ছে..."
            } else {
                "Transmitting response..."
            }

            voiceOutputService.speak(
                text = normalized.speechText,
                onStart = {
                    _assistantState.value = AssistantState.SPEAKING
                },
                onDone = {
                    // Transition: SPEAKING -> IDLE
                    resetToIdle()
                },
                onError = { _ ->
                    // TTS failed/unavailable: still succeed visually and return to IDLE
                    resetToIdle()
                }
            )
        }
    }

    private fun transitionToError(errorMsg: String) {
        _assistantState.value = AssistantState.ERROR
        _statusDetail.value = errorMsg
        val errorConversationMsg = ConversationMessage(
            role = MessageRole.SYSTEM,
            text = errorMsg,
            isError = true
        )
        _messages.value = _messages.value + errorConversationMsg
    }

    private fun isBengaliMode(): Boolean {
        return _selectedLanguage.value == "BN"
    }

    fun clearTranscript() {
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        voiceInputService.destroy()
        voiceOutputService.shutdown()
    }
}
