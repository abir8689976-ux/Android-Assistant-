package com.example.domain

import com.example.data.service.AppLaunchResult

data class NormalizedResponse(
    val displayText: String,
    val speechText: String,
    val isError: Boolean = false,
    val actionSuccess: Boolean? = null
)

interface ResponseProcessor {
    fun processGeminiResponse(rawText: String): NormalizedResponse
    fun processAppLaunchResult(result: AppLaunchResult): NormalizedResponse
    fun processUnsupportedCommand(userMessage: String): NormalizedResponse
    fun processError(error: Throwable): NormalizedResponse
    fun sanitizeForSpeech(text: String): String
}

class ResponseProcessorImpl : ResponseProcessor {

    override fun processGeminiResponse(rawText: String): NormalizedResponse {
        val cleanDisplay = rawText.trim()
        val speech = sanitizeForSpeech(cleanDisplay)
        return NormalizedResponse(
            displayText = cleanDisplay,
            speechText = speech,
            isError = false,
            actionSuccess = null
        )
    }

    override fun processAppLaunchResult(result: AppLaunchResult): NormalizedResponse {
        return when (result) {
            is AppLaunchResult.Success -> {
                NormalizedResponse(
                    displayText = result.message,
                    speechText = result.message,
                    isError = false,
                    actionSuccess = true
                )
            }
            is AppLaunchResult.Failed -> {
                NormalizedResponse(
                    displayText = result.reason,
                    speechText = sanitizeForSpeech(result.reason),
                    isError = true,
                    actionSuccess = false
                )
            }
        }
    }

    override fun processUnsupportedCommand(userMessage: String): NormalizedResponse {
        return NormalizedResponse(
            displayText = userMessage,
            speechText = sanitizeForSpeech(userMessage),
            isError = false,
            actionSuccess = false
        )
    }

    override fun processError(error: Throwable): NormalizedResponse {
        val message = error.localizedMessage ?: "An unexpected error occurred during processing."
        return NormalizedResponse(
            displayText = message,
            speechText = sanitizeForSpeech(message),
            isError = true,
            actionSuccess = false
        )
    }

    override fun sanitizeForSpeech(text: String): String {
        return text
            // Strip markdown formatting symbols like **, *, __, `
            .replace(Regex("[*#_`~>\\[\\]()]"), " ")
            // Strip markdown links
            .replace(Regex("https?://\\S+"), "link")
            // Collapse multiple spaces
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
