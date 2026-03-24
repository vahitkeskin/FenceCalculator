package com.vahitkeskin.fencecalculator.ui.fence3d.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.model.FenceResult
import com.vahitkeskin.fencecalculator.ui.icons.*

// Glassmorphic tasarım ile proje özet bilgilerini ve kontrol butonlarını gösteren katman
@Composable
fun Fence3DOverlay(
    fenceResult: FenceResult,
    curX: Float,
    curY: Float,
    curS: Float,
    onReset: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onBackground.luminance() > 0.5f
    val glassBaseColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f)
    val glassContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val glassContentSecondaryColor = if (isDark) Color.White.copy(0.5f) else MaterialTheme.colorScheme.onSurface.copy(0.75f)
    val glassBorderColor1 = if (isDark) Color.White.copy(0.25f) else Color.White
    val glassBorderColor2 = if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.25f)
    val glassGradientColor1 = if (isDark) Color.White.copy(0.12f) else Color.White.copy(0.5f)
    val glassGradientColor2 = if (isDark) Color.White.copy(0.02f) else Color.White.copy(0.1f)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)) {
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.clip(RoundedCornerShape(28.dp))) {
            Surface(
                color = glassBaseColor,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = if (isDark) 0.dp else 24.dp,
                border = BorderStroke(
                    if (isDark) 1.dp else 2.dp,
                    Brush.linearGradient(listOf(glassBorderColor1, glassBorderColor2))
                ),
                modifier = Modifier
                    .matchParentSize()
                    .blur(if (android.os.Build.VERSION.SDK_INT >= 31) 25.dp else 0.dp)
            ) {}

            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(glassGradientColor1, Color.Transparent, glassGradientColor2)
                        ),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(0.25f),
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(6.dp).size(14.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "PROJE ÖZETİ",
                                style = MaterialTheme.typography.labelLarge,
                                color = glassContentColor,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                StatItem(Icons.Default.Square, "${fenceResult.postCount}", "Direk", glassContentColor, glassContentSecondaryColor)
                                StatItem(Icons.Default.Straighten, "${fenceResult.spacing}m", "Aralık", glassContentColor, glassContentSecondaryColor)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                StatItem(Icons.Default.Height, "${fenceResult.height}m", "Boy", glassContentColor, glassContentSecondaryColor)
                                StatItem(Icons.Default.Grid4x4, "${fenceResult.meshEye}cm", "Göz", glassContentColor, glassContentSecondaryColor)
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        ViewStatItem({ ViewStatIcon(Icons.Filled.RotationX, Color.Red.copy(0.8f)) }, "${curX.toInt()}°", glassContentColor)
                        ViewStatItem({ ViewStatIcon(Icons.Filled.RotationY, Color.Green.copy(0.8f)) }, "${curY.toInt()}°", glassContentColor)
                        ViewStatItem({ ViewStatIcon(Icons.Filled.ScaleZ, Color.Cyan.copy(0.8f)) }, "%.1fX".format(curS), glassContentColor)
                    }

                    val infiniteT = rememberInfiniteTransition()
                    val icS by infiniteT.animateFloat(
                        0.92f,
                        1.08f,
                        infiniteRepeatable(tween(1200), RepeatMode.Reverse)
                    )
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { onReset() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sıfırla",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(28.dp)
                                    .graphicsLayer(scaleX = icS, scaleY = icS)
                            )
                        }
                    }
                }
            }
        }
    }
}
