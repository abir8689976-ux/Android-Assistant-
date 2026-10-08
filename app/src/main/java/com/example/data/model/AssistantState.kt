package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BiolumEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.QuantumViolet

/**
 * Deterministic state machine states for Aether Assistant:
 * IDLE -> LISTENING -> PROCESSING -> SPEAKING -> IDLE
 * Any failure transitions safely to ERROR, with user-friendly recovery back to IDLE.
 */
enum class AssistantState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR;

    val labelEnglish: String
        get() = when (this) {
            IDLE -> "SYSTEM READY"
            LISTENING -> "LISTENING..."
            PROCESSING -> "PROCESSING..."
            SPEAKING -> "TRANSMITTING..."
            ERROR -> "ANOMALY DETECTED"
        }

    val labelBengali: String
        get() = when (this) {
            IDLE -> "সহকারী প্রস্তুত"
            LISTENING -> "শুনছি..."
            PROCESSING -> "বিশ্লেষণ করা হচ্ছে..."
            SPEAKING -> "উত্তর দেওয়া হচ্ছে..."
            ERROR -> "সমস্যা দেখা দিয়েছে"
        }

    val accentColor: Color
        get() = when (this) {
            IDLE -> CyberCyan
            LISTENING -> CyberCyan
            PROCESSING -> QuantumViolet
            SPEAKING -> BiolumEmerald
            ERROR -> GlitchCrimson
        }
}
