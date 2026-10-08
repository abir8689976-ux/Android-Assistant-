package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.HologramMuted
import com.example.ui.theme.HologramWhite
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceCardSurface
import com.example.ui.theme.SpaceDarkSurface

@Composable
fun PersonaConfigDialog(
    currentInstruction: String,
    onSave: (String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(currentInstruction) { mutableStateOf(currentInstruction) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SpaceCardBorder, RoundedCornerShape(20.dp))
                .testTag("persona_dialog"),
            color = SpaceDarkSurface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "AI PERSONA ARCHITECTURE",
                    color = CyberCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Configure system instructions governing Gemini reasoning, bilingual behavior, and action constraints.",
                    color = HologramMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("persona_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SpaceCardSurface,
                        unfocusedContainerColor = SpaceCardSurface,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SpaceCardBorder,
                        focusedTextColor = HologramWhite,
                        unfocusedTextColor = HologramWhite,
                        cursorColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HologramMuted),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.testTag("reset_persona_button")
                    ) {
                        Text("Reset Default", fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HologramMuted)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onSave(text)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = DeepSpaceBlack
                            ),
                            modifier = Modifier.testTag("save_persona_button")
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
