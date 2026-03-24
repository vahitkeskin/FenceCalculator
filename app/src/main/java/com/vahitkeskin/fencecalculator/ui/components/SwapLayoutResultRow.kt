package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.util.centerOnFocus
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwapLayoutResultRow(
    viewModel: CalculatorViewModel,
    item: CalculationItem,
    currentPriceInput: String,
    onPriceChange: (String) -> Unit,
    onPinToggle: () -> Unit = {},
    onPremiumClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    // TODO: İstediğim zaman aktif edebileyim - 50 sınırlaması ve premium
    // val isBlurred = !viewModel.isPremium && viewModel.usageCount >= 50
    val isBlurred = false
    val df = DecimalFormat("#,##0.##")
    val currencyFormat = DecimalFormat("#,##0.00")
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Scroll İsteği İçin Gerekli Değişkenler
    // val bringIntoViewRequester = remember { BringIntoViewRequester() }
    // val coroutineScope = rememberCoroutineScope()

    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isBlurred) onPremiumClick()
                else onClick()
            }
    ) {
        if (item.description.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 16.dp)
                    .background(item.color.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = item.color.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceColor.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center
            ) {
                if (item.emoji != null) {
                    Text(text = item.emoji, fontSize = 24.sp)
                } else {
                    Icon(item.icon, null, tint = item.color, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.6f)) {
                        // Title Only
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = onSurfaceColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .wrapContentSize()
                                .then(if (isBlurred) Modifier.blur(8.dp) else Modifier),
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = viewModel.strings.pin,
                                modifier = Modifier.size(20.dp),
                                tint = if (item.isPinned) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    if (isSystemInDarkTheme()) Color(0xFFCCCCCC) else Color(
                                        0xFF666666
                                    )
                                }
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
                        .centerOnFocus()
                        .then(if (isBlurred) Modifier.blur(8.dp) else Modifier),
                    readOnly = isBlurred,
                    label = {
                        Text(
                            viewModel.strings.unitPriceTl,
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceColor.copy(alpha = 0.5f)
                        )
                    },
                    placeholder = { Text("0", color = onSurfaceColor.copy(alpha = 0.2f)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = onSurfaceColor,
                        unfocusedTextColor = onSurfaceColor,
                        focusedBorderColor = item.color.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    trailingIcon = {
                        if (currentPriceInput.isNotEmpty() && !isBlurred) {
                            IconButton(onClick = { onPriceChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = onSurfaceColor.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                )

            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(item.color.copy(alpha = 0.04f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    viewModel.strings.totalLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${currencyFormat.format(item.totalCost)} ₺",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = item.color,
                        modifier = Modifier.then(if (isBlurred) Modifier.blur(8.dp) else Modifier)
                    )
                    if (isBlurred) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = item.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


@AppPreviews
@Composable
fun SwapLayoutResultRowPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }

    FenceCalculatorTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SwapLayoutResultRow(
                viewModel = viewModel,
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