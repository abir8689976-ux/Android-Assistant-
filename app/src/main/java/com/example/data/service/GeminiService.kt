package com.example.data.service

import com.example.BuildConfig
import com.example.data.model.ConversationMessage
import com.example.data.model.GeminiCandidate
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiErrorDetails
import com.example.data.model.GeminiGenerateContentRequest
import com.example.data.model.GeminiGenerateContentResponse
import com.example.data.model.GeminiGenerationConfig
import com.example.data.model.GeminiPart
import com.example.data.model.MessageRole
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

interface GeminiService {
    suspend fun generateResponse(
        prompt: String,
        history: List<ConversationMessage>,
        systemInstruction: String,
        apiKeyOverride: String? = null
    ): Result<String>
}

class GeminiServiceImpl(
    private val apiKeyStorage: ApiKeyStorage? = null
) : GeminiService {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiGenerateContentRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiGenerateContentResponse::class.java)

    // Recommended 60-second timeouts per Gemini skill guidelines
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(
        prompt: String,
        history: List<ConversationMessage>,
        systemInstruction: String,
        apiKeyOverride: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        val resolvedKey = apiKeyOverride?.trim()?.ifEmpty { null }
            ?: apiKeyStorage?.getEffectiveApiKey()
            ?: BuildConfig.GEMINI_API_KEY.trim()
        val apiKey = resolvedKey.trim()
        val isBengali = prompt.any { it in '\u0980'..'\u09FF' }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val missingMsg = if (isBengali) {
                "Gemini API কী সেট করা হয়নি। অনুগ্রহ করে সেটিংস-এ গিয়ে 'API Configuration'-এ আপনার নিজস্ব Gemini API Key যোগ করুন।"
            } else {
                "Gemini API key is not configured. Please open Settings -> 'API Configuration' and enter your Gemini API key."
            }
            return@withContext Result.failure(IllegalStateException(missingMsg))
        }

        // Build turn history
        val contentsList = mutableListOf<GeminiContent>()

        // Add previous conversational context (last 8 turns for responsive latency)
        val relevantHistory = history
            .filter { it.role == MessageRole.USER || it.role == MessageRole.ASSISTANT }
            .takeLast(8)

        for (msg in relevantHistory) {
            val role = if (msg.role == MessageRole.USER) "user" else "model"
            contentsList.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = msg.text))
                )
            )
        }

        // Add current user prompt
        contentsList.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = prompt))
            )
        )

        val systemContent = GeminiContent(
            parts = listOf(GeminiPart(text = systemInstruction))
        )

        val requestPayload = GeminiGenerateContentRequest(
            contents = contentsList,
            systemInstruction = systemContent,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                topK = 40,
                maxOutputTokens = 1024
            )
        )

        val jsonString = requestAdapter.toJson(requestPayload)
        val requestBody = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())

        // Pass API key securely via query param and header without ever logging it
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .post(requestBody)
            .build()

        try {
            client.newCall(httpRequest).execute().use { response ->
                val responseBodyString = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val errorParsed = try {
                        responseAdapter.fromJson(responseBodyString)?.error
                    } catch (_: Exception) {
                        null
                    }

                    val code = response.code
                    val errorDetail = errorParsed?.message ?: "HTTP $code"

                    val userMessage = when (code) {
                        400 -> if (isBengali) {
                            "অনুরোধটি অবৈধ অথবা API কী সঠিক নয় ($errorDetail)। আপনার API Key সঠিক কি না পরীক্ষা করুন।"
                        } else {
                            "Invalid API request or invalid API key ($errorDetail). Please verify your API key in Settings."
                        }
                        403 -> if (isBengali) {
                            "অনুমতি প্রত্যাখ্যান করা হয়েছে ($errorDetail)। API কী-এর অনুমতি পরীক্ষা করুন।"
                        } else {
                            "Access denied ($errorDetail). Please verify your API key permissions."
                        }
                        429 -> if (isBengali) {
                            "Gemini কোটা বা অনুরোধের সীমা পূর্ণ হয়েছে। কিছুক্ষণ পর পুনরায় চেষ্টা করুন।"
                        } else {
                            "Gemini rate limit exceeded. Please wait a moment and try again."
                        }
                        500, 503 -> if (isBengali) {
                            "Gemini ক্লাউড সেবা সাময়িকভাবে অনুপলব্ধ ($code)। কিছুক্ষণ পর চেষ্টা করুন।"
                        } else {
                            "Gemini service temporarily unavailable ($code). Please try again shortly."
                        }
                        else -> if (isBengali) {
                            "Gemini এআই ত্রুটি ($code): $errorDetail"
                        } else {
                            "Gemini API error ($code): $errorDetail"
                        }
                    }
                    return@withContext Result.failure(Exception(userMessage))
                }

                val parsed = responseAdapter.fromJson(responseBodyString)
                val textCandidate = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!textCandidate.isNullOrBlank()) {
                    Result.success(textCandidate.trim())
                } else {
                    val emptyMsg = if (isBengali) {
                        "Gemini ইঞ্জিন থেকে কোনো টেক্সট পাওয়া যায়নি।"
                    } else {
                        "Empty response received from Gemini engine."
                    }
                    Result.failure(Exception(emptyMsg))
                }
            }
        } catch (_: SocketTimeoutException) {
            val timeoutMsg = if (isBengali) {
                "Gemini সংযোগের সময় শেষ হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করে পুনরায় চেষ্টা করুন।"
            } else {
                "Connection to Gemini timed out. Please check your network and retry."
            }
            Result.failure(Exception(timeoutMsg))
        } catch (_: IOException) {
            val netMsg = if (isBengali) {
                "নেটওয়ার্ক সংযোগ পাওয়া যায়নি। ইন্টারনেট সংযোগ সচল আছে কি না দেখুন।"
            } else {
                "Network error: Unable to reach Gemini API. Please check your internet connection."
            }
            Result.failure(Exception(netMsg))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Unexpected error during Gemini processing."))
        }
    }
}
