package com.vahitkeskin.fencecalculator.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import com.vahitkeskin.fencecalculator.data.model.CustomCardItem
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.ceil

fun CalculatorViewModel.addOrUpdateCustomCardExt(card: CustomCardItem) {
    val current = customCards.toMutableList()
    val index = current.indexOfFirst { it.id == card.id }
    if (index >= 0) {
        current[index] = card
    } else {
        current.add(card)
    }
    customCards = current
    updateCustomCardResultsExt()
    viewModelScope.launch {
        dataStoreManager.saveCustomCards(Json.encodeToString(current))
    }
}

fun CalculatorViewModel.deleteCustomCardExt(id: String) {
    val current = customCards.toMutableList()
    current.removeAll { it.id == id }
    customCards = current
    updateCustomCardResultsExt()
    viewModelScope.launch {
        dataStoreManager.saveCustomCards(Json.encodeToString(current))
    }
}

fun CalculatorViewModel.getCustomCardByIdExt(id: String): CustomCardItem? {
    return customCards.find { it.id == id }
}

fun CalculatorViewModel.updateCustomCardResultsExt() {
    val allStaticResults = results // Varsayılan kart sonuçları

    customCardResults = customCards.map { card ->
        val color = try {
            Color(android.graphics.Color.parseColor(card.colorHex))
        } catch (e: Exception) {
            Color(0xFF607D8B)
        }

        // Bağımlılık hesabı
        val calculationResult =
            if (card.dependentCardId != null && card.dependentRatio != null) {
                // Önce varsayılan ürünlerde ara (id as is)
                val baseItem = if (card.dependentCardId == "v_total_length") {
                    CalculationItem(
                        id = "v_total_length",
                        title = strings.pdfTotalLengthLabel.removeSuffix(":"),
                        description = "",
                        quantity = totalLengthInput.toDoubleOrNull() ?: 0.0,
                        unit = strings.unitMeter,
                        unitPrice = 0.0,
                        totalCost = 0.0,
                        icon = Icons.Filled.Straighten,
                        color = Color.Transparent
                    )
                } else {
                    allStaticResults.find { it.id == card.dependentCardId }
                }
                // Eğer orada yoksa özel kartlarda ara
                    ?: customCards.find { it.id == card.dependentCardId }?.let { depCard ->
                        CalculationItem(
                            id = depCard.id,
                            title = depCard.title,
                            description = "",
                            quantity = depCard.quantity, // Note: This might need recursive update if custom cards depend on each other
                            unit = depCard.unit,
                            unitPrice = depCard.unitPrice,
                            totalCost = 0.0,
                            icon = Icons.Filled.Block,
                            color = Color.Transparent
                        )
                    }

                val baseQty = baseItem?.quantity ?: 0.0
                val ratio = card.dependentRatio

                val result = when (card.dependentOperation) {
                    "+" -> baseQty + ratio
                    "-" -> baseQty - ratio
                    "÷", "/" -> if (ratio != 0.0) baseQty / ratio else baseQty
                    else -> baseQty * ratio // Varsayılan çarpma "*"
                }

                val depInfo = if (baseItem != null) {
                    "${baseItem.title} (${baseQty.toInt()} ${baseItem.unit}) ${card.dependentOperation} $ratio"
                } else null

                ceil(kotlin.math.max(0.0, result)) to depInfo
            } else {
                card.quantity to null
            }

        val (finalQty, depInfo) = calculationResult
        val totalCost = finalQty * card.unitPrice
        CalculationItem(
            id = "custom_${card.id}",
            title = card.title,
            description = card.description,
            quantity = finalQty,
            unit = card.unit,
            unitPrice = card.unitPrice,
            totalCost = totalCost,
            icon = Icons.Default.Extension,
            color = color,
            emoji = card.emoji,
            dependencyInfo = depInfo
        )
    }
    rebuildOrderedVisibleItemsExt()
}
