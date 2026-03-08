package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.data.model.CustomCardItem
import com.vahitkeskin.fencecalculator.ui.components.ColorPickerCircle
import com.vahitkeskin.fencecalculator.ui.components.EmojiPicker
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.PremiumGlassCard
import com.vahitkeskin.fencecalculator.ui.components.presetColors
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.ui.components.VerticalPicker
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    viewModel: CalculatorViewModel,
    editCardId: String?,
    onNavigateBack: () -> Unit
) {
    // Eğer düzenleme modundaysak, mevcut kartı al
    val existingCard = editCardId?.let { viewModel.getCustomCardById(it) }

    var title by remember { mutableStateOf(existingCard?.title ?: "") }
    var description by remember { mutableStateOf(existingCard?.description ?: "") }
    var quantity by remember { mutableStateOf(existingCard?.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(existingCard?.unit ?: "") }
    var unitPrice by remember { mutableStateOf(existingCard?.unitPrice?.toString() ?: "") }
    var selectedColorHex by remember { mutableStateOf(existingCard?.colorHex ?: presetColors.first()) }
    var selectedEmoji by remember { mutableStateOf(existingCard?.emoji ?: "📦") }
    
    // Dependency states
    var isDependent by remember { mutableStateOf(existingCard?.dependentCardId != null) }
    var dependentCardId by remember { mutableStateOf(existingCard?.dependentCardId ?: "") }
    var dependentRatio by remember { mutableStateOf(existingCard?.dependentRatio?.toString() ?: "") }
    var dependentOperation by remember { mutableStateOf(existingCard?.dependentOperation ?: "*") }

    val allBaseCards = viewModel.results // Varsayılanlar
    val baseCardOptions = allBaseCards.map { it.id to it.title }
    
    val unitOptions = remember { listOf("Adet", "Metre", "Kg", "Litre", "Birim", "TL") }
    if (unit.isEmpty()) unit = unitOptions.first()
    
    // Rasyo seçenekleri artık manuel
    // Kullanıcı isteği: miktar/değer alanı ilk başta temiz olsun.
    
    var isDependentCardLocked by remember { mutableStateOf(false) }
    var isDependentUnitLocked by remember { mutableStateOf(false) }

    val isEditing = existingCard != null
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    // Validation
    val isFormValid = title.isNotBlank() && unit.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (isEditing) "KARTI DÜZENLE" else "YENİ KART EKLE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = onBackgroundColor,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "MANUEL GİRİŞ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Geri",
                                tint = onBackgroundColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Başlık ---
                PremiumGlassCard {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Kart Başlığı *", color = onBackgroundColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                    )
                }

                // --- Açıklama ---
                PremiumGlassCard {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Açıklama / Not", color = onBackgroundColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                    )
                }

                // --- Miktar ve Birim ---
                PremiumGlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "MİKTAR & BİRİM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Bağımlılık Ayarları (Her Zaman Görünür)
                            // Bağımlılık Ayarları
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "REFERANS KART SEÇİN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = onBackgroundColor.copy(alpha = 0.5f)
                                    )
                                }
                                
                                // Kart Seçici (VerticalPicker)
                                VerticalPicker(
                                    items = baseCardOptions,
                                    selectedItem = baseCardOptions.find { it.first == dependentCardId } ?: baseCardOptions.firstOrNull() ?: ("" to ""),
                                    onItemSelected = { 
                                        if (dependentCardId != it.first) {
                                            dependentCardId = it.first
                                            if (!isEditing) dependentRatio = "" // Yeni kartta kart değiştikçe boşalt
                                        }
                                    },
                                    label = { it.second },
                                    visibleItemsCount = 3,
                                    isLocked = isDependentCardLocked,
                                    onLockToggle = { isDependentCardLocked = !isDependentCardLocked }
                                )

                                // İşlem Seçici
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "İŞLEM SEÇİN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = onBackgroundColor.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        listOf("+" to "Topla", "-" to "Çıkar", "*" to "Çarp", "÷" to "Böl").forEach { (op, label) ->
                                            val isSelected = dependentOperation == op
                                            val animatedContainerColor by animateColorAsState(
                                                targetValue = if (isSelected) primaryColor else onBackgroundColor.copy(alpha = 0.05f),
                                                label = "color"
                                            )
                                            val animatedContentColor by animateColorAsState(
                                                targetValue = if (isSelected) Color.White else onBackgroundColor.copy(alpha = 0.6f),
                                                label = "contentColor"
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(animatedContainerColor)
                                                    .clickable { dependentOperation = op },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = op, 
                                                        fontSize = 28.sp, 
                                                        fontWeight = FontWeight.Black,
                                                        color = animatedContentColor
                                                    )
                                                    Text(
                                                        text = label,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = animatedContentColor.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = dependentRatio,
                                        onValueChange = { newVal ->
                                            val s = newVal.replace(',', '.')
                                            if (s.isEmpty() || (s.all { it.isDigit() || it == '.' } && s.count { it == '.' } <= 1)) {
                                                dependentRatio = s
                                            }
                                        },
                                        label = { Text("Miktar / Değer", color = onBackgroundColor.copy(alpha = 0.5f)) },
                                        leadingIcon = { 
                                            if (dependentOperation == "÷") {
                                                Text(
                                                    "÷",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = primaryColor,
                                                    modifier = Modifier.padding(start = 4.dp)
                                                )
                                            } else {
                                                val icon = when(dependentOperation) {
                                                    "+" -> Icons.Default.Add
                                                    "-" -> Icons.Default.Remove
                                                    else -> Icons.Default.Close
                                                }
                                                Icon(icon, contentDescription = null, tint = primaryColor)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                                    )
                                    
                                    Column(modifier = Modifier.weight(0.6f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "BİRİM",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = onBackgroundColor.copy(alpha = 0.5f)
                                            )
                                        }
                                        VerticalPicker(
                                            items = unitOptions,
                                            selectedItem = unitOptions.find { it == unit } ?: unitOptions.first(),
                                            onItemSelected = { unit = it },
                                            visibleItemsCount = 3,
                                            isLocked = isDependentUnitLocked,
                                            onLockToggle = { isDependentUnitLocked = !isDependentUnitLocked }
                                        )
                                    }
                                }
                                
                                // Hesaplanan miktar önizlemesi
                                val baseQty = allBaseCards.find { it.id == dependentCardId }?.quantity ?: 0.0
                                val ratio = dependentRatio.toDoubleOrNull() ?: 0.0
                                val calcQty = kotlin.math.ceil(
                                    when (dependentOperation) {
                                        "+" -> baseQty + ratio
                                        "-" -> baseQty - ratio
                                        "÷", "/" -> if (ratio != 0.0) baseQty / ratio else baseQty
                                        else -> baseQty * ratio
                                    }
                                )
                                if (dependentCardId.isNotEmpty()) {
                                    PremiumGlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 16.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Calculate, contentDescription = null, tint = primaryColor)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                "Sonuç: ${baseQty.toInt()} $dependentOperation $ratio = ${calcQty.toInt()} $unit",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = onBackgroundColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        
                        // Manuel Giriş (Her Zaman Görünür)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { newVal ->
                                    val s = newVal.replace(',', '.')
                                    if (s.isEmpty() || (s.all { it.isDigit() || it == '.' } && s.count { it == '.' } <= 1)) {
                                        quantity = s
                                    }
                                },
                                label = { Text("Miktar", color = onBackgroundColor.copy(alpha = 0.5f)) },
                                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                            )
                        }
                    }
                }

                // --- Birim Fiyat ---
                PremiumGlassCard {
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { newVal ->
                            val s = newVal.replace(',', '.')
                            if (s.isEmpty() || (s.all { it.isDigit() || it == '.' } && s.count { it == '.' } <= 1)) {
                                unitPrice = s
                            }
                        },
                        label = { Text("Birim Fiyat (₺)", color = onBackgroundColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                    )
                }

                // --- Emoji Seçici ---
                PremiumGlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        EmojiPicker(
                            selectedEmoji = selectedEmoji,
                            onEmojiSelected = { selectedEmoji = it }
                        )
                    }
                }

                // --- Renk Seçici ---
                PremiumGlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ColorPickerCircle(
                            selectedColorHex = selectedColorHex,
                            onColorSelected = { selectedColorHex = it }
                        )
                    }
                }

                // --- Önizleme ---
                val previewTotal = (quantity.toDoubleOrNull() ?: 0.0) * (unitPrice.toDoubleOrNull() ?: 0.0)
                if (title.isNotBlank()) {
                    val previewColor = try {
                        Color(android.graphics.Color.parseColor(selectedColorHex))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                    PremiumGlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "ÖNİZLEME",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor.copy(alpha = 0.5f),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = previewColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = selectedEmoji,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = onBackgroundColor
                                    )
                                    if (description.isNotBlank()) {
                                        Text(
                                            description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = onBackgroundColor.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                if (quantity.isNotBlank() && unit.isNotBlank()) {
                                    Surface(
                                        color = onBackgroundColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "$quantity $unit",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = onBackgroundColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            if (previewTotal > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Toplam: ${String.format("%,.2f", previewTotal)} ₺",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = previewColor
                                )
                            }
                        }
                    }
                }

                // --- Kaydet Butonu ---
                Button(
                    onClick = {
                        val card = CustomCardItem(
                            id = existingCard?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            description = description.trim(),
                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                            unit = unit.trim(),
                            unitPrice = unitPrice.toDoubleOrNull() ?: 0.0,
                            colorHex = selectedColorHex,
                            emoji = selectedEmoji,
                            dependentCardId = dependentCardId.ifEmpty { null },
                            dependentRatio = dependentRatio.toDoubleOrNull(),
                            dependentOperation = dependentOperation
                        )
                        viewModel.addOrUpdateCustomCard(card)
                        onNavigateBack()
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = primaryColor.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        if (isEditing) Icons.Default.Save else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isEditing) "GÜNCELLE" else "KAYDET",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // --- Sil Butonu (sadece düzenleme modunda) ---
                if (isEditing && existingCard != null) {
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteCustomCard(existingCard.id)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "KARTI SİL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                // Klavye açıldığında en alttaki içeriğin yukarı kaydırılabilmesi için spacer
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors(
    onBackgroundColor: Color,
    primaryColor: Color
) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = primaryColor,
    unfocusedBorderColor = Color(0xFFCBD5E1),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedTextColor = onBackgroundColor,
    unfocusedTextColor = onBackgroundColor
)
