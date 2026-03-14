package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vahitkeskin.fencecalculator.util.QrGenerator
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.draw.rotate
import com.vahitkeskin.fencecalculator.R
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.PremiumGlassCard
import com.vahitkeskin.fencecalculator.ui.components.DnsWarningCard
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: CalculatorViewModel
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var showLargeQr by remember { mutableStateOf(false) }
    var isShareExpanded by remember { mutableStateOf(false) }
    
    val appLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val qrBitmap = remember(appLink) { QrGenerator.generateQrCode(appLink, 512) }

    LaunchedEffect(isShareExpanded) {
        if (isShareExpanded) {
            // Give some time for the expanding animation to progress
            kotlinx.coroutines.delay(300)
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    fun shareBitmap(bitmap: android.graphics.Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "app_qr_code.png")
            val stream = FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, viewModel.strings.shareReportTitle))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scrollToTop.collect { route ->
            if (route == "profile_tab") {
                listState.animateScrollToItem(0)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            overscrollEffect = null
        ) {
            item {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            viewModel.strings.navProfile.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                // Profile Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("personal_info") },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val initials = androidx.compose.runtime.remember(viewModel.companyName) {
                        val words = viewModel.companyName.trim().split("\\s+".toRegex())
                        when {
                            words.isEmpty() || viewModel.companyName.isBlank() -> null
                            words.size == 1 -> words.first().take(2).uppercase()
                            else -> {
                                val first = words.first().firstOrNull()?.uppercase() ?: ""
                                val last = words.last().firstOrNull()?.uppercase() ?: ""
                                "$first$last"
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (initials != null) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (viewModel.companyName.isBlank()) viewModel.strings.companyName else viewModel.companyName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.companyName.isBlank()) MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.3f
                        ) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = viewModel.strings.personalInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                // Settings Section
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = viewModel.strings.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    /*
                    // TODO: İstediğim zaman aktif edebileyim - 50 sınırlaması ve premium
                    PremiumGlassCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                                Spacer(Modifier.width(12.dp))
                                Text("Premium Status", fontWeight = FontWeight.Bold)
                            }
                            Switch(
                                checked = viewModel.isPremium,
                                onCheckedChange = { viewModel.onPremiumToggle(it) }
                            )
                        }
                    }
                    */

                    ProfileMenuItem(
                        icon = Icons.Default.Badge,
                        title = viewModel.strings.personalInfo,
                        subtitle = viewModel.strings.companyInfoTitle,
                        onClick = { navController.navigate("personal_info") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = viewModel.strings.settings,
                        subtitle = viewModel.strings.calculationParametersDesc,
                        onClick = { navController.navigate("settings_detail") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = viewModel.strings.about,
                        subtitle = viewModel.strings.licensesDesc,
                        onClick = { navController.navigate("about") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val appName = viewModel.strings.appName
                    val shareMessage = String.format(viewModel.strings.shareAppMessage, appName)
                    
                    PremiumGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isShareExpanded = !isShareExpanded },
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = viewModel.strings.share,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                val rotateChevron by animateFloatAsState(
                                    targetValue = if (isShareExpanded) 90f else 0f,
                                    label = "ChevronRotation"
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.rotate(rotateChevron)
                                )
                            }
                            
                            AnimatedVisibility(
                                visible = isShareExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        qrBitmap?.let { bitmap ->
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "App QR Code",
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.White)
                                                    .padding(8.dp)
                                                    .clickable { showLargeQr = true }
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(24.dp))
                                        
                                        IconButton(
                                            onClick = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, viewModel.strings.shareAppTitle))
                                            },
                                            modifier = Modifier
                                                .size(120.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.IosShare,
                                                contentDescription = "System Share",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (showLargeQr && qrBitmap != null) {
                        Dialog(
                            onDismissRequest = { showLargeQr = false },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .clickable { showLargeQr = false },
                                contentAlignment = Alignment.Center
                            ) {
                                PremiumGlassCard(
                                    modifier = Modifier.padding(32.dp),
                                    cornerRadius = 24.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "Large QR Code",
                                            modifier = Modifier
                                                .size(280.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White)
                                                .padding(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text(
                                            text = viewModel.strings.appName,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = viewModel.strings.shareAppTitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        Button(
                                            onClick = { shareBitmap(qrBitmap) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(16.dp)
                                        ) {
                                            Icon(Icons.Default.Share, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text(viewModel.strings.shareAppTitle)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }

            item {
                val packageInfo = remember {
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    } catch (e: Exception) {
                        null
                    }
                }
                val versionName = packageInfo?.versionName ?: "1.0"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_professional),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .size(60.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = viewModel.strings.appName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "v$versionName",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(128.dp)) } // Bottom bar padding
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
@AppPreviews
@Composable
fun ProfileScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }
    val navController = rememberNavController()
    
    FenceCalculatorTheme {
        ProfileScreen(navController = navController, viewModel = viewModel)
    }
}
