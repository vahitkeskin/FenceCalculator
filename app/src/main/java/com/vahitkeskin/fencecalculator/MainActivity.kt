package com.vahitkeskin.fencecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import java.text.DecimalFormat
import kotlin.math.sin

// --- MODEL ---
data class CalculationItem(
    val id: String,
    val title: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val totalCost: Double,
    val icon: ImageVector,
    val color: Color
)

// --- VIEWMODEL ---
class CalculatorViewModel : ViewModel() {

    // --- TEMEL GİRDİLER ---
    var totalLengthInput by mutableStateOf("300")
        private set
    var poleSpacingInput by mutableStateOf("3.5")
        private set

    // --- GELİŞMİŞ AYARLAR (Varsayılan Değerler) ---
    var strutIntervalInput by mutableStateOf("15") // Kaç direkte bir payanda?
        private set
    var strutCountInput by mutableStateOf("2")     // Her aralıkta kaç payanda?
        private set
    var meshRollLengthInput by mutableStateOf("20") // Kafes tel topu kaç metre?
        private set
    var barbedWireRowsInput by mutableStateOf("3")  // Dikenli tel kaç sıra?
        private set
    var barbedWireRollLengthInput by mutableStateOf("250") // Dikenli tel topu kaç metre? (Genelde 250m olur, güncelledim)
        private set

    // Fiyat Haritası
    private var priceMap = mutableMapOf<String, String>()

    // Sonuçlar
    var results by mutableStateOf<List<CalculationItem>>(emptyList())
        private set
    var grandTotalCost by mutableStateOf(0.0)
        private set

    init {
        calculateValues()
    }

    // --- GÜNCELLEME FONKSİYONLARI ---
    fun onTotalLengthChange(v: String) { if(isValid(v)) { totalLengthInput = v; calculateValues() } }
    fun onPoleSpacingChange(v: String) { if(isValid(v)) { poleSpacingInput = v; calculateValues() } }

    // Ayar Güncellemeleri
    fun onStrutIntervalChange(v: String) { if(isValid(v)) { strutIntervalInput = v; calculateValues() } }
    fun onStrutCountChange(v: String) { if(isValid(v)) { strutCountInput = v; calculateValues() } }
    fun onMeshRollLengthChange(v: String) { if(isValid(v)) { meshRollLengthInput = v; calculateValues() } }
    fun onBarbedWireRowsChange(v: String) { if(isValid(v)) { barbedWireRowsInput = v; calculateValues() } }
    fun onBarbedWireRollLengthChange(v: String) { if(isValid(v)) { barbedWireRollLengthInput = v; calculateValues() } }

    fun onPriceChange(id: String, v: String) {
        val s = v.replace(',', '.')
        if (isValid(s)) { priceMap[id] = s; calculateValues() }
    }

    private fun isValid(input: String): Boolean {
        if (input.isEmpty()) return true
        return input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1
    }

