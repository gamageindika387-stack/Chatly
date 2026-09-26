package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PulseEmeraldGreen
import com.example.ui.theme.PulseGradient

@Composable
fun PulseAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    avatarUrl: String = "",
    isOnline: Boolean = false,
    hasStory: Boolean = false,
    isStoryViewed: Boolean = false
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "P" }

    // Consistent color based on name hash
    val colorIndex = (name.hashCode() and 0x7FFFFFFF) % avatarColors.size
    val bgGradient = avatarColors[colorIndex]

    val storyBorderModifier = if (hasStory) {
        val borderBrush = if (isStoryViewed) {
            Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFF64748B)))
        } else {
            PulseGradient
        }
        Modifier.border(2.5.dp, borderBrush, CircleShape).padding(2.5.dp)
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .then(storyBorderModifier)
            .testTag("avatar_$name"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(bgGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.3f).coerceAtLeast(10f).dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(PulseEmeraldGreen)
            )
        }
    }
}

private val avatarColors = listOf(
    Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
    Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))),
    Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E))),
    Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
    Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
    Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)))
)
