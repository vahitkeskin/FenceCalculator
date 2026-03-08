package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfFileForPreview by remember { mutableStateOf<java.io.File?>(null) }
    val scope = rememberCoroutineScope()
    
    var showScanSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap?.let { b -> viewModel.scanQrCode(b) }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: android.graphics.Bitmap? ->
        bitmap?.let { viewModel.scanQrCode(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Kamera izni verilmedi!", Toast.LENGTH_SHORT).show()
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
                            Text("ÇİT HESAPLAMA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = onBackgroundColor, letterSpacing = 2.sp)
                            Text("PREMIUM ARCHITECTURAL TOOL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = onBackgroundColor.copy(alpha = 0.5f), letterSpacing = 1.sp)
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
                                    companyName = viewModel.companyName
                                )
                                isGeneratingPdf = false
                                pdfFileForPreview = file
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "PDF Paylaş", tint = primaryColor)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Firma Adı
                PremiumGlassCard {
                    OutlinedTextField(
                        value = viewModel.companyName,
                        onValueChange = { viewModel.onCompanyNameChange(it) },
                        label = { Text("Firma Adı", color = onBackgroundColor.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Business, null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                    )
                }

                // Müşteri Bilgileri
                PremiumGlassCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "MÜŞTERİ BİLGİLERİ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = onBackgroundColor.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                        
                        OutlinedTextField(
                            value = viewModel.customerName,
                            onValueChange = { viewModel.onCustomerNameChange(it) },
                            label = { Text("Müşteri Adı Soyadı", color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                        )

                        OutlinedTextField(
                            value = viewModel.customerPhone,
                            onValueChange = { viewModel.onCustomerPhoneChange(it) },
                            label = { Text("Müşteri Telefon No", color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            placeholder = { Text("905xxxxxxxxx", color = onBackgroundColor.copy(alpha = 0.2f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                        )

                        OutlinedTextField(
                            value = viewModel.iban,
                            onValueChange = { viewModel.onIbanChange(it) },
                            label = { Text("IBAN", color = onBackgroundColor.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                            trailingIcon = {
                                IconButton(onClick = { showScanSheet = true }) {
                                    Icon(Icons.Default.QrCodeScanner, "Karekod Tara", tint = primaryColor)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 2,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                        )

                        val qrBitmap = remember(viewModel.iban) {
                            if (viewModel.iban.isNotBlank()) {
                                QrGenerator.generateQrCode(viewModel.iban, 300)
                            } else null
                        }

                        qrBitmap?.let { bitmap ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PremiumGlassCard(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .padding(8.dp)
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "IBAN QR Code",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Text(
                                    "IBAN Karekod",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onBackgroundColor.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Ana Parametreler
                AdvancedInputSection(
                    lengthValue = viewModel.totalLengthInput,
                    onLengthChange = viewModel::onTotalLengthChange
                )

                // --- FAVORİ HESAPLAMALAR ---
                if (viewModel.pinnedItems.isNotEmpty()) {
                    Divider(color = onBackgroundColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        "FAVORİ HESAPLAMALAR",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = onBackgroundColor.copy(alpha = 0.5f),
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    viewModel.pinnedItems.forEach { item ->
                        val isCustom = item.id.startsWith("custom_")
                        val realId = if (isCustom) item.id.removePrefix("custom_") else item.id
                        
                        Box(modifier = Modifier.clickable {
                            if (isCustom) {
                                navController.navigate("add_edit_card/$realId")
                            }
                        }) {
                            SwapLayoutResultRow(
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
                Text(
                    "Hesaplama detaylarını görmek için alttaki 'Hesaplar' veya 'Özel' sekmelerini kullanın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onBackgroundColor.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Klavye ve Toplam Maliyet kartının üstünde kalması için büyük boşluk
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        if (isGeneratingPdf) {
            Dialog(onDismissRequest = {}) {
                PremiumGlassCard {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("PDF Raporu Hazırlanıyor...", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        pdfFileForPreview?.let { file ->
            PdfPreviewDialog(
                file,
                viewModel.customerPhone,
                viewModel.iban
            ) { pdfFileForPreview = null }
        }

        if (showScanSheet) {
            ModalBottomSheet(
                onDismissRequest = { showScanSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "KAREKOD TARA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Button(
                        onClick = {
                            showScanSheet = false
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (hasPermission) {
                                cameraLauncher.launch()
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Kamerayı Kullan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    
                    OutlinedButton(
                        onClick = {
                            showScanSheet = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Galeriden Seç", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryColor)
                    }
                }
            }
        }
    }
}