package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
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
