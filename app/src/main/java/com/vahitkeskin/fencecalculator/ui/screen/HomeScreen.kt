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
    // UI Durumları (State)
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
                    // PDF PAYLAŞ BUTONU
                    IconButton(onClick = {
                        scope.launch {
                            isGeneratingPdf = true
                            // Kullanıcı animasyonu görsün diye ufak bir gecikme
                            delay(1500)

                            // PDF Oluşturucu Çağır
                            PdfGenerator.generateAndSharePdf(
                                context = context,
                                results = viewModel.results,
                                totalCost = viewModel.grandTotalCost,
                                length = viewModel.totalLengthInput
                            )

                            isGeneratingPdf = false
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "PDF Paylaş",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->

        // ANA İÇERİK (BOX)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // LİSTE (Inputs + Items)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Altta BottomBar olduğu için içeriğin kesilmemesi adına bottom padding verdik
                contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 200.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Bölüm: Girdi Alanları
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

                // 2. Bölüm: Başlık ve Düzenle Butonu
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "GİDER KALEMLERİ",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showSettingsSheet = true }) {
                            Text(
                                text = "Değerleri Düzenle",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // 3. Bölüm: Hesaplama Sonuçları
                items(viewModel.results, key = { it.id }) { item ->
                    val rawPrice = viewModel.getPriceString(item.id)
                    SwapLayoutResultRow(
                        item = item,
                        currentPriceInput = rawPrice,
                        onPriceChange = { newPrice -> viewModel.onPriceChange(item.id, newPrice) }
                    )
                }
            }

            // ALT BAR (Animasyonlu Toplam Tutar)
            AnimatedWaveBottomBar(
                totalCost = viewModel.grandTotalCost,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // BOTTOM SHEET (Ayarlar)
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                //windowInsets = WindowInsets.ime
            ) {
                // SettingsSheetContent'i buraya bağlıyoruz
                SettingsSheetContent(viewModel = viewModel) {
                    showSettingsSheet = false
                }
            }
        }

        // YÜKLENİYOR DIALOG (PDF Oluşturulurken)
        if (isGeneratingPdf) {
            Dialog(onDismissRequest = { /* Kapatılamaz */ }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "PDF Raporu Hazırlanıyor...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lütfen bekleyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}