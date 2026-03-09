package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme

@Composable
fun BalloonTooltip(
    text: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, -10),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(0.5f, 1f)),
            exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(0.5f, 1f))
        ) {
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 220.dp)
                    .clickable { onDismiss() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                // Arrow
                Box(
                    modifier = Modifier
                        .offset(y = (-1).dp)
                        .size(16.dp, 10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = GenericShape { size, _ ->
                                moveTo(0f, 0f)
                                lineTo(size.width, 0f)
                                lineTo(size.width / 2f, size.height)
                                close()
                            }
                        )
                )
            }
        }
    }
}

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
    tooltipText: String? = null,
    showTooltip: Boolean = false,
    onTooltipDismiss: (() -> Unit)? = null,
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

        if (showTooltip && tooltipText != null && onTooltipDismiss != null) {
            BalloonTooltip(
                text = tooltipText,
                onDismiss = onTooltipDismiss
            )
        }

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
                            Box {
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
}

@AppPreviews
@Composable
fun VerticalPickerPreview() {
    val items = listOf("Seçenek 1", "Seçenek 2", "Seçenek 3", "Seçenek 4", "Seçenek 5")
    var selectedItem by remember { mutableStateOf(items[2]) }
    
    FenceCalculatorTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            VerticalPicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                visibleItemsCount = 3
            )
        }
    }
}
