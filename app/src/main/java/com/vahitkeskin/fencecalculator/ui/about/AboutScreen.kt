package com.vahitkeskin.fencecalculator.ui.about

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.fencecalculator.R
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.util.NavigationUtils.safePopBackStack
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutScreen(navController: NavController, viewModel: CalculatorViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    var isDeveloperExpanded by rememberSaveable { mutableStateOf(false) }
    
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0"
    
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text(viewModel.strings.about, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.safePopBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = viewModel.strings.back)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            MeshBackground()
            
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                overscrollEffect = null
            ) {
                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    // App Icon & Name
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_professional),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    Text(
                        text = viewModel.strings.appName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                item {
                    Text(
                        text = "v$versionName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
                
                item {
                    DeveloperCard(
                        viewModel = viewModel,
                        isExpanded = isDeveloperExpanded,
                        onToggleExpand = { expanded ->
                            isDeveloperExpanded = expanded
                            if (expanded) {
                                coroutineScope.launch {
                                    // Animasyonun tamamlanmasını beklemeden veya paralel olarak kaydır
                                    // Yaklaşık olarak kartın ortasını ekranın ortasına getir
                                    val scrollOffset = with(density) { 
                                        (-(screenHeight / 2) + 250.dp).roundToPx()
                                    }
                                    lazyListState.animateScrollToItem(
                                        index = 6, 
                                        scrollOffset = scrollOffset
                                    )
                                }
                            }
                        }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    AboutInfoItem(
                        icon = Icons.Default.BugReport,
                        title = viewModel.strings.feedback,
                        value = viewModel.strings.feedbackDesc,
                        onClick = { uriHandler.openUri("https://play.google.com/store/apps/details?id=com.vahitkeskin.fencecalculator") }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(12.dp)) }
                
                item {
                    AboutInfoItem(
                        icon = Icons.Default.Description,
                        title = viewModel.strings.licenses,
                        value = viewModel.strings.licensesDesc,
                        onClick = { uriHandler.openUri("https://github.com/vahitkeskin/FenceCalculator") }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(48.dp)) }
                
                item {
                    val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
                    
                    Text(
                        text = String.format(viewModel.strings.allRightsReserved, currentYear, viewModel.strings.appName),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@AppPreviews
@Composable
fun AboutScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }
    val navController = rememberNavController()
    FenceCalculatorTheme {
        AboutScreen(navController = navController, viewModel = viewModel)
    }
}
