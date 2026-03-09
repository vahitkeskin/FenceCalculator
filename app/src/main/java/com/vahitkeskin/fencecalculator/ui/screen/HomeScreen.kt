package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.vahitkeskin.fencecalculator.util.QrGenerator
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.R
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalculatorViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfFileForPreview by remember { mutableStateOf<java.io.File?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.scrollToTop.collect { route ->
            if (route == "home_tab") {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val currencyFormat = remember { DecimalFormat("#,##0.00") }

    fun getFormattedCustomerTitle(name: String): String {
        return if (name.isBlank()) "" else name.uppercase()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(viewModel.strings.fenceCalculation, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = onBackgroundColor, letterSpacing = 2.sp)
                            Text(viewModel.strings.premiumArchitecturalTool, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = onBackgroundColor.copy(alpha = 0.5f), letterSpacing = 1.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                isGeneratingPdf = true
                                val finalPdfTitle = getFormattedCustomerTitle(viewModel.customerName)
                                delay(1000)
                                val file = PdfGenerator.generatePdf(
                                    context = context,
                                    results = viewModel.results,
                                    totalCost = viewModel.grandTotalCost,
                                    length = viewModel.totalLengthInput,
                                    customerTitle = finalPdfTitle,
                                    companyName = viewModel.companyName,
                                    viewModel = viewModel
                                )
                                isGeneratingPdf = false
                                pdfFileForPreview = file
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = viewModel.strings.sharePdf, tint = primaryColor)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                overscrollEffect = null
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Müşteri Bilgileri
                item {
                    PremiumGlassCard(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            viewModel.strings.customerInfo,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = onBackgroundColor.copy(alpha = 0.5f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                        )
                        
                        OutlinedTextField(
                            value = viewModel.customerName,
                            onValueChange = { viewModel.onCustomerNameChange(it) },
                            label = { Text(viewModel.strings.customerNameSurname, color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                        )

                        PhoneNumberField(
                            phoneNumber = viewModel.customerPhone,
                            onPhoneNumberChange = { viewModel.onCustomerPhoneChange(it) },
                            label = viewModel.strings.customerPhoneNo,
                            selectCountryLabel = viewModel.strings.selectCountry,
                            searchCountryLabel = viewModel.strings.searchCountryOrCode,
                            primaryColor = primaryColor,
                            onBackgroundColor = onBackgroundColor
                        )
                    }
                }

                // Ana Parametreler
                item {
                    AdvancedInputSection(
                        labelText = viewModel.strings.pdfTotalLengthLabel.removeSuffix(":"),
                        lengthValue = viewModel.totalLengthInput,
                        onLengthChange = viewModel::onTotalLengthChange
                    )
                }

                // --- FAVORİ HESAPLAMALAR ---
                if (viewModel.pinnedItems.isNotEmpty()) {
                    item {
                        Divider(color = onBackgroundColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                    }
                    
                    item {
                        Text(
                            viewModel.strings.favoriteCalculations,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = onBackgroundColor.copy(alpha = 0.5f),
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(viewModel.pinnedItems.size) { index ->
                        val item = viewModel.pinnedItems[index]
                        val isCustom = item.id.startsWith("custom_")
                        val realId = if (isCustom) item.id.removePrefix("custom_") else item.id
                        
                        Box(modifier = Modifier.clickable {
                            if (isCustom) {
                                navController.navigate("add_edit_card/$realId")
                            }
                        }) {
                            SwapLayoutResultRow(
                                viewModel = viewModel,
                                item = item,
                                currentPriceInput = viewModel.getPriceString(item.id),
                                onPriceChange = { viewModel.onPriceChange(item.id, it) },
                                onPinToggle = { viewModel.togglePin(item.id) }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Hızlı Bilgi
                item {
                    PremiumGlassCard(
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(42.dp),
                                color = primaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            
                            Text(
                                viewModel.strings.infoTextHome,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor.copy(alpha = 0.6f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Klavye ve Toplam Maliyet kartının üstünde kalması için büyük boşluk
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }

        if (isGeneratingPdf) {
            Dialog(onDismissRequest = {}) {
                PremiumGlassCard {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(viewModel.strings.preparingPdfReport, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        pdfFileForPreview?.let { file ->
            PdfPreviewDialog(
                file,
                viewModel
            ) { pdfFileForPreview = null }
        }
    }
}

@AppPreviews
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }
    val navController = rememberNavController()
    
    FenceCalculatorTheme {
        HomeScreen(viewModel = viewModel, navController = navController)
    }
}