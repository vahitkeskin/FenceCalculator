package com.vahitkeskin.fencecalculator.ui.about

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vahitkeskin.fencecalculator.R
import com.vahitkeskin.fencecalculator.ui.components.PremiumGlassCard
import com.vahitkeskin.fencecalculator.ui.icons.*
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.util.DataStoreManager

@Composable
fun DeveloperCard(
    viewModel: CalculatorViewModel,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val photo = painterResource(id = R.drawable.personel)

    val animationSpec = androidx.compose.animation.core.tween<Float>(
        durationMillis = 300,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
    )
    val dpAnimationSpec = androidx.compose.animation.core.tween<androidx.compose.ui.unit.Dp>(
        durationMillis = 300,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
    )

    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
            .clickable { onToggleExpand(!isExpanded) },
        cornerRadius = 16.dp
    ) {
        Box(
            modifier = Modifier
                .padding(if (isExpanded) 20.dp else 16.dp)
                .fillMaxWidth()
        ) {
            // Shared Animation Values
            val bias by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isExpanded) 0f else -1f,
                animationSpec = animationSpec,
                label = "horizontalBias"
            )
            val photoSize by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isExpanded) 80.dp else 40.dp,
                animationSpec = dpAnimationSpec,
                label = "photoSize"
            )
            val photoY by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isExpanded) 24.dp else 0.dp,
                animationSpec = dpAnimationSpec,
                label = "photoY"
            )

            val textY by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isExpanded) photoY + photoSize + 16.dp else 0.dp,
                animationSpec = dpAnimationSpec,
                label = "textY"
            )
            val textXOffset by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isExpanded) 0.dp else photoSize + 16.dp,
                animationSpec = dpAnimationSpec,
                label = "textXOffset"
            )

            // 1. Photo (Animates size, bias and Y offset)
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.BiasAlignment(bias, -1f))
                    .offset(y = photoY)
                    .size(photoSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = photo,
                    contentDescription = "Vahit Keskin",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 2. Text Column (Name & Title)
            Column(
                modifier = Modifier
                    .align(androidx.compose.ui.BiasAlignment(bias, -1f))
                    .offset(x = textXOffset, y = textY)
                    .then(if (isExpanded) Modifier.fillMaxWidth() else Modifier),
                horizontalAlignment = if (isExpanded) Alignment.CenterHorizontally else Alignment.Start
            ) {
                if (!isExpanded) {
                    Text(
                        text = "Geliştirici",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text(
                    text = "Vahit KESKİN" + if (!isExpanded) " (Bana Ulaşın)" else "",
                    style = if (isExpanded) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = if (isExpanded) androidx.compose.ui.text.style.TextAlign.Center else null
                )
                if (isExpanded) {
                    Text(
                        text = "Bilgisayar Mühendisi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // 3. Arrow Icon
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 4. Social Links (Only in expanded state, below the text)
            Column(
                modifier = Modifier.padding(top = if (isExpanded) textY + 60.dp else 0.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isExpanded,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialLinkRow(
                            icon = Icons.Default.Phone,
                            label = "+90 551 044 43 06",
                            onClick = { uriHandler.openUri("tel:+905510444306") },
                            color = Color(0xFF25D366)
                        )
                        SocialLinkRow(
                            icon = Icons.Default.Email,
                            label = "vahitkeskin07@gmail.com",
                            onClick = { uriHandler.openUri("mailto:vahitkeskin07@gmail.com") },
                            color = Color(0xFFEA4335)
                        )
                        SocialLinkRow(
                            icon = Icons.Filled.Website,
                            label = "vahitkeskin.github.io/iamvahitkeskin/",
                            onClick = { uriHandler.openUri("https://vahitkeskin.github.io/iamvahitkeskin/") },
                            color = Color(0xFF4285F4)
                        )
                        SocialLinkRow(
                            icon = Icons.Filled.LinkedIn,
                            label = "linkedin.com/in/vahit-keskin/",
                            onClick = { uriHandler.openUri("https://www.linkedin.com/in/vahit-keskin/") },
                            color = Color(0xFF0A66C2)
                        )
                        SocialLinkRow(
                            icon = Icons.Filled.Github,
                            label = "github.com/vahitkeskin",
                            onClick = { uriHandler.openUri("https://github.com/vahitkeskin") },
                            color = Color(0xFF181717)
                        )
                        SocialLinkRow(
                            icon = Icons.Filled.PlayStore,
                            label = "play.google.com/vahitkeskin",
                            onClick = { uriHandler.openUri("https://play.google.com/store/apps/developer?id=vahitkeskin") },
                            color = Color(0xFF34A853)
                        )
                        SocialLinkRow(
                            icon = Icons.Filled.Instagram,
                            label = "instagram.com/keskin.vahit/",
                            onClick = { uriHandler.openUri("https://www.instagram.com/keskin.vahit/") },
                            color = Color(0xFFE4405F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SocialLinkRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SocialFab(
            icon = icon,
            onClick = onClick,
            color = color
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SocialFab(
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@AppPreviews
@Composable
fun DeveloperCardPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }
    var isExpanded by remember { mutableStateOf(false) }
    FenceCalculatorTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            DeveloperCard(
                viewModel = viewModel,
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = it }
            )
        }
    }
}
