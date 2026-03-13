package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme

val presetColors = listOf(
    "#F44336", // Red
    "#E91E63", // Pink
    "#9C27B0", // Purple
    "#673AB7", // Deep Purple
    "#3F51B5", // Indigo
    "#2196F3", // Blue
    "#03A9F4", // Light Blue
    "#00BCD4", // Cyan
    "#009688", // Teal
    "#4CAF50", // Green
    "#8BC34A", // Light Green
    "#CDDC39", // Lime
    "#FFC107", // Amber
    "#FF9800", // Orange
    "#FF5722", // Deep Orange
    "#795548", // Brown
    "#607D8B", // Blue Grey
    "#455A64", // Dark Blue Grey
)

@Composable
fun ColorPickerCircle(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    strings: com.vahitkeskin.fencecalculator.util.AppStrings? = null,
    modifier: Modifier = Modifier
) {
    val currentStrings = strings ?: com.vahitkeskin.fencecalculator.util.Localization.getStrings(java.util.Locale.getDefault().language)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        Text(
            text = currentStrings.cardColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor.copy(alpha = 0.5f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(presetColors) { colorHex ->
                val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)
                val color = try {
                    Color(android.graphics.Color.parseColor(colorHex))
                } catch (e: Exception) {
                    Color.Gray
                }

                val animatedBorderColor by animateColorAsState(
                    targetValue = if (isSelected) color else Color.Transparent,
                    animationSpec = tween(300),
                    label = "border"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) Modifier.border(
                                width = 3.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        animatedBorderColor.copy(alpha = 1f),
                                        animatedBorderColor.copy(alpha = 0.4f)
                                    )
                                ),
                                shape = CircleShape
                            ) else Modifier.border(
                                width = 1.dp,
                                color = onSurfaceColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                        )
                        .clickable { onColorSelected(colorHex) }
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = currentStrings.selected,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@AppPreviews
@Composable
fun ColorPickerCirclePreview() {
    FenceCalculatorTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ColorPickerCircle(
                selectedColorHex = "#2196F3",
                onColorSelected = {}
            )
        }
    }
}
