package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommandType
import com.example.data.model.ConversationMessage
import com.example.data.model.MessageRole
import com.example.ui.theme.BiolumEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.HologramDim
import com.example.ui.theme.HologramMuted
import com.example.ui.theme.HologramWhite
import com.example.ui.theme.QuantumViolet
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.SpaceDarkSurface
import com.example.ui.theme.WarningAmber

@Composable
fun ConversationHistorySheet(
    messages: List<ConversationMessage>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .testTag("conversation_transcript"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageCard(message)
        }
    }
}

@Composable
private fun MessageCard(message: ConversationMessage) {
    val isUser = message.role == MessageRole.USER
    val isSystem = message.role == MessageRole.SYSTEM

    val cardBorderColor = when {
        message.isError -> GlitchCrimson.copy(alpha = 0.5f)
        isUser -> QuantumViolet.copy(alpha = 0.4f)
        isSystem -> WarningAmber.copy(alpha = 0.4f)
        else -> SpaceCardBorder
    }

    val cardBg = when {
        isUser -> SpaceCardSurface.copy(alpha = 0.9f)
        isSystem -> SpaceDarkSurface
        else -> SpaceDarkSurface.copy(alpha = 0.85f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag(if (isUser) "user_message_item" else "assistant_message_item")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            isUser -> "USER"
                            isSystem -> "SYSTEM"
                            else -> "AETHER CORE"
                        },
                        color = when {
                            isUser -> QuantumViolet
                            isSystem -> WarningAmber
                            message.isError -> GlitchCrimson
                            else -> CyberCyan
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    // Optional command classification badge
                    if (message.commandType != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CommandBadge(message.commandType, message.actionSuccess)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body text
            Text(
                text = message.text,
                color = if (message.isError) GlitchCrimson else HologramWhite,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun CommandBadge(
    commandType: CommandType,
    actionSuccess: Boolean?
) {
    val (label, icon, color) = when (commandType) {
        CommandType.NORMAL_AI_QUERY -> Triple("GEMINI AI", Icons.Default.Psychology, CyberCyan)
        CommandType.OPEN_APP_COMMAND -> {
            if (actionSuccess == true) {
                Triple("APP LAUNCHED", Icons.Default.CheckCircle, BiolumEmerald)
            } else {
                Triple("APP COMMAND", Icons.AutoMirrored.Filled.Launch, WarningAmber)
            }
        }
        CommandType.UNSUPPORTED_DEVICE_COMMAND -> Triple("RESTRICTED", Icons.Default.Info, WarningAmber)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
