package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun <T> VerticalPicker(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Int = 40,
    visibleItemsCount: Int = 3,
    isLocked: Boolean = false,
    onLockToggle: (() -> Unit)? = null,
    label: (T) -> String = { it.toString() }
) {
    val density = LocalDensity.current
    val itemHeightDp = with(density) { itemHeight.dp }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Find initial index
    val initialIndex = items.indexOf(selectedItem).coerceAtLeast(0)
    
    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex)
    }

    // Reaction to scroll
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) initialIndex
            else {
                val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2) - center) }?.index ?: initialIndex
            }
        }
    }

    LaunchedEffect(centerIndex) {
        if (centerIndex in items.indices) {
            onItemSelected(items[centerIndex])
        }
    }

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .height(itemHeightDp * visibleItemsCount)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Selection Highlight
        PremiumGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp),
            cornerRadius = 12.dp
        ) {}

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeightDp * (visibleItemsCount / 2)),
            verticalArrangement = Arrangement.Center,
            userScrollEnabled = !isLocked
        ) {
            items(items.size) { index ->
                val isSelected = index == centerIndex
                Box(
                    modifier = Modifier
                        .height(itemHeightDp)
                        .fillMaxWidth()
                        .then(if (isSelected && onLockToggle != null) Modifier.clickable { onLockToggle.invoke() } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = label(items[index]),
                            fontSize = if (isSelected) 18.sp else 14.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else onBackgroundColor,
                            modifier = Modifier.alpha(if (isSelected) 1f else 0.4f),
                            letterSpacing = if (isSelected) 1.sp else 0.sp
                        )
                        if (isSelected && onLockToggle != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock Toggle",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
