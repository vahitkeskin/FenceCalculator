package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwapLayoutResultRow(
    item: CalculationItem,
    currentPriceInput: String,
    onPriceChange: (String) -> Unit
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
                    Icon(item.icon, null, tint = item.color, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = onSurfaceColor)
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = onSurfaceColor.copy(alpha = 0.6f))
                        }
                        Surface(color = onSurfaceColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), tonalElevation = 0.dp) {
                            Text("${df.format(item.quantity)} ${item.unit}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
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
                            unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.1f)
                        )
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().background(onSurfaceColor.copy(alpha = 0.05f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TOPLAM", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = onSurfaceColor.copy(alpha = 0.4f), letterSpacing = 1.sp)
                    Text("${currencyFormat.format(item.totalCost)} ₺", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = item.color)
                }
            }
        }
    }
}