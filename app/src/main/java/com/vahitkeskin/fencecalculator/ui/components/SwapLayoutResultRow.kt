package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwapLayoutResultRow(
    item: CalculationItem,
    currentPriceInput: String,
    onPriceChange: (String) -> Unit,
    onPinToggle: () -> Unit = {}
) {
    val df = DecimalFormat("#,##0.##")
    val currencyFormat = DecimalFormat("#,##0.00")
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Scroll İsteği İçin Gerekli Değişkenler
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    PremiumGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(item.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    if (item.emoji != null) {
                        Text(text = item.emoji, fontSize = 24.sp)
                    } else {
                        Icon(item.icon, null, tint = item.color, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(0.6f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = onSurfaceColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (item.dependencyInfo != null) {
                                Text(
                                    text = item.dependencyInfo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = item.color.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.wrapContentSize(),
                                color = onSurfaceColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                tonalElevation = 0.dp
                            ) {
                                Text(
                                    text = "${df.format(item.quantity)} ${item.unit}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onPinToggle,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isPinned) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = "Pin",
                                    tint = if (item.isPinned) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Alanı
                    OutlinedTextField(
                        value = currentPriceInput,
                        onValueChange = onPriceChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        label = { Text("Birim Fiyat (₺)", style = MaterialTheme.typography.bodySmall, color = onSurfaceColor.copy(alpha = 0.5f)) },
                        placeholder = { Text("0", color = onSurfaceColor.copy(alpha = 0.2f)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = onSurfaceColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                            focusedBorderColor = item.color.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().background(item.color.copy(alpha = 0.04f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TOPLAM", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor.copy(alpha = 0.4f), letterSpacing = 1.sp)
                    Text("${currencyFormat.format(item.totalCost)} ₺", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = item.color)
                }
            }
        }
    }
}

@AppPreviews
@Composable
fun SwapLayoutResultRowPreview() {
    FenceCalculatorTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SwapLayoutResultRow(
                item = CalculationItem(
                    id = "preview",
                    title = "Tel Örgü (1.5m)",
                    description = "Arazinin çevresi için gerekli tel örgü",
                    quantity = 150.0,
                    unit = "m",
                    unitPrice = 100.0,
                    totalCost = 15000.0,
                    color = Color(0xFF3B82F6),
                    icon = Icons.Default.CheckCircle,
                    emoji = "🕸️",
                    dependencyInfo = "Rulo Uzunluğu: 20m"
                ),
                currentPriceInput = "100",
                onPriceChange = {}
            )
        }
    }
}