    // --- HESAPLAMA MOTORU ---
    private fun calculateValues() {
        val length = totalLengthInput.toDoubleOrNull() ?: 0.0
        val spacing = poleSpacingInput.toDoubleOrNull() ?: 0.0

        // Ayar değişkenlerini sayıya çevir
        val strutFreq = strutIntervalInput.toDoubleOrNull() ?: 15.0
        val strutCnt = strutCountInput.toDoubleOrNull() ?: 2.0
        val meshLen = meshRollLengthInput.toDoubleOrNull() ?: 20.0
        val barbedRows = barbedWireRowsInput.toDoubleOrNull() ?: 3.0
        val barbedLen = barbedWireRollLengthInput.toDoubleOrNull() ?: 250.0

        if (length == 0.0 || spacing <= 0.0) {
            results = emptyList(); grandTotalCost = 0.0; return
        }

        // 1. Direk
        val direkSayisi = length / spacing

        // 2. Payanda (Dinamik)
        // Her 'strutFreq' direkte bir 'strutCnt' adet
        val payandaSayisi = (direkSayisi / strutFreq) * strutCnt

        // 3. Kafes Tel (Dinamik Top Uzunluğu)
        val kafesTopSayisi = length / meshLen

        // 4. Dikenli Tel (Diken Sırası * Uzunluk / Top Metrajı)
        val dikenliTelTopSayisi = (barbedRows * length) / barbedLen

        // 5. Gergi Teli (Standart formül olarak kalabilir veya ayara bağlanabilir, şimdilik sabit)
        val gergiTeli = length / 6.66
        val baglamaTeli = gergiTeli / 3.0

        fun getP(id: String) = priceMap[id]?.toDoubleOrNull() ?: 0.0

        val list = listOf(
            createItem("direk", "Direk", "Her $spacing m'de bir", direkSayisi, "Adet", Icons.Filled.Straighten, Color(0xFF3F51B5), ::getP),
            createItem("payanda", "Payanda", "Her $strutFreq direkte $strutCnt adet", payandaSayisi, "Adet", Icons.Filled.ChangeHistory, Color(0xFF9C27B0), ::getP),
            createItem("kafes", "Kafes Tel", "$meshLen m'lik top", kafesTopSayisi, "Top", Icons.Filled.GridOn, Color(0xFF009688), ::getP),
            createItem("diken", "Dikenli Tel", "${barbedRows.toInt()} Sıra ($barbedLen m/top)", dikenliTelTopSayisi, "Top", Icons.Filled.Warning, Color(0xFFD32F2F), ::getP),
            createItem("gergi", "Gergi Teli", "Uzunluk / 6.66", gergiTeli, "Kg", Icons.Filled.LinearScale, Color(0xFFFF9800), ::getP),
            createItem("baglama", "Bağlama Teli", "Gergi Telinin 1/3'ü", baglamaTeli, "Kg", Icons.Filled.AllInclusive, Color(0xFF795548), ::getP)
        )

        results = list
        grandTotalCost = list.sumOf { it.totalCost }
    }

    private fun createItem(id: String, t: String, d: String, q: Double, u: String, i: ImageVector, c: Color, p: (String)->Double) =
        CalculationItem(id, t, d, q, u, p(id), q * p(id), i, c)

    fun getPriceString(id: String): String = priceMap[id] ?: ""
}

// --- ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FenceCalculatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FenceCalculatorScreen()
                }
            }
        }
    }
}

// --- UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FenceCalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    // Bottom Sheet Kontrolü
    var showSettingsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Çit Maliyet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Hesaplayıcı", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    // AYARLAR BUTONU
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Ayarlar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            AnimatedWaveBottomBar(totalCost = viewModel.grandTotalCost)
        }
    ) { innerPadding ->

        // ANA LİSTE
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ana Girdiler (Uzunluk ve Ara Mesafe)
            item {
                AdvancedInputSection(
                    lengthValue = viewModel.totalLengthInput,
                    onLengthChange = viewModel::onTotalLengthChange,
                    spacingValue = viewModel.poleSpacingInput,
                    onSpacingChange = viewModel::onPoleSpacingChange
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("GİDER KALEMLERİ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showSettingsSheet = true }) {
                        Text("Parametreleri Düzenle", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            items(viewModel.results, key = { it.id }) { item ->
                val rawPrice = viewModel.getPriceString(item.id)
                SwapLayoutResultRow(
                    item = item,
                    currentPriceInput = rawPrice,
                    onPriceChange = { newPrice -> viewModel.onPriceChange(item.id, newPrice) }
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        // BOTTOM SHEET (AYARLAR PANELİ)
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                SettingsSheetContent(viewModel = viewModel) {
                    // Kapatma isteği gelirse (örneğin Tamam butonu)
                    showSettingsSheet = false
                }
            }
        }
    }
}

// --- SETTINGS SHEET UI (YENİ EKLENEN KISIM) ---
@Composable
fun SettingsSheetContent(viewModel: CalculatorViewModel, onDismiss: () -> Unit) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding() // Klavye altında kalmasın
    ) {
        Text(
            text = "Hesaplama Parametreleri",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Standart değerleri projenize göre özelleştirin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Grup 1: Payanda Ayarları
        SettingsGroupTitle("Payanda Kurulumu", Icons.Filled.ChangeHistory)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingsInput(
                label = "Sıklık (Direk)",
                value = viewModel.strutIntervalInput,
                onValueChange = viewModel::onStrutIntervalChange,
                modifier = Modifier.weight(1f),
                focusManager = focusManager
            )
            SettingsInput(
                label = "Adet (Her Sefer)",
                value = viewModel.strutCountInput,
                onValueChange = viewModel::onStrutCountChange,
                modifier = Modifier.weight(1f),
                focusManager = focusManager
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grup 2: Tel Özellikleri
        SettingsGroupTitle("Tel Özellikleri", Icons.Filled.GridOn)
        SettingsInput(
            label = "Kafes Tel Top Uzunluğu (m)",
            value = viewModel.meshRollLengthInput,
            onValueChange = viewModel::onMeshRollLengthChange,
            modifier = Modifier.fillMaxWidth(),
            focusManager = focusManager
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingsInput(
                label = "Dikenli Tel (Sıra)",
                value = viewModel.barbedWireRowsInput,
                onValueChange = viewModel::onBarbedWireRowsChange,
                modifier = Modifier.weight(1f),
                focusManager = focusManager
            )
            SettingsInput(
                label = "Dikenli Top (m)",
                value = viewModel.barbedWireRollLengthInput,
                onValueChange = viewModel::onBarbedWireRollLengthChange,
                modifier = Modifier.weight(1f),
                focusManager = focusManager
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Kaydet ve Kapat", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
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

@Composable
fun SettingsInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha=0.3f)
        )
    )
}

// --- DİĞER BİLEŞENLER (Önceki kodlardan aynı) ---

@Composable
fun SwapLayoutResultRow(item: CalculationItem, currentPriceInput: String, onPriceChange: (String) -> Unit) {
    val df = DecimalFormat("#,##0.##")
    val currencyFormat = DecimalFormat("#,##0.00")
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(item.color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) { Text("${df.format(item.quantity)} ${item.unit}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = currentPriceInput, onValueChange = onPriceChange, modifier = Modifier.fillMaxWidth(), label = { Text("Birim Fiyat (₺)", style = MaterialTheme.typography.bodySmall) }, placeholder = { Text("0", color = Color.LightGray) }, textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = item.color, unfocusedBorderColor = Color.Transparent))
                }
            }
            Surface(color = item.color.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TOPLAM", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = item.color.copy(alpha = 0.8f))
                    Text("${currencyFormat.format(item.totalCost)} ₺", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = item.color)
                }
            }
        }
    }
}

