package com.vahitkeskin.fencecalculator.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomCardItem(
    val id: String,
    val title: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val colorHex: String // "#FF9800" gibi hex renk kodu
)
