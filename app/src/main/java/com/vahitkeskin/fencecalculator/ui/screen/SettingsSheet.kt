package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.components.SmartSettingsInput
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel

@Composable
fun SettingsSheetContent(viewModel: CalculatorViewModel, onDismiss: () -> Unit) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text("Hesaplama Parametreleri", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Değerleri değiştirdiğinizde anlık olarak hesaplanır.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Grup: Tel Ağırlık
        SettingsGroupTitle("Kafes Tel Ağırlık Hesabı", Icons.Filled.Scale)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Formül: (Kalınlık² * Çarpan) / Göz Aralığı", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
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
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(24.dp))
        SettingsGroupTitle("Payanda Kurulumu", Icons.Filled.ChangeHistory)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Sıklık (Direk)", viewModel.strutIntervalInput, CalculatorViewModel.Defaults.STRUT_INTERVAL, viewModel::onStrutIntervalChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Adet (Her Sefer)", viewModel.strutCountInput, CalculatorViewModel.Defaults.STRUT_COUNT, viewModel::onStrutCountChange, Modifier.weight(1f), focusManager)
        }

        // Grup: Rulo
        Spacer(modifier = Modifier.height(24.dp))
        SettingsGroupTitle("Tel Rulo Özellikleri", Icons.Filled.GridOn)
        SmartSettingsInput("Kafes Tel Top Uzunluğu (m)", viewModel.meshRollLengthInput, CalculatorViewModel.Defaults.MESH_ROLL, viewModel::onMeshRollLengthChange, Modifier.fillMaxWidth(), focusManager)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmartSettingsInput("Dikenli Tel (Sıra)", viewModel.barbedWireRowsInput, CalculatorViewModel.Defaults.BARBED_ROWS, viewModel::onBarbedWireRowsChange, Modifier.weight(1f), focusManager)
            SmartSettingsInput("Dikenli Top (m)", viewModel.barbedWireRollLengthInput, CalculatorViewModel.Defaults.BARBED_ROLL, viewModel::onBarbedWireRollLengthChange, Modifier.weight(1f), focusManager)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
            Text("Kaydet ve Kapat", fontSize = 16.sp)
        }
    }
}

@Composable
fun SettingsGroupTitle(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}