@Composable
fun AnimatedWaveBottomBar(totalCost: Double) {
    val animatedTotalCost by animateFloatAsState(
        targetValue = totalCost.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "costAnim"
    )
    val currencyFormat = DecimalFormat("#,##0.00")

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "wavePhase"
    )

    val backgroundColor = Color(0xFF1E1E1E)
    val waveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val waveAmplitude = 15.dp.toPx()

            val path = Path().apply {
                moveTo(0f, height)
                lineTo(0f, height * 0.5f)
                for (x in 0..width.toInt() step 10) {
                    val yPos = (height * 0.6f) + waveAmplitude * sin((x.toFloat() / width) * (2 * Math.PI) * 1f + wavePhase).toFloat()
                    lineTo(x.toFloat(), yPos)
                }
                lineTo(width, height)
                close()
            }

            // --- DÜZELTME BURADA ---
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(waveColor, Color.Transparent),
                    startY = height * 0.4f,
                    endY = height
                ),
                style = Fill // 'style =' diyerek parametreyi ismen belirttik
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    "GENEL MALİYET",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tahmini Tutar",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "${currencyFormat.format(animatedTotalCost)} ₺",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun AdvancedInputSection(lengthValue: String, onLengthChange: (String) -> Unit, spacingValue: String, onSpacingChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    Card(elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CompactInput("Arazi (m)", lengthValue, onLengthChange, Icons.Filled.Straighten, Modifier.weight(1f), focusManager)
            CompactInput("Ara (m)", spacingValue, onSpacingChange, Icons.Filled.Settings, Modifier.weight(1f), focusManager, true)
        }
    }
}

@Composable
fun CompactInput(t: String, v: String, c: (String)->Unit, i: ImageVector, m: Modifier, f: androidx.compose.ui.focus.FocusManager, l: Boolean = false) {
    Column(m) {
        Text(t, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(value = v, onValueChange = c, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), singleLine = true, leadingIcon = { Icon(i, null, Modifier.size(18.dp)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if(l) ImeAction.Done else ImeAction.Next), keyboardActions = KeyboardActions(onDone = { f.clearFocus() }), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
    }
}

@Preview(showBackground = true)
@Composable
fun FullAppPreview() {
    FenceCalculatorTheme { FenceCalculatorScreen() }
}