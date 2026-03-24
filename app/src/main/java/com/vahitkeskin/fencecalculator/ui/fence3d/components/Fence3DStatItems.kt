package com.vahitkeskin.fencecalculator.ui.fence3d.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// İstatistik öğelerini (ikon, değer, etiket) gösteren bileşen
@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    contentColor: Color = if (MaterialTheme.colorScheme.onBackground.luminance() > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface,
    secondaryColor: Color = if (MaterialTheme.colorScheme.onBackground.luminance() > 0.5f) Color.White.copy(
        0.5f
    ) else MaterialTheme.colorScheme.onSurface.copy(0.65f)
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = secondaryColor.copy(0.8f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = secondaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Görünüm istatistik öğelerini (X, Y, Z değerleri) gösteren bileşen
@Composable
fun ViewStatItem(
    icon: @Composable (() -> Unit)? = null,
    value: String,
    contentColor: Color = if (MaterialTheme.colorScheme.onBackground.luminance() > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                icon()
            }
            Spacer(Modifier.width(10.dp))
        }
        Text(value, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(0.8f))
    }
}

// Görünüm istatistikleri için renkli ikon kutusu bileşeni
@Composable
fun ViewStatIcon(imageVector: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tint.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.padding(3.dp)
        )
    }
}
