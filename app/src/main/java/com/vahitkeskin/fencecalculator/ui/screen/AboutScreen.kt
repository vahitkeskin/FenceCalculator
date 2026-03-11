package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.PremiumGlassCard
import com.vahitkeskin.fencecalculator.R
import com.vahitkeskin.fencecalculator.util.NavigationUtils.safePopBackStack


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutScreen(navController: NavController, viewModel: CalculatorViewModel) {
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0"

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
                // App Description
                PremiumGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = viewModel.strings.aboutAppTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(viewModel.strings.appDescriptionLong, viewModel.strings.appName),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item {
                // Info Items
                AboutInfoItem(
                    icon = Icons.Default.Person,
                    title = viewModel.strings.developerLabel,
                    value = "Vahit Keskin"
                )
            }
            
            item { Spacer(modifier = Modifier.height(12.dp)) }
            
            item {
                AboutInfoItem(
                    icon = Icons.Default.BugReport,
                    title = viewModel.strings.feedback,
                    value = viewModel.strings.feedbackDesc
                )
            }
            
            item { Spacer(modifier = Modifier.height(12.dp)) }
            
            item {
                AboutInfoItem(
                    icon = Icons.Default.Description,
                    title = viewModel.strings.licenses,
                    value = viewModel.strings.licensesDesc
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

@Composable
fun AboutInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    PremiumGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
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
