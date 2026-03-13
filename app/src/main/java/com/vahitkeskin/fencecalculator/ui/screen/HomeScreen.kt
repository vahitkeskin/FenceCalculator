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
import com.vahitkeskin.fencecalculator.util.AdManager
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.ui.theme.shadowlessElevation
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
    navController: NavController,
    onPremiumClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfFileForPreview by remember { mutableStateOf<java.io.File?>(null) }
    val scope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val sheetState = rememberModalBottomSheetState()
    var showScanSheet by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.scanQrCode(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            bitmap?.let { b -> viewModel.scanQrCode(b) }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, viewModel.strings.cameraPermissionRequired, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scrollToTop.collect { route ->
            if (route == "home_tab") {
                listState.animateScrollToItem(0)
            }
        }
    }
    
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
                            // TODO: İstediğim zaman aktif edebileyim - 50 sınırlaması ve premium
                            // Text(viewModel.strings.premiumArchitecturalTool, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = onBackgroundColor.copy(alpha = 0.5f), letterSpacing = 1.sp)
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
                                    results = viewModel.orderedVisibleItems,
                                    totalCost = viewModel.grandTotalCost,
                                    length = viewModel.totalLengthInput,
                                    customerTitle = finalPdfTitle,
                                    customerName = viewModel.customerName,
                                    companyName = viewModel.companyName,
                                    viewModel = viewModel
                                )
                                isGeneratingPdf = false
                                pdfFileForPreview = file
                                // activity?.let { AdManager.onShareClicked(it) }
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
                    PremiumGlassCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                viewModel.strings.customerInfo,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundColor.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                            
                            OutlinedTextField(
                                value = viewModel.customerName,
                                onValueChange = { viewModel.onCustomerNameChange(it) },
                                label = { Text(viewModel.strings.customerNameSurname, color = onBackgroundColor.copy(alpha = 0.5f)) },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                            )

                            PhoneNumberField(
                                phoneNumber = viewModel.customerPhone,
                                onPhoneNumberChange = { viewModel.onCustomerPhoneChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = viewModel.strings.customerPhoneNo,
                                selectCountryLabel = viewModel.strings.selectCountry,
                                searchCountryLabel = viewModel.strings.searchCountryOrCode,
                                primaryColor = primaryColor,
                                onBackgroundColor = onBackgroundColor
                            )

                            // --- Expandable IBAN Section ---
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.onIbanExpandedToggle(!viewModel.isIbanExpanded) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccountBalance, 
                                            null, 
                                            tint = primaryColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            viewModel.strings.iban,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = onBackgroundColor.copy(alpha = 0.7f)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (viewModel.isIbanExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = onBackgroundColor.copy(alpha = 0.5f)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = viewModel.isIbanExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val isIbanValid = remember(viewModel.iban) {
                                            viewModel.iban.isBlank() || com.vahitkeskin.fencecalculator.util.IbanValidator.isValidIban(viewModel.iban)
                                        }

                                        OutlinedTextField(
                                            value = viewModel.iban,
                                            onValueChange = { viewModel.onIbanChange(it) },
                                            label = { Text(viewModel.strings.iban, color = onBackgroundColor.copy(alpha = 0.5f)) },
                                            leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = onBackgroundColor.copy(alpha = 0.7f)) },
                                            trailingIcon = {
                                                IconButton(onClick = { showScanSheet = true }) {
                                                    Icon(Icons.Default.QrCodeScanner, viewModel.strings.scanQrCode, tint = primaryColor)
                                                }
                                            },
                                            isError = !isIbanValid && viewModel.iban.isNotBlank(),
                                            supportingText = {
                                                if (!isIbanValid && viewModel.iban.isNotBlank()) {
                                                    Text(viewModel.strings.invalidIban, color = MaterialTheme.colorScheme.error)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            maxLines = 2,
                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = primaryColor,
                                                errorBorderColor = MaterialTheme.colorScheme.error
                                            )
                                        )

                                        val qrBitmap = remember(viewModel.iban) {
                                            if (com.vahitkeskin.fencecalculator.util.IbanValidator.isValidIban(viewModel.iban)) {
                                                com.vahitkeskin.fencecalculator.util.QrGenerator.generateQrCode(viewModel.iban, 300)
                                            } else null
                                        }

                                        qrBitmap?.let { bitmap ->
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                PremiumGlassCard(
                                                    modifier = Modifier
                                                        .size(160.dp)
                                                        .padding(8.dp)
                                                ) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = viewModel.strings.ibanQrCodeDesc,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(8.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Ana Parametreler
                item {
                    AdvancedInputSection(
                        labelText = viewModel.strings.pdfTotalLengthLabel.removeSuffix(":"),
                        lengthValue = viewModel.totalLengthDraft,
                        onLengthChange = viewModel::onTotalLengthChange,
                        onClear = { viewModel.clearTotalLength() }
                    )
                }

                // --- FAVORİ HESAPLAMALAR (STANDART) ---
                val standardPinned = viewModel.pinnedItems.filter { !it.id.startsWith("custom_") }
                if (standardPinned.isNotEmpty()) {
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
                    
                    items(standardPinned) { item ->
                        SwapLayoutResultRow(
                            viewModel = viewModel,
                            item = item,
                            currentPriceInput = viewModel.getPriceString(item.id),
                            onPriceChange = { viewModel.onPriceChange(item.id, it) },
                            onPinToggle = { viewModel.togglePin(item.id) },
                            onPremiumClick = onPremiumClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // --- ÖZEL KARTLAR ---
                val customPinned = viewModel.pinnedItems.filter { it.id.startsWith("custom_") }
                if (customPinned.isNotEmpty()) {
                    item {
                        Divider(color = onBackgroundColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                    }
                    
                    item {
                        Text(
                            viewModel.strings.customCards,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = onBackgroundColor.copy(alpha = 0.5f),
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(customPinned) { item ->
                        val realId = item.id.removePrefix("custom_")
                        SwapLayoutResultRow(
                            viewModel = viewModel,
                            item = item,
                            currentPriceInput = viewModel.getPriceString(item.id),
                            onPriceChange = { viewModel.onPriceChange(item.id, it) },
                            onPinToggle = { viewModel.togglePin(item.id) },
                            onPremiumClick = onPremiumClick,
                            onClick = {
                                navController.navigate("add_edit_card/$realId")
                            }
                        )
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
                        viewModel.strings.scanQrCodeTitle,
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
                                cameraLauncher.launch(null)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        elevation = (shadowlessElevation())
                    ) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(Modifier.width(12.dp))
                        Text(viewModel.strings.useCamera, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            showScanSheet = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor),
                        elevation = (shadowlessElevation())
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(12.dp))
                        Text(viewModel.strings.selectFromGallery, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryColor)
                    }
                }
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
        HomeScreen(
            viewModel = viewModel, 
            navController = navController,
            onPremiumClick = {}
        )
    }
}