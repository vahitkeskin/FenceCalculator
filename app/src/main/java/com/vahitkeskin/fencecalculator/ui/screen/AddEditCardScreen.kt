package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.text.style.TextOverflow
import com.vahitkeskin.fencecalculator.data.model.CustomCardItem
import com.vahitkeskin.fencecalculator.ui.components.ColorPickerCircle
import com.vahitkeskin.fencecalculator.ui.components.EmojiPicker
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.PremiumGlassCard
import com.vahitkeskin.fencecalculator.ui.components.presetColors
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.ui.components.VerticalPicker
import java.util.UUID
import androidx.compose.ui.platform.LocalContext
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.ui.theme.shadowlessElevation
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.R
import androidx.compose.ui.res.stringResource
import com.vahitkeskin.fencecalculator.util.NavigationUtils.safeClick
import com.vahitkeskin.fencecalculator.util.centerOnFocus


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
    val baseCardOptions = remember(viewModel.results, viewModel.customCards) {
        val list = mutableListOf("v_total_length" to viewModel.strings.pdfTotalLengthLabel.removeSuffix(":"))
        list.addAll(viewModel.results.map { it.id to it.title })
        list.addAll(viewModel.customCards.filter { it.id != editCardId }.map { it.id to it.title })
        list
    }
    
    val unitOptions = remember { listOf(
        viewModel.strings.unitPiece, 
        viewModel.strings.unitMeter, 
        viewModel.strings.unitKg, 
        viewModel.strings.unitLiter, 
        viewModel.strings.unitUnit, 
        viewModel.strings.unitTl
    ) }
    if (unit.isEmpty()) unit = unitOptions.first()
    
    // Rasyo seçenekleri artık manuel
    // Kullanıcı isteği: miktar/değer alanı ilk başta temiz olsun.
    
    var isDependentCardLocked by remember { mutableStateOf(false) }
    var isDependentUnitLocked by remember { mutableStateOf(false) }

    val isEditing = existingCard != null
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary

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
                                if (isEditing) viewModel.strings.editCard else viewModel.strings.addNewCard,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = onBackgroundColor,
                                letterSpacing = 2.sp
                            )
                            Text(
                                viewModel.strings.manualEntry,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { safeClick { onNavigateBack() } }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = viewModel.strings.back,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                overscrollEffect = null
            ) {
                item {
                    // --- Başlık ---
                    PremiumGlassCard {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(viewModel.strings.cardTitleRequired, color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            trailingIcon = {
                                if (title.isNotEmpty()) {
                                    IconButton(
                                        onClick = { title = "" },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = if (androidx.compose.foundation.isSystemInDarkTheme())
                                                    Color.White.copy(alpha = 0.15f)
                                                else
                                                    Color.Black.copy(alpha = 0.08f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .padding(5.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = if (androidx.compose.foundation.isSystemInDarkTheme())
                                                Color.White.copy(alpha = 0.6f)
                                            else
                                                Color.Black.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).centerOnFocus(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                        )
                    }
                }

                item {
                    // --- Açıklama ---
                    PremiumGlassCard {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(viewModel.strings.descriptionNote, color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            trailingIcon = {
                                if (description.isNotEmpty()) {
                                    IconButton(
                                        onClick = { description = "" },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = if (androidx.compose.foundation.isSystemInDarkTheme())
                                                    Color.White.copy(alpha = 0.15f)
                                                else
                                                    Color.Black.copy(alpha = 0.08f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .padding(5.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = if (androidx.compose.foundation.isSystemInDarkTheme())
                                                Color.White.copy(alpha = 0.6f)
                                            else
                                                Color.Black.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).centerOnFocus(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                        )
                    }
                }

                item {
                    // --- Miktar ve Birim ---
                    PremiumGlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    viewModel.strings.quantityAndUnit,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onBackgroundColor.copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Bağımlılık Ayarları (Her Zaman Görünür)
                                // Bağımlılık Ayarları
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    // Referans Kart Seçici Başlığı
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 0.dp)) {
                                        Icon(Icons.Default.CopyAll, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(viewModel.strings.selectReferenceCard, style = MaterialTheme.typography.titleSmall, color = onBackgroundColor.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                                    }
                                    
                                    // Kart Seçici (VerticalPicker)
                                    if (baseCardOptions.isNotEmpty()) {
                                        VerticalPicker(
                                            items = baseCardOptions,
                                            selectedItem = baseCardOptions.find { it.first == dependentCardId } ?: baseCardOptions.first(),
                                            onItemSelected = { 
                                                if (dependentCardId != it.first) {
                                                    dependentCardId = it.first
                                                    if (!isEditing) dependentRatio = "" // Yeni kartta kart değiştikçe boşalt
                                                }
                                            },
                                            label = { it.second },
                                            visibleItemsCount = 3,
                                            isLocked = isDependentCardLocked,
                                            onLockToggle = { isDependentCardLocked = !isDependentCardLocked },
                                            tooltipText = viewModel.strings.lockTooltip,
                                            showTooltip = !viewModel.isLockTooltipShown,
                                            onTooltipDismiss = { viewModel.setLockTooltipShown() }
                                        )
                                    } else {
                                        Text(
                                            viewModel.strings.noCardsYet,
                                            modifier = Modifier.padding(8.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = onBackgroundColor.copy(alpha = 0.5f)
                                        )
                                    }

                                    // İşlem Seçici
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            viewModel.strings.selectOperation,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = onBackgroundColor.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            listOf("+" to viewModel.strings.opAdd, "-" to viewModel.strings.opSub, "*" to viewModel.strings.opMul, "÷" to viewModel.strings.opDiv).forEach { (op, label) ->
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

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // 1. Başlıklar Satırı
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // TextField Başlığı (Görünmez)
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Numbers, contentDescription = null, tint = Color.Transparent, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(" ", style = MaterialTheme.typography.titleSmall)
                                            }
                                            
                                            // Birim Seçici Başlığı
                                            Row(
                                                modifier = Modifier.weight(0.6f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.Straighten, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(viewModel.strings.unitLabel, style = MaterialTheme.typography.titleSmall, color = onBackgroundColor.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        
                                        // 2. Giriş Alanları Satırı (Seçilen ile hizalamak için dikeyde ortalanmış)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Miktar / Değer Girişi
                                            OutlinedTextField(
                                                value = dependentRatio,
                                                onValueChange = { newVal ->
                                                    val s = newVal.replace(',', '.')
                                                    if (s.isEmpty() || (s.all { it.isDigit() || it == '.' } && s.count { it == '.' } <= 1)) {
                                                        dependentRatio = s
                                                    }
                                                },
                                                label = { Text(viewModel.strings.quantityValue, color = onBackgroundColor.copy(alpha = 0.5f)) },
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
                                                modifier = Modifier.weight(1f).centerOnFocus(),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Next
                                                ),
                                                colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                                            )
                                            
                                            Column(
                                                modifier = Modifier.weight(0.6f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
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
                                    }
                                    
                                    // Hesaplanan miktar önizlemesi
                                    val baseQty = when {
                                        dependentCardId == "v_total_length" -> viewModel.totalLengthInput.toDoubleOrNull() ?: 0.0
                                        allBaseCards.any { it.id == dependentCardId } -> allBaseCards.find { it.id == dependentCardId }?.quantity ?: 0.0
                                        else -> viewModel.customCards.find { it.id == dependentCardId }?.quantity ?: 0.0
                                    }
                                    val baseUnit = when {
                                        dependentCardId == "v_total_length" -> viewModel.strings.unitMeter
                                        allBaseCards.any { it.id == dependentCardId } -> allBaseCards.find { it.id == dependentCardId }?.unit ?: ""
                                        else -> viewModel.customCards.find { it.id == dependentCardId }?.unit ?: ""
                                    }
                                    val ratio = dependentRatio.toDoubleOrNull() ?: 0.0
                                    val calcQty = kotlin.math.ceil(
                                        when (dependentOperation) {
                                            "+" -> baseQty + ratio
                                            "-" -> baseQty - ratio
                                            "÷" -> if (ratio != 0.0) baseQty / ratio else baseQty
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
                                                    String.format(viewModel.strings.calcPreviewResult, baseQty.toInt(), baseUnit, dependentOperation, ratio, calcQty.toInt(), unit),
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
                                    label = { Text(viewModel.strings.quantity, color = onBackgroundColor.copy(alpha = 0.5f)) },
                                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                                    trailingIcon = {
                                        if (quantity.isNotEmpty()) {
                                            IconButton(
                                                onClick = { quantity = "" },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(
                                                        color = if (androidx.compose.foundation.isSystemInDarkTheme())
                                                            Color.White.copy(alpha = 0.15f)
                                                        else
                                                            Color.Black.copy(alpha = 0.08f),
                                                        shape = androidx.compose.foundation.shape.CircleShape
                                                    )
                                                    .padding(5.dp),
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    contentColor = if (androidx.compose.foundation.isSystemInDarkTheme())
                                                        Color.White.copy(alpha = 0.6f)
                                                    else
                                                        Color.Black.copy(alpha = 0.5f)
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = androidx.compose.material.icons.Icons.Rounded.Close,
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).centerOnFocus(),
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
                }

                item {
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
                            label = { Text(viewModel.strings.unitPriceTl, color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            trailingIcon = {
                                if (unitPrice.isNotEmpty()) {
                                    IconButton(
                                        onClick = { unitPrice = "" },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = if (androidx.compose.foundation.isSystemInDarkTheme())
                                                    Color.White.copy(alpha = 0.15f)
                                                else
                                                    Color.Black.copy(alpha = 0.08f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .padding(5.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = if (androidx.compose.foundation.isSystemInDarkTheme())
                                                Color.White.copy(alpha = 0.6f)
                                            else
                                                Color.Black.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Rounded.Close,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).centerOnFocus(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            colors = outlinedTextFieldColors(onBackgroundColor, primaryColor)
                        )
                    }
                }

                item {
                    // --- Emoji Seçici ---
                    PremiumGlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            EmojiPicker(
                                selectedEmoji = selectedEmoji,
                                onEmojiSelected = { selectedEmoji = it }
                            )
                        }
                    }
                }

                item {
                    // --- Renk Seçici ---
                    PremiumGlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ColorPickerCircle(
                                selectedColorHex = selectedColorHex,
                                onColorSelected = { selectedColorHex = it }
                            )
                        }
                    }
                }

                item {
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
                                    viewModel.strings.previewTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onBackgroundColor.copy(alpha = 0.5f),
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                if (description.isNotBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                            .background(previewColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = previewColor.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onBackgroundColor.copy(alpha = 0.65f),
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
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
                                        // Title Only
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = onBackgroundColor
                                        )
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
                                        String.format(viewModel.strings.totalPreviewText, String.format("%,.2f", previewTotal)),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = previewColor
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // --- Kaydet Butonu ---
                    Button(
                        onClick = {
                            safeClick {
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
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            disabledContainerColor = primaryColor.copy(alpha = 0.3f)
                        ),
                        elevation = shadowlessElevation()
                    ) {
                        Icon(
                            if (isEditing) Icons.Default.Save else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isEditing) viewModel.strings.update else viewModel.strings.save,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                if (isEditing && existingCard != null) {
                    item {
                        // --- Sil Butonu (sadece düzenleme modunda) ---
                        OutlinedButton(
                            onClick = {
                                safeClick {
                                    viewModel.deleteCustomCard(existingCard.id)
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFD32F2F)
                            ),
                            elevation = shadowlessElevation()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                viewModel.strings.deleteCard,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
                
                // Klavye açıldığında en alttaki içeriğin yukarı kaydırılabilmesi için spacer
                item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime)) }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@AppPreviews
@Composable
fun AddEditCardScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }
    
    FenceCalculatorTheme {
        AddEditCardScreen(
            viewModel = viewModel,
            editCardId = null,
            onNavigateBack = {}
        )
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
