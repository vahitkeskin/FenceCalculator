package com.vahitkeskin.fencecalculator.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import com.vahitkeskin.fencecalculator.data.model.CustomCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.ceil

import androidx.lifecycle.viewModelScope
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class AppTheme { LIGHT, DARK, SYSTEM }

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // --- PERSISTENT STATE ---
    var companyName by mutableStateOf(""); private set
    fun onCompanyNameChange(name: String) {
        companyName = name
        viewModelScope.launch {
            dataStoreManager.saveCompanyName(name)
        }
    }

    // --- THEME STATE ---
    var currentTheme by mutableStateOf(AppTheme.SYSTEM); private set
    fun onThemeChange(theme: AppTheme) {
        currentTheme = theme
        viewModelScope.launch {
            dataStoreManager.saveTheme(theme.name)
        }
    }

    // --- CONSTANTS ---
    object Defaults {
        const val LENGTH = "300"
        const val SPACING = "3.5"
        const val HEIGHT = "1.5"
        const val STRUT_INTERVAL = "15"
        const val STRUT_COUNT = "2"
        const val MESH_ROLL = "20"
        const val BARBED_ROWS = "3"
        const val BARBED_ROLL = "200"
        const val WIRE_THICKNESS = "2.5"
        const val WEIGHT_CONSTANT = "1.3"
        const val MESH_EYE = "6.5"
    }

    // --- STATE ---
    var totalLengthInput by mutableStateOf(Defaults.LENGTH); private set
    var fenceHeightInput by mutableStateOf(Defaults.HEIGHT); private set
    var poleSpacingInput by mutableStateOf(Defaults.SPACING); private set
    var strutIntervalInput by mutableStateOf(Defaults.STRUT_INTERVAL); private set
    var strutCountInput by mutableStateOf(Defaults.STRUT_COUNT); private set
    var meshRollLengthInput by mutableStateOf(Defaults.MESH_ROLL); private set
    var barbedWireRowsInput by mutableStateOf(Defaults.BARBED_ROWS); private set
    var barbedWireRollLengthInput by mutableStateOf(Defaults.BARBED_ROLL); private set
    var wireThicknessInput by mutableStateOf(Defaults.WIRE_THICKNESS); private set
    var weightConstantInput by mutableStateOf(Defaults.WEIGHT_CONSTANT); private set
    var meshEyeInput by mutableStateOf(Defaults.MESH_EYE); private set

    // --- ADVANCED PARAMETERS (PERSISTENT) ---
    var poleLengthInput by mutableStateOf("2.4"); private set
    fun onPoleLengthChange(v: String) = updateIfValid(v) { 
        poleLengthInput = it 
        viewModelScope.launch { dataStoreManager.savePoleLength(it) }
    }

    var pipeLengthInput by mutableStateOf("6.0"); private set
    fun onPipeLengthChange(v: String) = updateIfValid(v) { 
        pipeLengthInput = it 
        viewModelScope.launch { dataStoreManager.savePipeLength(it) }
    }

    var tensionFactorInput by mutableStateOf("6.66"); private set
    fun onTensionFactorChange(v: String) = updateIfValid(v) { 
        tensionFactorInput = it 
        viewModelScope.launch { dataStoreManager.saveTensionFactor(it) }
    }

    var bindingFactorInput by mutableStateOf("3.0"); private set
    fun onBindingFactorChange(v: String) = updateIfValid(v) { 
        bindingFactorInput = it 
        viewModelScope.launch { dataStoreManager.saveBindingFactor(it) }
    }

    var cementFactorInput by mutableStateOf("6.0"); private set
    fun onCementFactorChange(v: String) = updateIfValid(v) { 
        cementFactorInput = it 
        viewModelScope.launch { dataStoreManager.saveCementFactor(it) }
    }

    var concreteFactorInput by mutableStateOf("30.0"); private set
    fun onConcreteFactorChange(v: String) = updateIfValid(v) { 
        concreteFactorInput = it 
        viewModelScope.launch { dataStoreManager.saveConcreteFactor(it) }
    }

    private var priceMap = mutableMapOf<String, String>()
    var results by mutableStateOf<List<CalculationItem>>(emptyList()); private set
    var grandTotalCost by mutableStateOf(0.0); private set

    // --- CUSTOM CARDS ---
    var customCards by mutableStateOf<List<CustomCardItem>>(emptyList()); private set
    var customCardResults by mutableStateOf<List<CalculationItem>>(emptyList()); private set

    // --- HIDDEN & ORDER ---
    var hiddenCardIds by mutableStateOf<Set<String>>(emptySet()); private set
    var cardOrder by mutableStateOf<List<String>>(emptyList()); private set

    // Tüm kartları (varsayılan + özel) sıralı ve filtrelenmiş şekilde döndür
    var orderedVisibleItems by mutableStateOf<List<CalculationItem>>(emptyList()); private set

    private fun rebuildOrderedVisibleItems() {
        val allItems = results + customCardResults
        val visibleItems = allItems.filter { it.id !in hiddenCardIds }

        orderedVisibleItems = if (cardOrder.isNotEmpty()) {
            val orderMap = cardOrder.withIndex().associate { (i, id) -> id to i }
            val ordered = visibleItems.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
            ordered
        } else {
            visibleItems
        }
        // Recalculate grand total from visible items
        grandTotalCost = orderedVisibleItems.sumOf { it.totalCost }
    }

    fun hideCard(id: String) {
        hiddenCardIds = hiddenCardIds + id
        // Özel kartsa tamamen sil
        if (id.startsWith("custom_")) {
            deleteCustomCard(id.removePrefix("custom_"))
        }
        saveHiddenCards()
        rebuildOrderedVisibleItems()
    }

    fun restoreDefaultCards() {
        hiddenCardIds = emptySet()
        cardOrder = emptyList()
        saveHiddenCards()
        saveCardOrder()
        rebuildOrderedVisibleItems()
    }

    fun moveCardUp(id: String) {
        val currentList = orderedVisibleItems.map { it.id }.toMutableList()
        val idx = currentList.indexOf(id)
        if (idx > 0) {
            currentList[idx] = currentList[idx - 1].also { currentList[idx - 1] = currentList[idx] }
            cardOrder = currentList
            saveCardOrder()
            rebuildOrderedVisibleItems()
        }
    }

    fun moveCardDown(id: String) {
        val currentList = orderedVisibleItems.map { it.id }.toMutableList()
        val idx = currentList.indexOf(id)
        if (idx >= 0 && idx < currentList.size - 1) {
            currentList[idx] = currentList[idx + 1].also { currentList[idx + 1] = currentList[idx] }
            cardOrder = currentList
            saveCardOrder()
            rebuildOrderedVisibleItems()
        }
    }

    private fun saveHiddenCards() {
        viewModelScope.launch {
            dataStoreManager.saveHiddenCards(hiddenCardIds.joinToString(","))
        }
    }

    private fun saveCardOrder() {
        viewModelScope.launch {
            dataStoreManager.saveCardOrder(cardOrder.joinToString(","))
        }
    }

    fun addOrUpdateCustomCard(card: CustomCardItem) {
        val current = customCards.toMutableList()
        val index = current.indexOfFirst { it.id == card.id }
        if (index >= 0) {
            current[index] = card
        } else {
            current.add(card)
        }
        customCards = current
        updateCustomCardResults()
        viewModelScope.launch {
            dataStoreManager.saveCustomCards(Json.encodeToString(current))
        }
    }

    fun deleteCustomCard(id: String) {
        val current = customCards.toMutableList()
        current.removeAll { it.id == id }
        customCards = current
        updateCustomCardResults()
        viewModelScope.launch {
            dataStoreManager.saveCustomCards(Json.encodeToString(current))
        }
    }

    fun getCustomCardById(id: String): CustomCardItem? {
        return customCards.find { it.id == id }
    }

    private fun updateCustomCardResults() {
        customCardResults = customCards.map { card ->
            val color = try {
                Color(android.graphics.Color.parseColor(card.colorHex))
            } catch (e: Exception) {
                Color(0xFF607D8B)
            }
            val totalCost = card.quantity * card.unitPrice
            CalculationItem(
                id = "custom_${card.id}",
                title = card.title,
                description = card.description,
                quantity = card.quantity,
                unit = card.unit,
                unitPrice = card.unitPrice,
                totalCost = totalCost,
                icon = Icons.Filled.Extension,
                color = color,
                emoji = card.emoji
            )
        }
        rebuildOrderedVisibleItems()
    }

    init {
        calculateValues()
        observeDataStore()
    }

    private fun observeDataStore() {
        viewModelScope.launch {
            dataStoreManager.companyName.collectLatest { name ->
                companyName = name
            }
        }
        viewModelScope.launch {
            dataStoreManager.theme.collectLatest { themeName ->
                currentTheme = try {
                    AppTheme.valueOf(themeName)
                } catch (e: Exception) {
                    AppTheme.SYSTEM
                }
            }
        }
        viewModelScope.launch { dataStoreManager.poleLength.collectLatest { poleLengthInput = it; calculateValues() } }
        viewModelScope.launch { dataStoreManager.pipeLength.collectLatest { pipeLengthInput = it; calculateValues() } }
        viewModelScope.launch { dataStoreManager.tensionFactor.collectLatest { tensionFactorInput = it; calculateValues() } }
        viewModelScope.launch { dataStoreManager.bindingFactor.collectLatest { bindingFactorInput = it; calculateValues() } }
        viewModelScope.launch { dataStoreManager.cementFactor.collectLatest { cementFactorInput = it; calculateValues() } }
        viewModelScope.launch { dataStoreManager.concreteFactor.collectLatest { concreteFactorInput = it; calculateValues() } }
        viewModelScope.launch {
            dataStoreManager.customCards.collectLatest { json ->
                customCards = try {
                    Json.decodeFromString(json)
                } catch (e: Exception) {
                    emptyList()
                }
                updateCustomCardResults()
            }
        }
        viewModelScope.launch {
            dataStoreManager.hiddenCards.collectLatest { csv ->
                hiddenCardIds = if (csv.isBlank()) emptySet() else csv.split(",").toSet()
                rebuildOrderedVisibleItems()
            }
        }
        viewModelScope.launch {
            dataStoreManager.cardOrder.collectLatest { csv ->
                cardOrder = if (csv.isBlank()) emptyList() else csv.split(",")
                rebuildOrderedVisibleItems()
            }
        }
    }

    // --- EVENTS ---
    fun onTotalLengthChange(v: String) = updateIfValid(v) { totalLengthInput = it }
    fun onFenceHeightChange(v: String) = updateIfValid(v) { fenceHeightInput = it }
    fun onPoleSpacingChange(v: String) = updateIfValid(v) { poleSpacingInput = it }
    fun onStrutIntervalChange(v: String) = updateIfValid(v) { strutIntervalInput = it }
    fun onStrutCountChange(v: String) = updateIfValid(v) { strutCountInput = it }
    fun onMeshRollLengthChange(v: String) = updateIfValid(v) { meshRollLengthInput = it }
    fun onBarbedWireRowsChange(v: String) = updateIfValid(v) { barbedWireRowsInput = it }
    fun onBarbedWireRollLengthChange(v: String) = updateIfValid(v) { barbedWireRollLengthInput = it }
    fun onWireThicknessChange(v: String) = updateIfValid(v) { wireThicknessInput = it }
    fun onWeightConstantChange(v: String) = updateIfValid(v) { weightConstantInput = it }
    fun onMeshEyeChange(v: String) = updateIfValid(v) { meshEyeInput = it }

    fun onPriceChange(id: String, v: String) {
        val s = v.replace(',', '.')
        if (isValid(s)) {
            priceMap[id] = s
            calculateValues()
        }
    }

    private inline fun updateIfValid(value: String, setter: (String) -> Unit) {
        if (isValid(value)) {
            setter(value)
            calculateValues()
        }
    }

    private fun isValid(input: String): Boolean {
        if (input.isEmpty()) return true
        return input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1
    }

    fun getPriceString(id: String): String = priceMap[id] ?: ""

    // --- YARDIMCI FONKSİYON: YUVARLAMA VE LOGLAMA ---
    private fun ceilAndLog(label: String, rawValue: Double): Double {
        val roundedValue = ceil(rawValue) // Yukarı Yuvarla (3.1 -> 4.0)
        // Logcat'e Yazdır
        println("HESAPLAMA LOG: $label -> Eski: $rawValue, Yeni: $roundedValue")
        return roundedValue
    }

    private fun calculateValues() {
        val length = totalLengthInput.toDoubleOrNull() ?: 0.0
        val height = fenceHeightInput.toDoubleOrNull() ?: 0.0
        val spacing = poleSpacingInput.toDoubleOrNull() ?: 3.5
        val strutFreq = strutIntervalInput.toDoubleOrNull() ?: 15.0
        val strutCnt = strutCountInput.toDoubleOrNull() ?: 2.0
        val meshLen = meshRollLengthInput.toDoubleOrNull() ?: 20.0
        val barbedRows = barbedWireRowsInput.toDoubleOrNull() ?: 3.0
        val barbedLen = barbedWireRollLengthInput.toDoubleOrNull() ?: 250.0
        val thickness = wireThicknessInput.toDoubleOrNull() ?: 2.5
        val constant = weightConstantInput.toDoubleOrNull() ?: 1.3
        val eye = meshEyeInput.toDoubleOrNull() ?: 6.5

        // Advanced Parameters
        val pLength = poleLengthInput.toDoubleOrNull() ?: 2.4
        val pipeLen = pipeLengthInput.toDoubleOrNull() ?: 6.0
        val tFactor = tensionFactorInput.toDoubleOrNull() ?: 6.66
        val bFactor = bindingFactorInput.toDoubleOrNull() ?: 3.0
        val cemFactor = cementFactorInput.toDoubleOrNull() ?: 6.0
        val concFactor = concreteFactorInput.toDoubleOrNull() ?: 30.0

        if (length == 0.0 || spacing <= 0.0 || height == 0.0) {
            results = emptyList()
            grandTotalCost = 0.0 + customCardResults.sumOf { it.totalCost }
            return
        }

        println("----------------- HESAPLAMA BAŞLADI -----------------")

        // 1. Direk (Adet)
        val rawDirek = length / spacing
        val direkSayisi = ceilAndLog("Direk Sayısı", rawDirek)

        // 2. Payanda (Adet)
        val rawPayanda = (direkSayisi / strutFreq) * strutCnt
        val payandaSayisi = ceilAndLog("Payanda Sayısı", rawPayanda)

        // 3. Kafes Tel (Top)
        val rawKafesTop = length / meshLen
        val kafesTopSayisi = ceilAndLog("Kafes Tel (Top)", rawKafesTop)

        // 4. Kafes Tel (1 Top Ağırlığı - Kg)
        // m2 Ağırlığı
        val unitWeightPerM2 = (thickness * thickness * constant) / eye
        val oneRollArea = meshLen * height
        val rawOneRollWeight = unitWeightPerM2 * oneRollArea
        val oneRollWeight = ceilAndLog("1 Top Tel Ağırlığı (Kg)", rawOneRollWeight)

        // 5. Dikenli Tel (Top)
        val rawDiken = (barbedRows * length) / barbedLen
        val dikenliTelTopSayisi = ceilAndLog("Dikenli Tel (Top)", rawDiken)

        // 6. Gergi Teli (Kg)
        val rawGergi = length / tFactor
        val gergiTeli = ceilAndLog("Gergi Teli (Kg)", rawGergi)

        // 7. Bağlama Teli (Kg)
        val rawBaglama = gergiTeli / bFactor // Yuvarlanmış gergi üzerinden hesaplamak daha güvenli
        val baglamaTeli = ceilAndLog("Bağlama Teli (Kg)", rawBaglama)

        // 8. Çimento (50 Kg - Adet)
        val rawCimento = direkSayisi / cemFactor
        val cimentoSayisi = ceilAndLog("Çimento Sayısı", rawCimento)

        // 9. Hazır Beton (m3)
        val rawBeton = direkSayisi / concFactor
        val hazirBetonM3 = ceilAndLog("Hazır Beton (m3)", rawBeton)

        // 10. Boy Demir Boru (6m)
        // Her direk pLength, her boru pipeLen.
        val rawBoyDemir = (direkSayisi * pLength) / pipeLen
        val boyDemirSayisi = ceilAndLog("Boy Demir Boru ($pipeLen m)", rawBoyDemir)

        println("----------------- HESAPLAMA BİTTİ -----------------")

        fun getP(id: String) = priceMap[id]?.toDoubleOrNull() ?: 0.0

        val list = mutableListOf(
            createItem("direk", "Direk", "Her $spacing m'de bir", direkSayisi, "Adet", Icons.Filled.Straighten, Color(0xFF3F51B5), ::getP),
            createItem("boy_demir", "Boy Demir Boru", "$pipeLen m (Her borudan $pLength m'lik direk)", boyDemirSayisi, "Adet", Icons.Filled.FormatLineSpacing, Color(0xFF5C6BC0), ::getP),
            createItem("payanda", "Payanda", "Her $strutFreq direkte $strutCnt adet", payandaSayisi, "Adet", Icons.Filled.ChangeHistory, Color(0xFF9C27B0), ::getP),
            createItem("kafes_top", "Kafes Tel (Toplam)", "$meshLen m'lik top ($height m Yükseklik)", kafesTopSayisi, "Top", Icons.Filled.GridOn, Color(0xFF009688), ::getP),
            // Dikkat: oneRollWeight sadece bilgi amaçlı, toplam maliyete doğrudan adet olarak eklenmiyor, birim fiyat belirlemede kullanılıyor.
            createItem("kafes_kg", "1 Top Tel Ağırlığı", "1 Rulo ($meshLen m) ağırlığıdır.", oneRollWeight, "Kg", Icons.Filled.Scale, Color(0xFF00796B), ::getP),
            createItem("diken", "Dikenli Tel", "${barbedRows.toInt()} Sıra ($barbedLen m/top)", dikenliTelTopSayisi, "Top", Icons.Filled.Warning, Color(0xFFD32F2F), ::getP),
            createItem("gergi", "Gergi Teli", "Uzunluk / 6.66", gergiTeli, "Kg", Icons.Filled.LinearScale, Color(0xFFFF9800), ::getP),
            createItem("baglama", "Bağlama Teli", "Gergi Telinin 1/3'ü", baglamaTeli, "Kg", Icons.Filled.AllInclusive, Color(0xFF795548), ::getP),
            createItem("cimento", "Çimento (50 Kg)", "Direk Sayısı / 6", cimentoSayisi, "Adet", Icons.Filled.Layers, Color(0xFF607D8B), ::getP),
            createItem("beton", "Hazır Beton", "Direk Sayısı / 30", hazirBetonM3, "m³", Icons.Filled.PrecisionManufacturing, Color(0xFF455A64), ::getP)
        )

        results = list
        rebuildOrderedVisibleItems()
    }

    private fun createItem(id: String, t: String, d: String, q: Double, u: String, i: ImageVector, c: Color, p: (String) -> Double) =
        CalculationItem(id, t, d, q, u, p(id), q * p(id), i, c)
}