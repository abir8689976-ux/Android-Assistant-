package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.ui.theme.BiolumEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HologramMuted
import com.example.ui.theme.HologramWhite
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDarkSurface

@Composable
fun StatusIndicator(
    state: AssistantState,
    detailText: String,
    hasCustomApiKey: Boolean,
    onOpenApiConfig: () -> Unit,
    onOpenPersonaSettings: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val beaconColor by animateColorAsState(
        targetValue = state.accentColor,
        animationSpec = tween(400),
        label = "BeaconColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SpaceDarkSurface)
            .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("status_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Glowing status beacon
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(beaconColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.labelEnglish,
                        color = beaconColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${state.labelBengali}",
                        color = HologramMuted,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detailText,
                    color = HologramWhite,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // API Configuration button
            IconButton(
                onClick = onOpenApiConfig,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("api_config_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "API Configuration",
                    tint = if (hasCustomApiKey) BiolumEmerald else CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Persona settings button
            IconButton(
                onClick = onOpenPersonaSettings,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("persona_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Assistant Persona Settings",
                    tint = HologramMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Clear history button
            IconButton(
                onClick = onClearHistory,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("clear_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Session History",
                    tint = HologramMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
