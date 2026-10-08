package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.ui.theme.BiolumEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.HologramMuted
import com.example.ui.theme.HologramWhite
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.SpaceDarkSurface

@Composable
fun AssistantControlBar(
    state: AssistantState,
    selectedLanguage: String,
    onMicClick: () -> Unit,
    onSendText: (String) -> Unit,
    onStopSpeech: () -> Unit,
    onResetError: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var isTextModeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Language Selector Pills
        Row(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SpaceDarkSurface)
                .border(1.dp, SpaceCardBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguagePill(
                label = "AUTO",
                isSelected = selectedLanguage == "AUTO",
                onClick = { onLanguageSelected("AUTO") }
            )
            LanguagePill(
                label = "বাংলা",
                isSelected = selectedLanguage == "BN",
                onClick = { onLanguageSelected("BN") }
            )
            LanguagePill(
                label = "EN",
                isSelected = selectedLanguage == "EN",
                onClick = { onLanguageSelected("EN") }
            )
        }

        // Secondary controls: Stop speech if speaking, or recover from error
        AnimatedVisibility(
            visible = state == AssistantState.SPEAKING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpaceCardSurface)
                    .border(1.dp, BiolumEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onStopSpeech() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("stop_speech_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speaking",
                        tint = BiolumEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STOP TRANSMISSION",
                        color = BiolumEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = state == AssistantState.ERROR,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpaceCardSurface)
                    .border(1.dp, GlitchCrimson.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onResetError() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("reset_error_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Assistant",
                        tint = GlitchCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECOVER SYSTEM",
                        color = GlitchCrimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Primary Voice Interaction Core Button (72dp tactile touch target)
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .size(76.dp)
                .shadow(
                    elevation = if (state == AssistantState.LISTENING) 16.dp else 8.dp,
                    shape = CircleShape,
                    spotColor = state.accentColor
                )
                .clip(CircleShape)
                .background(SpaceDarkSurface)
                .border(2.dp, state.accentColor, CircleShape)
                .clickable { onMicClick() }
                .testTag("mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (state) {
                    AssistantState.LISTENING -> Icons.Default.MicOff
                    AssistantState.SPEAKING -> Icons.Default.Stop
                    AssistantState.ERROR -> Icons.Default.Refresh
                    else -> Icons.Default.Mic
                },
                contentDescription = "Voice Control",
                tint = state.accentColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Text input fallback bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SpaceDarkSurface)
                .border(1.dp, SpaceCardBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("text_input"),
                placeholder = {
                    Text(
                        text = if (selectedLanguage == "BN") "একটি প্রশ্ন লিখুন..." else "Ask or type a command...",
                        color = HologramMuted,
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = HologramWhite,
                    unfocusedTextColor = HologramWhite,
                    cursorColor = CyberCyan
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.isNotBlank()) {
                            onSendText(textInput)
                            textInput = ""
                        }
                    }
                )
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendText(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Command",
                    tint = if (textInput.isNotBlank()) CyberCyan else HologramMuted
                )
            }
        }
    }
}

@Composable
private fun LanguagePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) CyberCyan else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) CyberCyan else HologramMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
