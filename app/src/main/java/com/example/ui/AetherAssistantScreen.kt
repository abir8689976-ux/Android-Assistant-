package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.data.model.MessageRole
import com.example.ui.components.AiCoreOrb
import com.example.ui.components.ApiConfigDialog
import com.example.ui.components.AssistantControlBar
import com.example.ui.components.ConversationHistorySheet
import com.example.ui.components.PersonaConfigDialog
import com.example.ui.components.StatusIndicator
import com.example.ui.theme.BiolumEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.HologramMuted
import com.example.ui.theme.HologramWhite
import com.example.ui.theme.SpaceDarkSurface

@Composable
fun AetherAssistantScreen(
    viewModel: AssistantViewModel,
    onRequestMicrophonePermission: () -> Unit,
    hasMicrophonePermission: Boolean,
    modifier: Modifier = Modifier
) {
    val state by viewModel.assistantState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val systemInstruction by viewModel.systemInstruction.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val statusDetail by viewModel.statusDetail.collectAsState()
    val rmsLevel by viewModel.rmsAudioLevel.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    var showPersonaDialog by remember { mutableStateOf(false) }
    var showApiConfigDialog by remember { mutableStateOf(false) }

    // Identify last assistant utterance or user query for live HUD readout
    val latestMessage = messages.lastOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
            .testTag("aether_assistant_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        val maxHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP HUD SECTION ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Branding Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AETHER CORE",
                            color = CyberCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "GEMINI 3.5 FLASH • BILINGUAL",
                            color = HologramMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpaceDarkSurface)
                            .clickable { showApiConfigDialog = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("api_status_badge")
                    ) {
                        Text(
                            text = if (customApiKey.isNotBlank()) "CUSTOM KEY ON" else "CONFIG KEY",
                            color = if (customApiKey.isNotBlank()) BiolumEmerald else CyberCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Telemetry Status Bar with API Config access
                StatusIndicator(
                    state = state,
                    detailText = statusDetail,
                    hasCustomApiKey = customApiKey.isNotBlank(),
                    onOpenApiConfig = { showApiConfigDialog = true },
                    onOpenPersonaSettings = { showPersonaDialog = true },
                    onClearHistory = { viewModel.clearTranscript() }
                )
            }

            // --- CENTER STAGE: AI CORE ORB & LIVE GLANCE HUD ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val orbSize = if (maxHeight < 680.dp) 180.dp else 220.dp

                AiCoreOrb(
                    state = state,
                    rmsLevel = rmsLevel,
                    size = orbSize
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Glanceable live subtitle/dialogue
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpaceDarkSurface.copy(alpha = 0.7f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (state) {
                            AssistantState.LISTENING -> "Speaking into microphone..."
                            AssistantState.PROCESSING -> "Synthesizing with Gemini engine..."
                            AssistantState.SPEAKING -> latestMessage?.text ?: "Transmitting response..."
                            AssistantState.ERROR -> statusDetail
                            AssistantState.IDLE -> latestMessage?.text ?: "Ready for your voice or question."
                        },
                        color = if (state == AssistantState.ERROR) state.accentColor else HologramWhite,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 3
                    )
                }
            }

            // --- TRANSCRIPT HUD PREVIEW (SCROLLABLE RECENT CONVERSATIONS) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceDarkSurface.copy(alpha = 0.5f))
                    .padding(vertical = 4.dp)
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Transcript empty. Tap mic or type below.",
                            color = HologramMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    ConversationHistorySheet(
                        messages = messages,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // --- BOTTOM CONTROL BAR ---
            AssistantControlBar(
                state = state,
                selectedLanguage = selectedLanguage,
                onMicClick = {
                    if (hasMicrophonePermission) {
                        viewModel.onMicTapped()
                    } else {
                        onRequestMicrophonePermission()
                    }
                },
                onSendText = { text ->
                    viewModel.processUserInput(text)
                },
                onStopSpeech = {
                    viewModel.stopSpeaking()
                },
                onResetError = {
                    viewModel.resetToIdle()
                },
                onLanguageSelected = { lang ->
                    viewModel.setLanguage(lang)
                }
            )
        }
    }

    // Persona Dialog
    if (showPersonaDialog) {
        PersonaConfigDialog(
            currentInstruction = systemInstruction,
            onSave = { updatedPrompt ->
                viewModel.updateSystemInstruction(updatedPrompt)
            },
            onReset = {
                viewModel.resetSystemInstruction()
            },
            onDismiss = {
                showPersonaDialog = false
            }
        )
    }

    // Personal API Configuration Dialog
    if (showApiConfigDialog) {
        ApiConfigDialog(
            currentApiKey = customApiKey,
            onSaveApiKey = { newKey ->
                viewModel.saveCustomApiKey(newKey)
            },
            onClearApiKey = {
                viewModel.clearCustomApiKey()
            },
            onDismiss = {
                showApiConfigDialog = false
            }
        )
    }
}
