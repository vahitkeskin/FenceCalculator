package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.vahitkeskin.fencecalculator.ui.components.*
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.util.PdfGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalculatorViewModel,
    navController: NavController
) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
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
        },
        bottomBar = {
            AnimatedWaveBottomBar(
                totalCost = viewModel.grandTotalCost
            )
        }
    ) { paddingValues ->
            var isEditMode by remember { mutableStateOf(false) }
            val orderedItems = viewModel.orderedVisibleItems

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
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
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
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
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
                        IconButton(onClick = { isEditMode = !isEditMode }) {
                            Icon(
                                if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditMode) "Bitti" else "Düzenle",
                                tint = if (isEditMode) primaryColor else onBackgroundColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        TextButton(onClick = { showSettingsSheet = true }) {
                            Text("Parametreleri Düzenle", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                        }
                    }
                }

                // --- UNIFIED ORDERED ITEMS ---
                items(orderedItems, key = { it.id }) { item ->
                    val isCustom = item.id.startsWith("custom_")
                    val realId = if (isCustom) item.id.removePrefix("custom_") else item.id

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Edit controls (left side)
                            if (isEditMode) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.moveCardUp(item.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, "Yukarı", tint = onBackgroundColor.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.moveCardDown(item.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, "Aşağı", tint = onBackgroundColor.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            // Card content
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (isCustom && !isEditMode) {
                                            Modifier.clickable { navController.navigate("add_edit_card/$realId") }
                                        } else Modifier
                                    )
                            ) {
                                SwapLayoutResultRow(
                                    item = item,
                                    currentPriceInput = if (isCustom) "" else viewModel.getPriceString(item.id),
                                    onPriceChange = { newPrice ->
                                        if (!isCustom) viewModel.onPriceChange(item.id, newPrice)
                                    }
                                )
                            }

                            // Delete button (right side)
                            if (isEditMode) {
                                IconButton(
                                    onClick = { viewModel.hideCard(item.id) },
                                    modifier = Modifier.size(36.dp).padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Sil", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // --- KART EKLE BUTONU ---
                item {
                    Button(
                        onClick = { navController.navigate("add_edit_card/new") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "KART EKLE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Alt boşluk
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
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