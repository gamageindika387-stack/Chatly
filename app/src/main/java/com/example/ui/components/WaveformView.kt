package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PulseCyanAccent
import com.example.ui.theme.PulseVioletPrimary

@Composable
fun WaveformPlayer(
    isPlaying: Boolean,
    progress: Float,
    durationSec: Int,
    waveform: List<Float>,
    speed: Float = 1.0f,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleSpeed: () -> Unit,
    isSentByMe: Boolean = false,
    modifier: Modifier = Modifier
) {
    val defaultPoints = if (waveform.isEmpty()) {
        listOf(0.3f, 0.5f, 0.8f, 0.4f, 0.9f, 0.7f, 0.5f, 0.8f, 0.6f, 0.3f, 0.7f, 0.9f, 0.4f, 0.6f)
    } else waveform

    val activeColor = if (isSentByMe) Color.White else PulseVioletPrimary
    val inactiveColor = if (isSentByMe) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val playBtnBg = if (isSentByMe) Color.White.copy(alpha = 0.2f) else PulseVioletPrimary.copy(alpha = 0.15f)
    val playIconColor = if (isSentByMe) Color.White else PulseVioletPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause Button
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(playBtnBg)
                .testTag("waveform_play_pause_button")
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = playIconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waveform Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(ratio)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount = defaultPoints.size
                val spacing = 3.dp.toPx()
                val totalSpacing = spacing * (barCount - 1)
                val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

                val activeBoundaryX = size.width * progress

                defaultPoints.forEachIndexed { index, amp ->
                    val x = index * (barWidth + spacing)
                    val barHeight = (size.height * amp.coerceIn(0.2f, 1.0f)).coerceAtLeast(4.dp.toPx())
                    val top = (size.height - barHeight) / 2f
                    val color = if (x <= activeBoundaryX) activeColor else inactiveColor

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Duration or speed
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${durationSec}s",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSentByMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Speed multiplier pill
            Surface(
                onClick = onToggleSpeed,
                shape = RoundedCornerShape(8.dp),
                color = if (isSentByMe) Color.White.copy(alpha = 0.2f) else PulseVioletPrimary.copy(alpha = 0.1f),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "${speed}x",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSentByMe) Color.White else PulseVioletPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
