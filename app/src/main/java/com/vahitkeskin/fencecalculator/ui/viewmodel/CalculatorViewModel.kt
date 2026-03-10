package com.vahitkeskin.fencecalculator.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import com.vahitkeskin.fencecalculator.data.model.CustomCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject
import kotlin.math.ceil

import androidx.lifecycle.viewModelScope
import com.vahitkeskin.fencecalculator.util.AppLanguage
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.util.AppStrings
import com.vahitkeskin.fencecalculator.util.Localization
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.launch
import android.app.Activity
import android.net.ConnectivityManager
import android.net.Network
import android.net.LinkProperties
import com.vahitkeskin.fencecalculator.util.DnsDetector
import com.vahitkeskin.fencecalculator.util.AdManager

enum class AppTheme { LIGHT, DARK, SYSTEM }

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _scrollToTop = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val scrollToTop = _scrollToTop.asSharedFlow()

    fun requestScrollToTop(route: String) {
        viewModelScope.launch {
            _scrollToTop.emit(route)
        }
    }

    // --- PERSISTENT STATE ---
    var companyName by mutableStateOf(""); private set
    fun onCompanyNameChange(name: String) {
        companyName = name
        viewModelScope.launch {
            dataStoreManager.saveCompanyName(name)
        }
    }

    var customerName by mutableStateOf(""); private set
    fun onCustomerNameChange(v: String) {
        customerName = v
    }

    var customerPhone by mutableStateOf(""); private set
    fun onCustomerPhoneChange(v: String) {
        customerPhone = v
    }

    var iban by mutableStateOf(""); private set
    fun onIbanChange(v: String) {
        iban = v
        viewModelScope.launch { dataStoreManager.saveIban(v) }
    }

    var isIbanExpanded by mutableStateOf(false); private set
    fun onIbanExpandedToggle(v: Boolean) {
        isIbanExpanded = v
        viewModelScope.launch { dataStoreManager.saveIbanExpanded(v) }
    }

    var isOnboardingCompleted by mutableStateOf(true); private set
    fun setOnboardingCompleted() {
        isOnboardingCompleted = true
        viewModelScope.launch { dataStoreManager.saveOnboardingCompleted(true) }
    }

    var isLockTooltipShown by mutableStateOf(false); private set
    fun setLockTooltipShown() {
        isLockTooltipShown = true
        viewModelScope.launch { dataStoreManager.saveLockTooltipShown(true) }
    }

    var usageCount by mutableIntStateOf(0); private set
    var isPremium by mutableStateOf(false); private set

    fun onPremiumToggle(v: Boolean) {
        isPremium = v
        viewModelScope.launch { dataStoreManager.saveIsPremium(v) }
    }

    // --- DNS DETECTION ---
    var isPrivateDnsEnabled by mutableStateOf(false); private set

    fun checkPrivateDns() {
        isPrivateDnsEnabled = DnsDetector.isPrivateDnsEnabled(context)
    }

    private fun startDnsMonitoring() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                checkPrivateDns()
            }
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                checkPrivateDns()
            }
            override fun onLost(network: Network) {
                checkPrivateDns()
            }
        })
    }

    // --- THEME STATE ---
    var currentTheme by mutableStateOf(AppTheme.SYSTEM); private set
    fun onThemeChange(theme: AppTheme) {
        currentTheme = theme
        viewModelScope.launch {
            dataStoreManager.saveTheme(theme.name)
        }
    }

    // --- LANGUAGE STATE ---
    var selectedLanguage by mutableStateOf(AppLanguage.EN); private set
    val strings: AppStrings
        get() = Localization.getStrings(selectedLanguage.code)

    fun onLanguageChange(language: AppLanguage) {
        selectedLanguage = language
        viewModelScope.launch {
            dataStoreManager.saveAppLanguage(language.code)
        }
        // Dillere bağlı olarak sonuçları tekrar oluştur
        calculateValues()
    }

    // --- CONSTANTS ---
    object Defaults {
        const val LENGTH = ""
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
    var totalLengthDraft by mutableStateOf(Defaults.LENGTH); private set
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
    }

    var pipeLengthInput by mutableStateOf("6.0"); private set
    fun onPipeLengthChange(v: String) = updateIfValid(v) {
        pipeLengthInput = it
    }

    var tensionFactorInput by mutableStateOf("6.66"); private set
    fun onTensionFactorChange(v: String) = updateIfValid(v) {
        tensionFactorInput = it
    }

    var bindingFactorInput by mutableStateOf("3.0"); private set
    fun onBindingFactorChange(v: String) = updateIfValid(v) {
        bindingFactorInput = it
    }

    var cementFactorInput by mutableStateOf("6.0"); private set
    fun onCementFactorChange(v: String) = updateIfValid(v) {
        cementFactorInput = it
    }

    var concreteFactorInput by mutableStateOf("30.0"); private set
    fun onConcreteFactorChange(v: String) = updateIfValid(v) {
        concreteFactorInput = it
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
    var pinnedCardIds by mutableStateOf<Set<String>>(emptySet()); private set

    // Pinned items derived state
    val pinnedItems: List<CalculationItem>
        get() = orderedVisibleItems.filter { it.id in pinnedCardIds }

    // Tüm kartları (varsayılan + özel) sıralı ve filtrelenmiş şekilde döndür
    var orderedVisibleItems by mutableStateOf<List<CalculationItem>>(emptyList()); private set

    private fun rebuildOrderedVisibleItems() {
        val allItems = results + customCardResults
        val processedItems = allItems.map { item ->
            item.copy(isPinned = item.id in pinnedCardIds)
        }
        val visibleItems = processedItems.filter { it.id !in hiddenCardIds }

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

    fun togglePin(id: String) {
        pinnedCardIds = if (id in pinnedCardIds) {
            pinnedCardIds - id
        } else {
            pinnedCardIds + id
        }
        savePinnedCards()
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

    private fun savePinnedCards() {
        viewModelScope.launch {
            dataStoreManager.savePinnedCards(pinnedCardIds.joinToString(","))
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
        val allStaticResults = results // Varsayılan kart sonuçları

        customCardResults = customCards.map { card ->
            val color = try {
                Color(android.graphics.Color.parseColor(card.colorHex))
            } catch (e: Exception) {
                Color(0xFF607D8B)
            }

            // Bağımlılık hesabı
            val calculationResult =
                if (card.dependentCardId != null && card.dependentRatio != null) {
                    // Önce varsayılan ürünlerde ara (id as is)
                    val baseItem = allStaticResults.find { it.id == card.dependentCardId }
                    // Eğer orada yoksa özel kartlarda ara
                        ?: customCards.find { it.id == card.dependentCardId }?.let { depCard ->
                            CalculationItem(
                                id = "",
                                title = depCard.title,
                                description = "",
                                quantity = depCard.quantity,
                                unit = "",
                                unitPrice = 0.0,
                                totalCost = 0.0,
                                icon = Icons.Filled.Block,
                                color = Color.Transparent
                            )
                        }

                    val baseQty = baseItem?.quantity ?: 0.0
                    val ratio = card.dependentRatio

                    val result = when (card.dependentOperation) {
                        "+" -> baseQty + ratio
                        "-" -> baseQty - ratio
                        "÷", "/" -> if (ratio != 0.0) baseQty / ratio else baseQty
                        else -> baseQty * ratio // Varsayılan çarpma "*"
                    }

                    val depInfo = if (baseItem != null) {
                        "${baseItem.title} (${baseQty.toInt()} ${baseItem.unit}) ${card.dependentOperation} $ratio"
                    } else null

                    ceil(result) to depInfo
                } else {
                    card.quantity to null
                }

            val (finalQty, dependencyInfo) = calculationResult
            val totalCost = finalQty * card.unitPrice
            CalculationItem(
                id = "custom_${card.id}",
                title = card.title,
                description = card.description,
                quantity = finalQty,
                unit = card.unit,
                unitPrice = card.unitPrice,
                totalCost = totalCost,
                icon = Icons.Default.Extension,
                color = color,
                emoji = card.emoji,
                dependencyInfo = null
            )
        }
        rebuildOrderedVisibleItems()
    }

    init {
        calculateValues()
        observeDataStore()
        checkPrivateDns()
        startDnsMonitoring()
    }

    private fun observeDataStore() {
        // Initializing with clean slate for inputs as requested
        customerName = ""
        customerPhone = ""
        totalLengthInput = Defaults.LENGTH
        totalLengthDraft = Defaults.LENGTH
        
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
        /* Removed persistent collection for fresh start on launch
        viewModelScope.launch {
            dataStoreManager.poleLength.collectLatest {
                poleLengthInput = it; calculateValues()
            }
        }
        viewModelScope.launch {
            dataStoreManager.pipeLength.collectLatest {
                pipeLengthInput = it; calculateValues()
            }
        }
        viewModelScope.launch {
            dataStoreManager.tensionFactor.collectLatest {
                tensionFactorInput = it; calculateValues()
            }
        }
        viewModelScope.launch {
            dataStoreManager.bindingFactor.collectLatest {
                bindingFactorInput = it; calculateValues()
            }
        }
        viewModelScope.launch {
            dataStoreManager.cementFactor.collectLatest {
                cementFactorInput = it; calculateValues()
            }
        }
        viewModelScope.launch {
            dataStoreManager.concreteFactor.collectLatest {
                concreteFactorInput = it; calculateValues()
            }
        }
        */
        viewModelScope.launch {
            dataStoreManager.customCards.collectLatest { json ->
                customCards = try {
                    val loaded: List<CustomCardItem> = Json.decodeFromString(json)
                    // Resetting unit prices for fresh start as requested
                    loaded.map { it.copy(unitPrice = 0.0) }
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
        viewModelScope.launch {
            dataStoreManager.pinnedCards.collectLatest { csv ->
                pinnedCardIds = if (csv.isBlank()) emptySet() else csv.split(",").toSet()
                rebuildOrderedVisibleItems()
            }
        }
        /* Removed persistent collection for fresh start on launch
        viewModelScope.launch { dataStoreManager.customerName.collectLatest { customerName = it } }
        viewModelScope.launch {
            dataStoreManager.customerPhone.collectLatest {
                customerPhone = it
            }
        }
        */
        viewModelScope.launch {
            dataStoreManager.iban.collectLatest {
                iban = it
            }
        }
        viewModelScope.launch {
            dataStoreManager.appLanguage.collectLatest { code ->
                selectedLanguage = if (code == "detect") {
                    AppLanguage.detectSystemLanguage()
                } else {
                    AppLanguage.fromCode(code)
                }
                calculateValues()
            }
        }
        viewModelScope.launch {
            dataStoreManager.onboardingCompleted.collectLatest {
                isOnboardingCompleted = it
            }
        }
        viewModelScope.launch {
            dataStoreManager.lockTooltipShown.collectLatest {
                isLockTooltipShown = it
            }
        }
        viewModelScope.launch {
            dataStoreManager.usageCount.collectLatest {
                usageCount = it
            }
        }
        viewModelScope.launch {
            dataStoreManager.isPremium.collectLatest {
                isPremium = it
            }
        }
        viewModelScope.launch {
            dataStoreManager.ibanExpanded.collectLatest {
                isIbanExpanded = it
            }
        }
    }

    // --- EVENTS ---
    fun onTotalLengthChange(v: String) {
        // Clear non-numeric chars except dot/comma
        val cleaned = v.replace(",", ".").filter { it.isDigit() || it == '.' }
        totalLengthDraft = cleaned
    }

    fun applyTotalLength() {
        totalLengthInput = totalLengthDraft
        calculateValues()
    }

    fun clearTotalLength() {
        totalLengthInput = ""
        totalLengthDraft = ""
        calculateValues()
    }

    fun onFenceHeightChange(v: String) = updateIfValid(v) { fenceHeightInput = it }
    fun onPoleSpacingChange(v: String) = updateIfValid(v) { poleSpacingInput = it }
    fun onStrutIntervalChange(v: String) = updateIfValid(v) { strutIntervalInput = it }
    fun onStrutCountChange(v: String) = updateIfValid(v) { strutCountInput = it }
    fun onMeshRollLengthChange(v: String) = updateIfValid(v) { meshRollLengthInput = it }
    fun onBarbedWireRowsChange(v: String) = updateIfValid(v) { barbedWireRowsInput = it }
    fun onBarbedWireRollLengthChange(v: String) =
        updateIfValid(v) { barbedWireRollLengthInput = it }

    fun onWireThicknessChange(v: String) = updateIfValid(v) { wireThicknessInput = it }
    fun onWeightConstantChange(v: String) = updateIfValid(v) { weightConstantInput = it }
    fun onMeshEyeChange(v: String) = updateIfValid(v) { meshEyeInput = it }

    fun onPriceChange(id: String, v: String) {
        val cleaned = v.replace(',', '.').filter { it.isDigit() || it == '.' }
        priceMap[id] = cleaned
        if (id.startsWith("custom_")) {
            val realId = id.removePrefix("custom_")
            val card = getCustomCardById(realId)
            if (card != null) {
                val newPrice = cleaned.toDoubleOrNull() ?: 0.0
                val updatedCard = card.copy(unitPrice = newPrice)
                val current = customCards.toMutableList()
                val index = current.indexOfFirst { it.id == realId }
                if (index >= 0) {
                    current[index] = updatedCard
                    customCards = current
                    // Güncelle ve hesabı çalıştır
                    updateCustomCardResults()
                    viewModelScope.launch {
                        dataStoreManager.saveCustomCards(Json.encodeToString(current))
                    }
                }
            }
        } else {
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

    fun getPriceString(id: String): String {
        return priceMap[id] ?: if (id.startsWith("custom_")) {
            val realId = id.removePrefix("custom_")
            val card = getCustomCardById(realId)
            if (card != null && card.unitPrice > 0) {
                val formatted = card.unitPrice.toString()
                if (formatted.endsWith(".0")) formatted.removeSuffix(".0") else formatted
            } else ""
        } else ""
    }

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

        if (spacing <= 0.0 || height == 0.0) {
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
            createItem(
                "direk",
                strings.direkTitle,
                String.format(strings.direkDesc, spacing.toString()),
                direkSayisi,
                strings.unitPiece,
                Icons.Filled.Straighten,
                Color(0xFF3F51B5),
                strings.catMetal,
                "", // Empty formula
                ::getP
            ),
            createItem(
                "boy_demir",
                strings.boyDemirTitle,
                String.format(strings.boyDemirDesc, pipeLen.toString(), pLength.toString()),
                boyDemirSayisi,
                strings.unitPiece,
                Icons.Filled.FormatLineSpacing,
                Color(0xFF5C6BC0),
                strings.catMetal,
                "", // Empty formula
                ::getP
            ),
            createItem(
                "payanda",
                strings.payandaTitle,
                String.format(strings.payandaDesc, strutFreq.toInt().toString(), strutCnt.toInt().toString()),
                payandaSayisi,
                strings.unitPiece,
                Icons.Filled.ChangeHistory,
                Color(0xFF9C27B0),
                strings.catMetal,
                "", // Empty formula
                ::getP
            ),
 
            createItem(
                "kafes_top",
                strings.kafesTopTitle,
                String.format(strings.kafesTopDesc, meshLen.toString(), height.toString()),
                kafesTopSayisi,
                strings.unitRoll,
                Icons.Filled.GridOn,
                Color(0xFF009688),
                strings.catWire,
                "", // Empty formula
                ::getP
            ),
            createItem(
                "kafes_kg",
                strings.kafesKgTitle,
                String.format(strings.kafesKgDesc, meshLen.toString()),
                oneRollWeight,
                strings.unitKg,
                Icons.Filled.Scale,
                Color(0xFF00796B),
                strings.catWire,
                "", // Empty formula
                ::getP
            ),
            createItem(
                "diken",
                strings.dikenTitle,
                String.format(strings.dikenDesc, barbedRows.toInt(), barbedLen.toString()),
                dikenliTelTopSayisi,
                strings.unitRoll,
                Icons.Filled.Warning,
                Color(0xFFD32F2F),
                strings.catWire,
                "", // Empty formula
                ::getP
            ),
            createItem(
                "gergi",
                strings.gergiTitle,
                String.format(strings.gergiDesc, tFactor.toString()),
                gergiTeli,
                strings.unitKg,
                Icons.Filled.LinearScale,
                Color(0xFFFF9800),
                strings.catWire,
                "", // Empty formula
                ::getP
            ),
            createItem(
                "baglama",
                strings.baglamaTitle,
                String.format(strings.baglamaDesc, bFactor.toInt().toString()),
                baglamaTeli,
                strings.unitKg,
                Icons.Filled.Link,
                Color(0xFF795548),
                strings.catWire,
                "", // Empty formula
                ::getP
            ),
 
            createItem(
                "cimento",
                strings.cimentoTitle,
                strings.cimentoDesc,
                cimentoSayisi,
                strings.unitPiece,
                Icons.Filled.Egg,
                Color(0xFF607D8B),
                strings.catConstruction,
                "", // Empty formula
                ::getP
            ),
            createItem(
                "beton",
                strings.betonTitle,
                strings.betonDesc,
                hazirBetonM3,
                strings.unitM3,
                Icons.Filled.Layers,
                Color(0xFF455A64),
                strings.catConstruction,
                "", // Empty formula
                ::getP
            )
        )

        results = list
        updateCustomCardResults() // Özel kartları da güncelle (bağımlılıklar için)
        rebuildOrderedVisibleItems()
    }

    private fun createItem(
        id: String,
        t: String,
        d: String,
        q: Double,
        u: String,
        i: ImageVector,
        c: Color,
        cat: String,
        formula: String,
        p: (String) -> Double
    ) =
        CalculationItem(
            id = id,
            title = t,
            description = d,
            quantity = q,
            unit = u,
            unitPrice = p(id),
            totalCost = q * p(id),
            icon = i,
            color = c,
            category = cat,
            dependencyInfo = formula
        )

    fun scanQrCode(bitmap: android.graphics.Bitmap) {
        val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
        val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val rawValue = barcodes.first().rawValue
                    if (!rawValue.isNullOrBlank()) {
                        onIbanChange(rawValue)
                    }
                }
            }
            .addOnFailureListener {
                // Ignore or log error
            }
    }
}