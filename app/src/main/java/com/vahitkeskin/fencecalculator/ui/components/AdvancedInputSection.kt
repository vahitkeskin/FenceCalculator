package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import com.vahitkeskin.fencecalculator.util.centerOnFocus
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme

@Composable
fun AdvancedInputSection(
    labelText: String,
    lengthValue: String,
    onLengthChange: (String) -> Unit,
    onClear: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompactInput(
                label = labelText,
                value = lengthValue,
                onValueChange = onLengthChange,
                icon = Icons.Filled.Straighten,
                modifier = Modifier.fillMaxWidth(),
                focusManager = focusManager,
                isLast = true,
                onClear = onClear
            )

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    focusManager: androidx.compose.ui.focus.FocusManager,
    isLast: Boolean = false,
    usageCount: Int = 0,
    isPremium: Boolean = false,
    usageLimitInfo: String = "",
    premiumRequiredInfo: String = "",
    onClear: () -> Unit = {}
) {
    // TODO: İstediğim zaman aktif edebileyim - 50 sınırlaması ve premium
    // val isLimitReached = !isPremium && usageCount >= 50
    val isLimitReached = false
    // val bringIntoViewRequester = remember { BringIntoViewRequester() }
    // val coroutineScope = rememberCoroutineScope()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .centerOnFocus(),
            label = {
                Text(
                    label,
                    color = onSurfaceColor.copy(alpha = 0.5f)
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    icon,
                    null,
                    Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (androidx.compose.foundation.isSystemInDarkTheme())
                                    Color.White.copy(alpha = 0.15f)
                                else
                                    Color.Black.copy(alpha = 0.08f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .padding(5.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (androidx.compose.foundation.isSystemInDarkTheme())
                                Color.White.copy(alpha = 0.6f)
                            else
                                Color.Black.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (isLast) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = onSurfaceColor,
                unfocusedTextColor = onSurfaceColor,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                disabledBorderColor = Color(0xFFE2E8F0),
                disabledTextColor = onSurfaceColor.copy(alpha = 0.3f),
                disabledLeadingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
            enabled = true // Always enabled, but blurred results if limit reached
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            if (usageLimitInfo.isNotEmpty()) {
                Text(
                    text = usageLimitInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLimitReached) MaterialTheme.colorScheme.error else onSurfaceColor.copy(
                        alpha = 0.5f
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (isLimitReached && !isPremium) {
                Text(
                    text = premiumRequiredInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SmartSettingsInput(
    label: String,
    value: String,
    defaultValue: String,
    defaultLabel: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val isChanged = value != defaultValue
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isChanged) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedLabelColor = if (isChanged) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
        )
        AnimatedVisibility(
            visible = isChanged,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.End)
        ) {
            TextButton(
                onClick = { onValueChange(defaultValue) },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    null,
                    Modifier.size(12.dp),
                    MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    defaultLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@AppPreviews
@Composable
fun AdvancedInputSectionPreview() {
    FenceCalculatorTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AdvancedInputSection(
                labelText = "Arazi Uzunluğu (m)",
                lengthValue = "150",
                onLengthChange = {},
                onClear = {}
            )
        }
    }
}