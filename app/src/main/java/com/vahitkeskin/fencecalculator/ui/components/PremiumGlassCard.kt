package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Light mode: clean white card with subtle blue-white tint
    // Dark mode: original glassmorphism style
    val backgroundBrush = if (isDark) {
        val onSurface = MaterialTheme.colorScheme.onSurface
        Brush.verticalGradient(
            colors = listOf(
                onSurface.copy(alpha = 0.1f),
                onSurface.copy(alpha = 0.05f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.97f),
                Color(0xFFF0F4FF).copy(alpha = 0.92f)  // Subtle blue-white tint
            )
        )
    }

    val borderBrush = if (isDark) {
        val onSurface = MaterialTheme.colorScheme.onSurface
        Brush.verticalGradient(
            colors = listOf(
                onSurface.copy(alpha = 0.2f),
                Color.Transparent,
                onSurface.copy(alpha = 0.1f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE2E8F0).copy(alpha = 0.6f),  // Soft slate border
                Color(0xFFEDF2F7).copy(alpha = 0.3f),
                Color(0xFFE2E8F0).copy(alpha = 0.4f)
            )
        )
    }

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = Color(0xFF94A3B8).copy(alpha = 0.12f),
                        spotColor = Color(0xFF94A3B8).copy(alpha = 0.08f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = if (isDark) 1.dp else 0.5.dp,
                brush = borderBrush,
                shape = shape
            )
    ) {
        content()
    }
}

// Extension to approximate luminance
private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
