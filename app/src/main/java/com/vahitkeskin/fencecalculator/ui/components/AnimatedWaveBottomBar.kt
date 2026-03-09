package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import java.text.DecimalFormat

@Composable
fun AnimatedWaveBottomBar(
    totalCost: Double,
    modifier: Modifier = Modifier
) {
    val currencyFormat = DecimalFormat("#,##0.00")
    val backgroundColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor = onSurfaceColor.copy(alpha = 0.15f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box {
            // Rounded top border line
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp) // Height to cover the corner radius
                    .align(Alignment.TopCenter)
            ) {
                val radius = 24.dp.toPx()
                val strokeWidth = 1.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, radius)
                    quadraticTo(0f, 0f, radius, 0f)
                    lineTo(size.width - radius, 0f)
                    quadraticTo(size.width, 0f, size.width, radius)
                }
                
                drawPath(
                    path = path,
                    color = borderColor,
                    style = Stroke(width = strokeWidth)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "TOPLAM MALİYET",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceColor.copy(0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Hesaplanan Tutar",
                            style = MaterialTheme.typography.titleMedium,
                            color = onSurfaceColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        color = primaryColor,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp
                    ) {
                        Text(
                            "${currencyFormat.format(totalCost)} ₺",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@AppPreviews
@Composable
fun AnimatedWaveBottomBarPreview() {
    FenceCalculatorTheme {
        AnimatedWaveBottomBar(totalCost = 15450.0)
    }
}
