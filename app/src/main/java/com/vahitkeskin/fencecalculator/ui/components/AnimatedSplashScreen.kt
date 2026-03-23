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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class SplashVec3(val x: Float, val y: Float, val z: Float)

private fun splashProject(v: SplashVec3, w: Float, h: Float, rotX: Float, rotY: Float, s: Float): Offset {
    val radX = rotX * Math.PI / 180f
    val radY = rotY * Math.PI / 180f
    val cosX = Math.cos(radX).toFloat()
    val sinX = Math.sin(radX).toFloat()
    val cosY = Math.cos(radY).toFloat()
    val sinY = Math.sin(radY).toFloat()
    var x = v.x
    var y = v.y
    var z = v.z
    val y1 = y * cosX - z * sinX
    val z1 = y * sinX + z * cosX
    y = y1; z = z1
    val x2 = x * cosY + z * sinY
    val z2 = -x * sinY + z * cosY
    x = x2; z = z2
    val p = 1000f
    val factor = p / (p + z)
    return Offset(w / 2 + x * factor * s, h / 2 - y * factor * s)
}

@Composable
fun AnimatedSplashScreen(onAnimationFinished: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    val lineProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        lineProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
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
            Canvas(modifier = Modifier.size(170.dp)) {
                val w = size.width
                val h = size.height
                
                val vScale = 3f * scale.value
                val baseFenceH = 45f // Base v14 height
                val fenceH = baseFenceH * lineProgress.value
                
                val concMain = Color(0xFFE0E0E0)
                val concEdge = Color(0xFF78909C).copy(0.6f)
                val shC = Color.Black.copy(0.15f)

                val rotX = -20f
                val rotY = 32f

                // Draw 3 Posts
                val positions = listOf(-35f, 0f, 35f)
                positions.forEach { xPos ->
                    val base = SplashVec3(xPos, 0f, 0f)
                    val joint = SplashVec3(xPos, fenceH, 0f)
                    
                    val pB = splashProject(base, w, h, rotX, rotY, vScale)
                    val pJ = splashProject(joint, w, h, rotX, rotY, vScale)

                    // Ground Shadow
                    drawOval(
                        shC,
                        topLeft = pB - Offset(14f * vScale, 5f * vScale),
                        size = Size(28f * vScale, 10f * vScale)
                    )

                    // Post Body
                    drawLine(concEdge, pB, pJ, 8.5f * vScale, StrokeCap.Square)
                    drawLine(concMain, pB, pJ, 5.5f * vScale, StrokeCap.Square)
                }
                
                // Horizontal Mesh Wires
                if (lineProgress.value > 0.4f) {
                    val wireProgress = (lineProgress.value - 0.4f) / 0.6f
                    val wireColor = Color(0xFFB0BEC5).copy(0.4f)
                    for (i in 1..3) {
                        val yWire = i * 12f
                        val wStart = SplashVec3(-35f, yWire, 0f)
                        val wEnd = SplashVec3(35f, yWire, 0f)
                        
                        val pW1 = splashProject(wStart, w, h, rotX, rotY, vScale)
                        val pW2 = splashProject(wEnd, w, h, rotX, rotY, vScale)
                        
                        drawLine(wireColor, pW1, pW2, 1.5f * vScale * wireProgress)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val currentStrings = com.vahitkeskin.fencecalculator.util.Localization.getStrings(java.util.Locale.getDefault().language)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
            ) {
                Text(
                    text = currentStrings.splashTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = onBackgroundColor,
                    letterSpacing = 4.sp
                )
                Text(
                    text = currentStrings.premiumArchitecturalTool,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@AppPreviews
@Composable
fun AnimatedSplashScreenPreview() {
    FenceCalculatorTheme {
        AnimatedSplashScreen(onAnimationFinished = {})
    }
}
