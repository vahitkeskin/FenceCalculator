package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vahitkeskin.fencecalculator.ui.components.*
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.util.PdfGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: CalculatorViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var showSettingsSheet by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfFileForPreview by remember { mutableStateOf<java.io.File?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    val customerNameInput = remember { mutableStateOf("") }
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary

    // Yardımcı: Müşteri başlığını formatla (PDF ve UI için)
    fun getFormattedCustomerTitle(name: String): String {
        return if (name.isBlank()) "" else name.uppercase()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ÇİT HESAPLAMA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = onBackgroundColor, letterSpacing = 2.sp)
                        Text("PREMIUM ARCHITECTURAL TOOL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = onBackgroundColor.copy(alpha = 0.5f), letterSpacing = 1.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            isGeneratingPdf = true
                            val finalPdfTitle = getFormattedCustomerTitle(customerNameInput.value)
                            delay(1000)
                            val file = PdfGenerator.generatePdf(
                                context = context,
                                results = viewModel.results,
                                totalCost = viewModel.grandTotalCost,
                                length = viewModel.totalLengthInput,
                                customerTitle = finalPdfTitle,
                                companyName = viewModel.companyName
                            )
                            isGeneratingPdf = false
                            pdfFileForPreview = file
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "PDF Paylaş", tint = primaryColor)
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = onBackgroundColor.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            MeshBackground()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Firma Adı Alanı (Persistent)
                item {
                    PremiumGlassCard {
                        OutlinedTextField(
                            value = viewModel.companyName,
                            onValueChange = { viewModel.onCompanyNameChange(it) },
                            label = { Text("Firma Adı (Kalıcı Saklanır)", color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = onBackgroundColor.copy(alpha = 0.1f),
                                focusedContainerColor = onBackgroundColor.copy(alpha = 0.05f),
                                unfocusedContainerColor = onBackgroundColor.copy(alpha = 0.05f),
                                focusedTextColor = onBackgroundColor,
                                unfocusedTextColor = onBackgroundColor
                            )
                        )
                    }
                }

                // Müşteri Adı Alanı
                item {
                    PremiumGlassCard {
                        OutlinedTextField(
                            value = customerNameInput.value,
                            onValueChange = { customerNameInput.value = it },
                            label = { Text("Müşteri Adı Soyadı", color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = onBackgroundColor.copy(alpha = 0.1f),
                                focusedContainerColor = onBackgroundColor.copy(alpha = 0.05f),
                                unfocusedContainerColor = onBackgroundColor.copy(alpha = 0.05f),
                                focusedTextColor = onBackgroundColor,
                                unfocusedTextColor = onBackgroundColor
                            )
                        )
                    }
                }

                item {
                    AdvancedInputSection(
                        lengthValue = viewModel.totalLengthInput,
                        onLengthChange = viewModel::onTotalLengthChange
                    )
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("HESAPLAMA DETAYLARI", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = onBackgroundColor.copy(alpha = 0.5f), modifier = Modifier.weight(1f), letterSpacing = 2.sp)
                        TextButton(onClick = { showSettingsSheet = true }) {
                            Text("Parametreleri Düzenle", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                        }
                    }
                }

                // --- GRUPLANMIŞ SONUÇLAR ---
                val resultGroups = listOf(
                    "Konstrüksiyon" to listOf("direk", "boy_demir", "payanda"),
                    "Tel Örgü Sistemi" to listOf("kafes_top", "kafes_kg", "diken"),
                    "Bağlantı & Gergi" to listOf("gergi", "baglama"),
                    "Beton & Zemin" to listOf("cimento", "beton")
                )

                resultGroups.forEach { (groupTitle, itemIds) ->
                    val groupItems = viewModel.results.filter { it.id in itemIds }
                    if (groupItems.isNotEmpty()) {
                        item {
                            Text(
                                text = groupTitle.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor.copy(alpha = 0.4f),
                                modifier = Modifier.padding(bottom = 8.dp),
                                letterSpacing = 1.sp
                            )
                        }
                        items(groupItems, key = { it.id }) { item ->
                            val rawPrice = viewModel.getPriceString(item.id)
                            SwapLayoutResultRow(
                                item = item,
                                currentPriceInput = rawPrice,
                                onPriceChange = { newPrice -> viewModel.onPriceChange(item.id, newPrice) }
                            )
                        }
                    }
                }

                // Alt boşluk (Bottom bar için)
                item { Spacer(modifier = Modifier.height(100.dp)) }
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
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = onBackgroundColor,
                tonalElevation = 0.dp
            ) {
                SettingsSheetContent(viewModel = viewModel, onDismiss = { showSettingsSheet = false })
            }
        }

        if (isGeneratingPdf) {
            Dialog(onDismissRequest = {}) {
                PremiumGlassCard {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("PDF Raporu Hazırlanıyor...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = onBackgroundColor)
                        Text(
                            text = if(customerNameInput.value.isNotBlank()) "Sayın ${getFormattedCustomerTitle(customerNameInput.value).substringBefore(" -")}" else "Lütfen bekleyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = onBackgroundColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        pdfFileForPreview?.let { file ->
            PdfPreviewDialog(file = file, onDismiss = { pdfFileForPreview = null })
        }
    }
}