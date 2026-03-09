package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.sin
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme

@Composable
fun MeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val colorScheme = MaterialTheme.colorScheme
    val isDark = !(colorScheme.background.let { (it.red + it.green + it.blue) / 3 > 0.5f })
    val backgroundColor = colorScheme.background
    val blobAlpha = if (isDark) 0.15f else 0.08f
    
    val primaryColor = colorScheme.primary.copy(alpha = blobAlpha)
    val secondaryColor = colorScheme.secondary.copy(alpha = blobAlpha)
    val tertiaryColor = colorScheme.tertiary.copy(alpha = blobAlpha)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background color
        drawRect(color = backgroundColor)

        // Animated blobs
        val blobs = listOf(
            BlobData(
                color = primaryColor,
                centerX = 0.2f, centerY = 0.3f, 
                radiusX = 0.4f, radiusY = 0.5f,
                speedX = 0.05f, speedY = 0.08f
            ),
            BlobData(
                color = secondaryColor,
                centerX = 0.8f, centerY = 0.7f, 
                radiusX = 0.5f, radiusY = 0.4f,
                speedX = -0.06f, speedY = -0.04f
            ),
            BlobData(
                color = tertiaryColor,
                centerX = 0.5f, centerY = 0.5f, 
                radiusX = 0.6f, radiusY = 0.6f,
                speedX = 0.03f, speedY = -0.07f
            )
        )

        blobs.forEach { blob ->
            val offsetX = sin(time * 2 * Math.PI.toFloat() * blob.speedX) * width * 0.1f
            val offsetY = sin(time * 2 * Math.PI.toFloat() * blob.speedY) * height * 0.1f
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blob.color, Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(
                        width * blob.centerX + offsetX,
                        height * blob.centerY + offsetY
                    ),
                    radius = width * blob.radiusX
                ),
                center = androidx.compose.ui.geometry.Offset(
                    width * blob.centerX + offsetX,
                    height * blob.centerY + offsetY
                ),
                radius = width * blob.radiusX
            )
        }
    }
}

private data class BlobData(
    val color: Color,
    val centerX: Float,
    val centerY: Float,
    val radiusX: Float,
    val radiusY: Float,
    val speedX: Float,
    val speedY: Float
)

@AppPreviews
@Composable
fun MeshBackgroundPreview() {
    FenceCalculatorTheme {
        MeshBackground()
    }
}
