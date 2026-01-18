package com.vahitkeskin.fencecalculator.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CalculationItem(
    val id: String,
    val title: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val totalCost: Double,
    val icon: ImageVector,
    val color: Color
)