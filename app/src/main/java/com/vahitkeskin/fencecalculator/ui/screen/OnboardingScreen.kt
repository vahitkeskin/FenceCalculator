package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.PremiumGlassCard
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: CalculatorViewModel,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val strings = viewModel.strings
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()
        
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPage(
                    title = when (page) {
                        0 -> strings.onboardingWelcomeTitle
                        1 -> strings.onboardingStep1Title
                        2 -> strings.onboardingStep2Title
                        else -> strings.onboardingStep3Title
                    },
                    description = when (page) {
                        0 -> strings.onboardingWelcomeDesc
                        1 -> strings.onboardingStep1Desc
                        2 -> strings.onboardingStep2Desc
                        else -> strings.onboardingStep3Desc
                    },
                    icon = when (page) {
                        0 -> Icons.Default.WavingHand
                        1 -> Icons.Default.GridOn
                        2 -> Icons.Default.Extension
                        else -> Icons.Default.Share
                    },
                    primaryColor = primaryColor,
                    onBackgroundColor = onBackgroundColor
                )
            }

            // Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { idx ->
                        val active = pagerState.currentPage == idx
                        Box(
                            modifier = Modifier
                                .size(if (active) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (active) primaryColor else onBackgroundColor.copy(alpha = 0.2f))
                        )
                    }
                }

                // Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pagerState.currentPage < 3) {
                        TextButton(onClick = onFinish) {
                            Text(strings.skip, color = onBackgroundColor.copy(alpha = 0.5f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(strings.next)
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text(strings.onboardingFinish, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    icon: ImageVector,
    primaryColor: Color,
    onBackgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PremiumGlassCard(
            modifier = Modifier.size(200.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = primaryColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = onBackgroundColor,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = onBackgroundColor.copy(alpha = 0.7f),
            lineHeight = 24.sp
        )
    }
}
