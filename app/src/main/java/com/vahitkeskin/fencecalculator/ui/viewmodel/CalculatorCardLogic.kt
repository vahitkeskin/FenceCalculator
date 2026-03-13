package com.vahitkeskin.fencecalculator.ui.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

fun CalculatorViewModel.rebuildOrderedVisibleItemsExt() {
    val allItems = results + customCardResults
    val processedItems = allItems.map { item ->
        item.copy(isPinned = item.id in pinnedCardIds)
    }
    val visibleItems = processedItems.filter { it.id !in hiddenCardIds }

    orderedVisibleItems = if (cardOrder.isNotEmpty()) {
        val orderMap = cardOrder.withIndex().associate { (i, id) -> id to i }
        val ordered = visibleItems.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
        ordered
    } else {
        visibleItems
    }
    // Recalculate grand total from visible items
    grandTotalCost = orderedVisibleItems.sumOf { it.totalCost }
}

fun CalculatorViewModel.hideCardExt(id: String) {
    hiddenCardIds = hiddenCardIds + id
    // Özel kartsa tamamen sil
    if (id.startsWith("custom_")) {
        deleteCustomCard(id.removePrefix("custom_"))
    }
    saveHiddenCardsExt()
    rebuildOrderedVisibleItemsExt()
}

fun CalculatorViewModel.togglePinExt(id: String) {
    pinnedCardIds = if (id in pinnedCardIds) {
        pinnedCardIds - id
    } else {
        pinnedCardIds + id
    }
    savePinnedCardsExt()
    rebuildOrderedVisibleItemsExt()
}

fun CalculatorViewModel.restoreDefaultCardsExt() {
    hiddenCardIds = emptySet()
    cardOrder = emptyList()
    saveHiddenCardsExt()
    saveCardOrderExt()
    rebuildOrderedVisibleItemsExt()
}

fun CalculatorViewModel.moveCardUpExt(id: String) {
    val currentList = orderedVisibleItems.map { it.id }.toMutableList()
    val idx = currentList.indexOf(id)
    if (idx > 0) {
        currentList[idx] = currentList[idx - 1].also { currentList[idx - 1] = currentList[idx] }
        cardOrder = currentList
        saveCardOrderExt()
        rebuildOrderedVisibleItemsExt()
    }
}

fun CalculatorViewModel.moveCardDownExt(id: String) {
    val currentList = orderedVisibleItems.map { it.id }.toMutableList()
    val idx = currentList.indexOf(id)
    if (idx >= 0 && idx < currentList.size - 1) {
        currentList[idx] = currentList[idx + 1].also { currentList[idx + 1] = currentList[idx] }
        cardOrder = currentList
        saveCardOrderExt()
        rebuildOrderedVisibleItemsExt()
    }
}

fun CalculatorViewModel.saveHiddenCardsExt() {
    viewModelScope.launch {
        dataStoreManager.saveHiddenCards(hiddenCardIds.joinToString(","))
    }
}

fun CalculatorViewModel.saveCardOrderExt() {
    viewModelScope.launch {
        dataStoreManager.saveCardOrder(cardOrder.joinToString(","))
    }
}

fun CalculatorViewModel.savePinnedCardsExt() {
    viewModelScope.launch {
        dataStoreManager.savePinnedCards(pinnedCardIds.joinToString(","))
    }
}
