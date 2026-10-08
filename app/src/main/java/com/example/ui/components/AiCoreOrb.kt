package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantState
import com.example.ui.theme.BiolumEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlitchCrimson
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.QuantumViolet
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lightweight, futuristic animated AI Core / Orb.
 * Reacts visually and deterministically to IDLE, LISTENING, PROCESSING, SPEAKING, ERROR.
 * Optimized for low-end and mid-range Android devices with purely math-based Canvas rendering.
 */
@Composable
fun AiCoreOrb(
    state: AssistantState,
    rmsLevel: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    val transition = rememberInfiniteTransition(label = "OrbTransitions")

    // Slow breath for idle/ambient
    val breathScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathScale"
    )

    // Continuous orbital rotation
    val spinAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.PROCESSING -> 1400
                    AssistantState.SPEAKING -> 2800
                    AssistantState.LISTENING -> 3200
                    AssistantState.ERROR -> 8000
                    AssistantState.IDLE -> 5000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinAngle"
    )

    // Wave oscillation for speaking / processing
    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("ai_core_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 3.4f

            // Dynamic scale influenced by audio RMS level when listening or speaking
            val dynamicBoost = when (state) {
                AssistantState.LISTENING -> rmsLevel * 0.35f
                AssistantState.SPEAKING -> (sin(wavePhase) * 0.08f) + 0.04f
                AssistantState.PROCESSING -> 0.05f
                AssistantState.ERROR -> 0.02f
                AssistantState.IDLE -> 0f
            }
            val activeScale = (breathScale + dynamicBoost).coerceIn(0.8f, 1.4f)
            val coreRadius = baseRadius * activeScale

            when (state) {
                AssistantState.IDLE -> {
                    // Outer diffuse glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyberCyan.copy(alpha = 0.35f),
                                ElectricBlue.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = coreRadius * 1.5f
                        ),
                        radius = coreRadius * 1.5f,
                        center = center
                    )

                    // Concentric orbital energy ring
                    drawArc(
                        color = CyberCyan.copy(alpha = 0.6f),
                        startAngle = spinAngle,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = Offset(center.x - coreRadius * 1.25f, center.y - coreRadius * 1.25f),
                        size = androidx.compose.ui.geometry.Size(coreRadius * 2.5f, coreRadius * 2.5f),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Inner energy nucleus
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                CyberCyan,
                                ElectricBlue,
                                Color(0xFF00152B)
                            ),
                            center = center,
                            radius = coreRadius
                        ),
                        radius = coreRadius,
                        center = center
                    )
                }

                AssistantState.LISTENING -> {
                    // Sonic ripple aura expanding with voice volume
                    val rippleRadius = coreRadius * (1.3f + rmsLevel * 0.6f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyberCyan.copy(alpha = 0.5f),
                                ElectricBlue.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = rippleRadius
                        ),
                        radius = rippleRadius,
                        center = center
                    )

                    // Triple resonant audio reactive rings
                    for (i in 1..3) {
                        val ringRadius = coreRadius * (1f + (i * 0.2f * (0.5f + rmsLevel)))
                        drawCircle(
                            color = CyberCyan.copy(alpha = 0.7f - (i * 0.18f)),
                            radius = ringRadius,
                            center = center,
                            style = Stroke(width = (3.dp.toPx() - i), cap = StrokeCap.Round)
                        )
                    }

                    // Bright listening nucleus
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                CyberCyan,
                                ElectricBlue
                            ),
                            center = center,
                            radius = coreRadius
                        ),
                        radius = coreRadius,
                        center = center
                    )
                }

                AssistantState.PROCESSING -> {
                    // Quantum energy vortex shimmer in Violet / Magenta
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonMagenta.copy(alpha = 0.4f),
                                QuantumViolet.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = coreRadius * 1.6f
                        ),
                        radius = coreRadius * 1.6f,
                        center = center
                    )

                    // High velocity dual counter-rotating arcs
                    drawArc(
                        color = NeonMagenta,
                        startAngle = spinAngle * 2f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(center.x - coreRadius * 1.25f, center.y - coreRadius * 1.25f),
                        size = androidx.compose.ui.geometry.Size(coreRadius * 2.5f, coreRadius * 2.5f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = QuantumViolet,
                        startAngle = -spinAngle * 1.5f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - coreRadius * 1.45f, center.y - coreRadius * 1.45f),
                        size = androidx.compose.ui.geometry.Size(coreRadius * 2.9f, coreRadius * 2.9f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Processing core nucleus
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                QuantumViolet,
                                NeonMagenta,
                                Color(0xFF1E0638)
                            ),
                            center = center,
                            radius = coreRadius
                        ),
                        radius = coreRadius,
                        center = center
                    )
                }

                AssistantState.SPEAKING -> {
                    // Harmonic emerald and cyan transmission aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BiolumEmerald.copy(alpha = 0.45f),
                                CyberCyan.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = coreRadius * 1.55f
                        ),
                        radius = coreRadius * 1.55f,
                        center = center
                    )

                    // Harmonic orbital nodes (8 orbital light points radiating speech energy)
                    val nodeCount = 8
                    for (i in 0 until nodeCount) {
                        val angle = (spinAngle * 0.8f + (i * (360f / nodeCount))) * (Math.PI.toFloat() / 180f)
                        val dist = coreRadius * (1.25f + 0.12f * sin(wavePhase + i))
                        val nodeX = center.x + dist * cos(angle)
                        val nodeY = center.y + dist * sin(angle)
                        drawCircle(
                            color = if (i % 2 == 0) BiolumEmerald else CyberCyan,
                            radius = 3.5.dp.toPx(),
                            center = Offset(nodeX, nodeY)
                        )
                    }

                    // Speaking core nucleus
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                BiolumEmerald,
                                CyberCyan,
                                Color(0xFF00241A)
                            ),
                            center = center,
                            radius = coreRadius
                        ),
                        radius = coreRadius,
                        center = center
                    )
                }

                AssistantState.ERROR -> {
                    // Cautionary glitch aura in crimson and amber
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GlitchCrimson.copy(alpha = 0.45f),
                                WarningAmber.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = coreRadius * 1.4f
                        ),
                        radius = coreRadius * 1.4f,
                        center = center
                    )

                    // Fractured jagged arcs
                    drawArc(
                        color = GlitchCrimson,
                        startAngle = spinAngle * 0.5f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(center.x - coreRadius * 1.2f, center.y - coreRadius * 1.2f),
                        size = androidx.compose.ui.geometry.Size(coreRadius * 2.4f, coreRadius * 2.4f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = WarningAmber,
                        startAngle = spinAngle * 0.5f + 160f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(center.x - coreRadius * 1.2f, center.y - coreRadius * 1.2f),
                        size = androidx.compose.ui.geometry.Size(coreRadius * 2.4f, coreRadius * 2.4f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Error core nucleus
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                WarningAmber,
                                GlitchCrimson,
                                Color(0xFF2E0505)
                            ),
                            center = center,
                            radius = coreRadius
                        ),
                        radius = coreRadius,
                        center = center
                    )
                }
            }
        }
    }
}
