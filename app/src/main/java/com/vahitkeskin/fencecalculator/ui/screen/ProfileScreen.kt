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
import androidx.navigation.NavController
import com.vahitkeskin.fencecalculator.R
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.PremiumGlassCard
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
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            overscrollEffect = null
        ) {
            item {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "PROFİL",
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
                        text = if (viewModel.companyName.isBlank()) "Firma Adı Girin" else viewModel.companyName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.companyName.isBlank()) MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.3f
                        ) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Profil Ayarları",
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
                        text = "Hesap ve Uygulama",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Badge,
                        title = "Kişisel Bilgiler",
                        subtitle = "Firma adı, IBAN ve iletişim bilgileri",
                        onClick = { navController.navigate("personal_info") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Ayarlar",
                        subtitle = "Hesaplama parametrelerini ve görünümü düzenle",
                        onClick = { navController.navigate("settings_detail") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "Hakkında",
                        subtitle = "Uygulama versiyonu ve bilgiler",
                        onClick = { navController.navigate("about") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val appName = stringResource(id = R.string.app_name)
                    val shareMessage = """
                        Çit ve örgü tel ihtiyaçlarınıza profesyonel çözüm!
                        $appName uygulamasını hemen indirin:
                        https://play.google.com/store/apps/details?id=com.vahitkeskin.fencecalculator
                    """.trimIndent()
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Share,
                        title = "Paylaş",
                        subtitle = "Uygulamayı arkadaşlarınla paylaş",
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Paylaş"))
                        }
                    )
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
                        text = stringResource(id = R.string.app_name).uppercase(),
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

            item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom bar padding
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
