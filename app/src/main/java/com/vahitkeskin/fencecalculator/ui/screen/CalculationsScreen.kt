package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.SwapLayoutResultRow
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.vahitkeskin.fencecalculator.util.PdfGenerator
import com.vahitkeskin.fencecalculator.util.AdManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.R
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalculationsScreen(
    viewModel: CalculatorViewModel,
    navController: NavController,
    onPremiumClick: () -> Unit
) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.scrollToTop.collect { route ->
            if (route == "calculations_tab") {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    val scope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfFileForPreview by remember { mutableStateOf<java.io.File?>(null) }
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    // Filtrelenmiş ve sıralanmış tüm görünür kalemler (varsayılan + özel)
    val allItems = viewModel.orderedVisibleItems
    val groupedItems = allItems.groupBy { it.category }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()
        
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { 
                    Text(viewModel.strings.readyCalculations, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            isGeneratingPdf = true
                            val finalPdfTitle = if (viewModel.customerName.isBlank()) "" else viewModel.customerName.uppercase()
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

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                overscrollEffect = null
            ) {
                groupedItems.forEach { (category, items) ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    items(items, key = { it.id }) { item ->
                        val isCustomCard = item.id.startsWith("custom_")
                        val realId = item.id.removePrefix("custom_")
                        
                        SwapLayoutResultRow(
                            viewModel = viewModel,
                            item = item,
                            currentPriceInput = viewModel.getPriceString(item.id),
                            onPriceChange = { viewModel.onPriceChange(item.id, it) },
                            onPinToggle = { viewModel.togglePin(item.id) },
                            onPremiumClick = onPremiumClick,
                            onClick = {
                                if (isCustomCard) {
                                    navController.navigate("add_edit_card/$realId")
                                }
                            }
                        )
                    }
                }
                
                // Klavye açıldığında en alttaki içeriğin yukarı kaydırılabilmesi için spacer
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }

        if (isGeneratingPdf) {
            androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                com.vahitkeskin.fencecalculator.ui.components.PremiumGlassCard {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(viewModel.strings.preparingPdfReport, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        pdfFileForPreview?.let { file ->
            com.vahitkeskin.fencecalculator.ui.components.PdfPreviewDialog(
                file,
                viewModel
            ) { pdfFileForPreview = null }
        }
    }
}

@AppPreviews
@Composable
fun CalculationsScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }
    val navController = rememberNavController()
    
    FenceCalculatorTheme {
        CalculationsScreen(
            viewModel = viewModel,
            navController = navController,
            onPremiumClick = {}
        )
    }
}
