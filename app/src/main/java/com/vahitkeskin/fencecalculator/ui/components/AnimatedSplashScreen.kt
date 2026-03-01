package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSplashScreen(onAnimationFinished: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    
    val lineProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // Line drawing animation
        lineProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
        // Reveal text and scale up
        launch {
            textAlpha.animateTo(1f, tween(800))
        }
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        
        delay(1000)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Architectural/Fence Drawing with Canvas
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = 8f
                val w = size.width
                val h = size.height
                
                // Vertical Poles
                val poleCount = 4
                for (i in 0 until poleCount) {
                    val x = (w / (poleCount - 1)) * i
                    drawLine(
                        color = primaryColor.copy(alpha = 0.3f), // Ghost pole
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = primaryColor,
                        start = Offset(x, h),
                        end = Offset(x, h - (h * lineProgress.value)),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Horizontal Mesh Lines (Stylized)
                val lineCount = 5
                for (i in 0 until lineCount) {
                    val y = (h / (lineCount - 1)) * i
                    drawLine(
                        color = primaryColor.copy(alpha = 0.2f * lineProgress.value),
                        start = Offset(0f, y),
                        end = Offset(w * lineProgress.value, y),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Branding Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
            ) {
                Text(
                    text = "ÇİT HESAPLAMA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = onBackgroundColor,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "PREMIUM CALCULATOR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
