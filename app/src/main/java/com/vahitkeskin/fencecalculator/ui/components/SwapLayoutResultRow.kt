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

    // Scroll İsteği İçin Gerekli Değişkenler
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(item.color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(item.icon, null, tint = item.color, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                            Text("${df.format(item.quantity)} ${item.unit}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Alanı
                    OutlinedTextField(
                        value = currentPriceInput,
                        onValueChange = onPriceChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            // Inputa tıklandığında (Focus olduğunda) ekrana getir
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        label = { Text("Birim Fiyat (₺)", style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text("0", color = Color.LightGray) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = item.color,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            Surface(color = item.color.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TOPLAM", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = item.color.copy(alpha = 0.8f))
                    Text("${currencyFormat.format(item.totalCost)} ₺", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = item.color)
                }
            }
        }
    }
}