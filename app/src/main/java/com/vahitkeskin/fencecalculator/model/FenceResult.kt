package com.vahitkeskin.fencecalculator.model

/**
 * Data class representing the results of a fence calculation.
 */
data class FenceResult(
    val totalLandLength: Float,
    val postCount: Int,
    val strutCount: Int,
    val height: Float,
    val spacing: Float,
    val meshEye: Float
)
