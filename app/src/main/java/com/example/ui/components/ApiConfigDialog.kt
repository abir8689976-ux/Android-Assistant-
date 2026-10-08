package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BiolumEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.HologramMuted
import com.example.ui.theme.HologramWhite
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.SpaceDarkSurface
import com.example.ui.theme.WarningAmber

@Composable
fun ApiConfigDialog(
    currentApiKey: String,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
    onDismiss: () -> Unit
) {
    var inputKey by remember { mutableStateOf(currentApiKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val hasSavedKey = currentApiKey.isNotBlank()

    // Mask for display
    val maskedPreview = if (hasSavedKey) {
        val start = currentApiKey.take(6)
        "$start••••••••••••"
    } else {
        "None configured"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, SpaceCardBorder, RoundedCornerShape(22.dp))
                .testTag("api_config_dialog"),
            color = SpaceDarkSurface
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "API CONFIGURATION",
                            color = CyberCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Use Your Own API Key",
                            color = HologramWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Each user uses their own Google Gemini API key independently. Your key is stored private and securely on this device only—never bundled in shared APKs.",
                    color = HologramMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current Key Status Chip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpaceCardSurface)
                        .border(
                            1.dp,
                            if (hasSavedKey) BiolumEmerald.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasSavedKey) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (hasSavedKey) BiolumEmerald else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasSavedKey) "Personal Key Active" else "No Personal Key Set",
                                color = if (hasSavedKey) BiolumEmerald else WarningAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (hasSavedKey) {
                            Text(
                                text = maskedPreview,
                                color = HologramMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hidden Input Field
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    label = { Text("Gemini API Key", fontSize = 12.sp) },
                    placeholder = { Text("Paste AIzaSy...", color = HologramMuted) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            modifier = Modifier.testTag("toggle_api_key_visibility")
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide API key" else "Show API key",
                                tint = HologramMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SpaceCardSurface,
                        unfocusedContainerColor = SpaceCardSurface,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SpaceCardBorder,
                        focusedTextColor = HologramWhite,
                        unfocusedTextColor = HologramWhite,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = HologramMuted,
                        cursorColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tip: You can obtain a free key at aistudio.google.com/apikey",
                    color = CyberCyan.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons: Save Key / Clear Key / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasSavedKey) {
                        OutlinedButton(
                            onClick = {
                                onClearApiKey()
                                inputKey = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GlitchCrimson),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlitchCrimson.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("clear_api_key_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HologramMuted),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
                        ) {
                            Text("Close", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onSaveApiKey(inputKey.trim())
                                onDismiss()
                            },
                            enabled = inputKey.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = DeepSpaceBlack,
                                disabledContainerColor = SpaceCardSurface,
                                disabledContentColor = HologramMuted
                            ),
                            modifier = Modifier.testTag("save_api_key_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Key", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
