package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vahitkeskin.fencecalculator.ui.components.AdvancedInputSection
import com.vahitkeskin.fencecalculator.ui.components.AnimatedWaveBottomBar
import com.vahitkeskin.fencecalculator.ui.components.SwapLayoutResultRow
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.util.PdfGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    // UI State
    var showSettingsSheet by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    // Müşteri İsmi State
    var customerNameInput by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // --- İSİM FORMATLAMA MANTIĞI ---
    fun getFormattedCustomerTitle(rawName: String): String {
        if (rawName.isBlank()) return "İsimsiz Müşteri - Tel Çit Hesaplama"

        val trimmedName = rawName.trim()
        val words = trimmedName.split("\\s+".toRegex())

        val formattedName = if (words.size == 1) {
            words.first().lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } else {
            val lastWordIndex = words.lastIndex
            words.mapIndexed { index, word ->
                if (index == lastWordIndex) {
                    word.uppercase(Locale.getDefault())
                } else {
                    word.lowercase(Locale.getDefault())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }.joinToString(" ")
        }

        return "$formattedName - Tel Çit Hesaplama"
    }

    Scaffold(
        // TOOLBAR DÜZELTİLDİ: Varsayılan Scaffold ayarları kullanılıyor.
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Çit Maliyet",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Hesaplayıcı",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        scope.launch {
                            isGeneratingPdf = true
                            val finalPdfTitle = getFormattedCustomerTitle(customerNameInput)
                            delay(1500)
                            PdfGenerator.generateAndSharePdf(
                                context = context,
                                results = viewModel.results,
                                totalCost = viewModel.grandTotalCost,
                                length = viewModel.totalLengthInput,
                                customerTitle = finalPdfTitle
                            )
                            isGeneratingPdf = false
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "PDF Paylaş", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
        // HATALI SATIR KALDIRILDI: contentWindowInsets = WindowInsets(0, 0, 0, 0)
        // Scaffold artık Status Bar'ı otomatik yönetiyor.
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Toolbar'ın altına iner
                .imePadding()          // Klavye açılınca Box'ı sıkıştırır (Alt bar yukarı çıkar)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 260.dp // Alt bar + Güvenli boşluk
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Müşteri Adı Alanı
                item {
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = { customerNameInput = it },
                        label = { Text("Müşteri Adı Soyadı") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                item {
                    AdvancedInputSection(
                        lengthValue = viewModel.totalLengthInput,
                        onLengthChange = viewModel::onTotalLengthChange,
                        heightValue = viewModel.fenceHeightInput,
                        onHeightChange = viewModel::onFenceHeightChange,
                        spacingValue = viewModel.poleSpacingInput,
                        onSpacingChange = viewModel::onPoleSpacingChange
                    )
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("GİDER KALEMLERİ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showSettingsSheet = true }) {
                            Text("Değerleri Düzenle", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                items(viewModel.results, key = { it.id }) { item ->
                    val rawPrice = viewModel.getPriceString(item.id)
                    SwapLayoutResultRow(
                        item = item,
                        currentPriceInput = rawPrice,
                        onPriceChange = { newPrice -> viewModel.onPriceChange(item.id, newPrice) }
                    )
                }
            }

            AnimatedWaveBottomBar(
                totalCost = viewModel.grandTotalCost,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SettingsSheetContent(viewModel = viewModel) { showSettingsSheet = false }
            }
        }

        if (isGeneratingPdf) {
            Dialog(onDismissRequest = {}) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("PDF Raporu Hazırlanıyor...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if(customerNameInput.isNotBlank()) "Sayın ${getFormattedCustomerTitle(customerNameInput).substringBefore(" -")}" else "Lütfen bekleyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}