package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.sin

@Composable
fun AnimatedWaveBottomBar(
    totalCost: Double,
    modifier: Modifier = Modifier
) {
    val animatedTotalCost by animateFloatAsState(targetValue = totalCost.toFloat(), animationSpec = tween(800, easing = FastOutSlowInEasing), label = "cost")
    val currencyFormat = DecimalFormat("#,##0.00")
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 2f * Math.PI.toFloat(), animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), label = "phase")

    val backgroundColor = Color(0xFF1E1E1E)
    val waveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width; val h = size.height; val amp = 15.dp.toPx()
            val path = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.5f)
                for(x in 0..w.toInt() step 10) {
                    lineTo(x.toFloat(), (h * 0.6f) + amp * sin((x / w) * (2 * Math.PI) * 1f + wavePhase).toFloat())
                }
                lineTo(w, h)
                close()
            }
            drawPath(path = path, brush = Brush.verticalGradient(listOf(waveColor, Color.Transparent), h * 0.4f, h), style = Fill)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("TOPLAM MALİYET", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f), letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text("Hesaplanan Tutar", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp), shadowElevation = 8.dp) {
                Text("${currencyFormat.format(animatedTotalCost)} ₺", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            }
        }
    }
}