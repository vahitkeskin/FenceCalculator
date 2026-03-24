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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class SplashVec3(val x: Float, val y: Float, val z: Float)

private fun splashProject(v: SplashVec3, w: Float, h: Float, rotX: Float, rotY: Float, s: Float): Offset {
    val radX = rotX * PI.toFloat() / 180f
    val radY = rotY * PI.toFloat() / 180f
    val cosX = cos(radX)
    val sinX = sin(radX)
    val cosY = cos(radY)
    val sinY = sin(radY)
    
    val x = v.x
    var y = v.y
    var z = v.z
    
    val y1 = y * cosX - z * sinX
    val z1 = y * sinX + z * cosX
    y = y1; z = z1
    
    val x2 = x * cosY + z * sinY
    val z2 = -x * sinY + z * cosY
    val finalX = x2; val finalZ = z2
    
    val p = 1000f
    val factor = p / (p + finalZ)
    return Offset(w / 2f + finalX * factor * s, h / 2f - y * factor * s)
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
            Canvas(modifier = Modifier.size(220.dp)) {
                val w = size.width
                val h = size.height
                
                val vScale = 3.5f * scale.value
                val baseFenceH = 38f
                val fenceH = baseFenceH * lineProgress.value
                val tipH = 7f * lineProgress.value
                val tipOut = 5f * lineProgress.value
                
                val concMain = Color(0xFFE0E0E0)
                val concEdge = Color(0xFF78909C).copy(0.6f)
                val wireColor = Color(0xFFB0BEC5).copy(0.35f)
                val shC = Color.Black.copy(0.1f)

                val rotX = -16f
                val rotY = 28f

                // 1. POSTS (5 positions)
                val xPosList = listOf(-60f, -30f, 0f, 30f, 60f)
                val pJoints = mutableListOf<Offset>()
                val pTips = mutableListOf<Offset>()

                xPosList.forEach { xPos ->
                    val base = SplashVec3(xPos, 0f, 0f)
                    val joint = SplashVec3(xPos, fenceH, 0f)
                    val tip = SplashVec3(xPos + tipOut, fenceH + tipH, 0f)
                    
                    val pB = splashProject(base, w, h, rotX, rotY, vScale)
                    val pJ = splashProject(joint, w, h, rotX, rotY, vScale)
                    val pT = splashProject(tip, w, h, rotX, rotY, vScale)
                    
                    pJoints.add(pJ); pTips.add(pT)

                    // Ground Shadow
                    drawOval(
                        shC,
                        topLeft = pB - Offset(14f * vScale, 4f * vScale),
                        size = Size(28f * vScale, 8f * vScale)
                    )

                    // Post Body
                    drawLine(concEdge, pB, pJ, 6.5f * vScale, StrokeCap.Square)
                    drawLine(concMain, pB, pJ, 4f * vScale, StrokeCap.Square)
                    
                    // Post Tip (Angled)
                    if (lineProgress.value > 0.3f) {
                        drawLine(concEdge, pJ, pT, 4.5f * vScale, StrokeCap.Round)
                        drawLine(concMain, pJ, pT, 2.8f * vScale, StrokeCap.Round)
                    }
                }
                
                // 2. ULTRA-DIAMOND MESH (Small Baklava Dilimleri)
                if (lineProgress.value > 0.4f) {
                    val mAlpha = (lineProgress.value - 0.4f) / 0.6f
                    val eyeX = 3.6f // Much denser horizontal
                    val eyeY = 2.4f // Much denser vertical
                    
                    for (i in 0 until xPosList.size - 1) {
                        val sX = xPosList[i]
                        val eX = xPosList[i+1]
                        val segmentWidth = eX - sX
                        
                        val mD = (segmentWidth / eyeX).toInt().coerceAtLeast(10)
                        val hD = (baseFenceH / eyeY).toInt().coerceAtLeast(12)
                        
                        for (m in -hD..mD + hD) {
                            val r1 = m.toFloat() / mD.toFloat()
                            val r2 = (m + hD).toFloat() / mD.toFloat()
                            
                            val r1C = r1.coerceIn(0f, 1f)
                            val r2C = r2.coerceIn(0f, 1f)
                            
                            // Line 1: Forward crossing
                            val p1Ref = splashProject(SplashVec3(sX + segmentWidth * r1C, r1C * fenceH, 0f), w, h, rotX, rotY, vScale)
                            val p2Ref = splashProject(SplashVec3(sX + segmentWidth * r2C, r2C * fenceH, 0f), w, h, rotX, rotY, vScale)
                            
                            if (r1 in -0.05f..1.05f || r2 in -0.05f..1.05f) {
                                drawLine(wireColor.copy(alpha = 0.25f * mAlpha), p1Ref, p2Ref, 0.45f * vScale)
                            }
                            
                            // Line 2: Backward crossing
                            val p3Ref = splashProject(SplashVec3(sX + segmentWidth * r2C, r1C * fenceH, 0f), w, h, rotX, rotY, vScale)
                            val p4Ref = splashProject(SplashVec3(sX + segmentWidth * r1C, r2C * fenceH, 0f), w, h, rotX, rotY, vScale)
                            
                            if (r1 in -0.05f..1.05f || r2 in -0.05f..1.05f) {
                                drawLine(wireColor.copy(alpha = 0.25f * mAlpha), p3Ref, p4Ref, 0.45f * vScale)
                            }
                        }
                    }
                }

                // 3. CLEAN PARALLEL TOP STRANDS (Between Tips)
                if (lineProgress.value > 0.6f) {
                    val bAlpha = (lineProgress.value - 0.6f) / 0.4f
                    for (i in 0 until pTips.size - 1) {
                        val t1 = pTips[i]; val t2 = pTips[i+1]
                        val j1 = pJoints[i]; val j2 = pJoints[i+1]
                        
                        for (s in 1..3) {
                            val ratio = s / 3f
                            val startS = j1 + (t1 - j1) * ratio
                            val endS = j2 + (t2 - j2) * ratio
                            
                            // Only draw the clean parallel wires, no barbs
                            drawLine(wireColor.copy(alpha = 0.7f * bAlpha), startS, endS, 1.0f * vScale)
                        }
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
