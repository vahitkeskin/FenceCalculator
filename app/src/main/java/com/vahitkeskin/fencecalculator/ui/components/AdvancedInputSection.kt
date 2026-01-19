package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch

@Composable
fun AdvancedInputSection(
    lengthValue: String, onLengthChange: (String) -> Unit,
    heightValue: String, onHeightChange: (String) -> Unit,
    spacingValue: String, onSpacingChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CompactInput("Arazi Uzunluğu (m)", lengthValue, onLengthChange, Icons.Filled.Straighten, Modifier.fillMaxWidth(), focusManager)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CompactInput("Çit Yüksekliği (m)", heightValue, onHeightChange, Icons.Filled.Height, Modifier.weight(1f), focusManager)
                CompactInput("Direk Aralığı (m)", spacingValue, onSpacingChange, Icons.Filled.Settings, Modifier.weight(1f), focusManager, true)
            }
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
    isLast: Boolean = false
) {
    // 1. Scroll İsteği İçin Gerekli Değişkenler
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                // 2. Inputa tıklandığında (Focus olduğunda) ekrana getir
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            singleLine = true,
            leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if(isLast) ImeAction.Done else ImeAction.Next),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun SmartSettingsInput(
    label: String,
    value: String,
    defaultValue: String,
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if(isChanged) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha=0.3f),
                focusedLabelColor = if(isChanged) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
        )
        AnimatedVisibility(visible = isChanged, enter = fadeIn(), exit = fadeOut()) {
            TextButton(
                onClick = { onValueChange(defaultValue) },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp).align(Alignment.End)
            ) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(12.dp), MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(4.dp))
                Text("Varsayılan: $defaultValue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}