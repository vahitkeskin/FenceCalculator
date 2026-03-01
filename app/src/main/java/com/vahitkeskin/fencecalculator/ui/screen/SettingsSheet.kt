package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.components.SmartSettingsInput
import com.vahitkeskin.fencecalculator.ui.viewmodel.AppTheme
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel

@Composable
fun SettingsSheetContent(viewModel: CalculatorViewModel, onDismiss: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val onBackgroundColor = MaterialTheme.colorScheme.onSurface 

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text("Hesaplama Parametreleri", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = onBackgroundColor)
        Text("Değerleri değiştirdiğinizde anlık olarak hesaplanır.", style = MaterialTheme.typography.bodyMedium, color = onBackgroundColor.copy(alpha = 0.6f))

        Spacer(modifier = Modifier.height(24.dp))

        // Grup: Görünüm (Tema)
        SettingsGroupTitle("Görünüm Ayarları", Icons.Filled.Palette)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeToggleButton(
                text = "Açık",
                icon = Icons.Filled.LightMode,
                isSelected = viewModel.currentTheme == AppTheme.LIGHT,
                onClick = { viewModel.onThemeChange(AppTheme.LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeToggleButton(
                text = "Koyu",
                icon = Icons.Filled.DarkMode,
                isSelected = viewModel.currentTheme == AppTheme.DARK,
                onClick = { viewModel.onThemeChange(AppTheme.DARK) },
                modifier = Modifier.weight(1f)
            )
            ThemeToggleButton(
                text = "Sistem",
                icon = Icons.Filled.SettingsSuggest,
                isSelected = viewModel.currentTheme == AppTheme.SYSTEM,
                onClick = { viewModel.onThemeChange(AppTheme.SYSTEM) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = onBackgroundColor.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Grup: Temel Ölçüler
        SettingsGroupTitle("Çit ve Direk Ölçüleri", Icons.Filled.Straighten)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Çit Yüksekliği (m)", viewModel.fenceHeightInput, CalculatorViewModel.Defaults.HEIGHT, viewModel::onFenceHeightChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Direk Aralığı (m)", viewModel.poleSpacingInput, CalculatorViewModel.Defaults.SPACING, viewModel::onPoleSpacingChange, Modifier.weight(1f), focusManager)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = onBackgroundColor.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Grup: Tel Ağırlık
        SettingsGroupTitle("Kafes Tel Ağırlık Hesabı", Icons.Filled.Scale)
        Card(
            colors = CardDefaults.cardColors(containerColor = onBackgroundColor.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Formül: (Kalınlık² * Çarpan) / Göz Aralığı", style = MaterialTheme.typography.labelSmall, color = onBackgroundColor.copy(alpha = 0.5f))
            }
        }
        SmartSettingsInput("Tel Kalınlığı (mm)", viewModel.wireThicknessInput, CalculatorViewModel.Defaults.WIRE_THICKNESS, viewModel::onWireThicknessChange, Modifier.fillMaxWidth(), focusManager)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Göz Aralığı (cm)", viewModel.meshEyeInput, CalculatorViewModel.Defaults.MESH_EYE, viewModel::onMeshEyeChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Sabit Çarpan", viewModel.weightConstantInput, CalculatorViewModel.Defaults.WEIGHT_CONSTANT, viewModel::onWeightConstantChange, Modifier.weight(1f), focusManager)
        }

        // Grup: Payanda
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = onBackgroundColor.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))
        SettingsGroupTitle("Payanda Kurulumu", Icons.Filled.ChangeHistory)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Sıklık (Direk)", viewModel.strutIntervalInput, CalculatorViewModel.Defaults.STRUT_INTERVAL, viewModel::onStrutIntervalChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Adet (Her Sefer)", viewModel.strutCountInput, CalculatorViewModel.Defaults.STRUT_COUNT, viewModel::onStrutCountChange, Modifier.weight(1f), focusManager)
        }

        // Grup: Rulo
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = onBackgroundColor.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))
        SettingsGroupTitle("Tel Rulo Özellikleri", Icons.Filled.GridOn)
        SmartSettingsInput("Kafes Tel Top Uzunluğu (m)", viewModel.meshRollLengthInput, CalculatorViewModel.Defaults.MESH_ROLL, viewModel::onMeshRollLengthChange, Modifier.fillMaxWidth(), focusManager)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Dikenli Tel (Sıra)", viewModel.barbedWireRowsInput, CalculatorViewModel.Defaults.BARBED_ROWS, viewModel::onBarbedWireRowsChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Dikenli Top (m)", viewModel.barbedWireRollLengthInput, CalculatorViewModel.Defaults.BARBED_ROLL, viewModel::onBarbedWireRollLengthChange, Modifier.weight(1f), focusManager)
        }

        // Grup: Gelişmiş (Advanced)
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = onBackgroundColor.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))
        SettingsGroupTitle("Gelişmiş Parametreler", Icons.Filled.AdminPanelSettings)
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Direk Boyu (m)", viewModel.poleLengthInput, "2.4", viewModel::onPoleLengthChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Boru Boyu (m)", viewModel.pipeLengthInput, "6.0", viewModel::onPipeLengthChange, Modifier.weight(1f), focusManager)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Gergi Katsayı", viewModel.tensionFactorInput, "6.66", viewModel::onTensionFactorChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Bağlama Katsayı", viewModel.bindingFactorInput, "3.0", viewModel::onBindingFactorChange, Modifier.weight(1f), focusManager)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Çimento Katsayı", viewModel.cementFactorInput, "6.0", viewModel::onCementFactorChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Beton Katsayı", viewModel.concreteFactorInput, "30.0", viewModel::onConcreteFactorChange, Modifier.weight(1f), focusManager)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("Kaydet ve Kapat", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun SettingsGroupTitle(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ThemeToggleButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else onSurface.copy(alpha = 0.05f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else onSurface.copy(alpha = 0.6f),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}