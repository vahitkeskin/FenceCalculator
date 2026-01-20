package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    var showSettingsSheet by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
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
                        scope.launch {
                            isGeneratingPdf = true
                            delay(1500)
                            PdfGenerator.generateAndSharePdf(
                                context = context,
                                results = viewModel.results,
                                totalCost = viewModel.grandTotalCost,
                                length = viewModel.totalLengthInput
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
        },
        // Scaffold'un kendi window inset yönetimini iptal edip kontrolü ele alıyoruz.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->

        // %100 ÇÖZÜM MANTIĞI:
        // 1. Box, ekranın tamamını kaplar.
        // 2. .imePadding() ekleyerek, klavye açıldığında Box'ın boyunu ZORLA kısaltırız.
        //    (Manifest ayarı çalışsa da çalışmasa da bu kod çalışır).
        // 3. Böylece AnimatedWaveBottomBar her zaman klavyenin hemen tepesinde durur.
        // 4. Listenin altına da Alt Barın yüksekliği kadar (örn: 250dp) sabit boşluk veririz.

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Topbar boşluğu
                .imePadding()          // KRİTİK: Klavye açılınca Box'ı yukarı it/sıkıştır
                .navigationBarsPadding() // Sanal tuşlar varsa üstüne binmesin
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Alt Bar'ın yaklaşık yüksekliği + ekstra güvenli boşluk.
                // AnimatedWaveBottomBar genelde 150-200dp yer kaplar.
                // Biz 260.dp vererek son item'ın barın üstüne rahatça çıkmasını garantiliyoruz.
                contentPadding = PaddingValues(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 260.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

            // ALT BAR
            // Box .imePadding() aldığı için, klavye açıldığında Box küçülür
            // ve bottom'a hizalı olan bu bar otomatik olarak klavyenin üzerine çıkar.
            AnimatedWaveBottomBar(
                totalCost = viewModel.grandTotalCost,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // --- Sheet ve Dialog Kodları Aynı ---
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
                        Text("Lütfen bekleyin.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}