package com.vahitkeskin.fencecalculator.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import com.vahitkeskin.fencecalculator.data.model.CustomCardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject
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
import com.vahitkeskin.fencecalculator.util.AdManager

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    internal val dataStoreManager: DataStoreManager,
    @ApplicationContext internal val context: Context
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

    var customerName by mutableStateOf(""); internal set
    fun onCustomerNameChange(v: String) {
        customerName = v
    }

    var customerPhone by mutableStateOf(""); internal set
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
    var isPrivateDnsEnabled by mutableStateOf(false); internal set

    // DNS monitoring logic is in CalculatorDnsLogic.kt
    fun checkPrivateDns() = checkPrivateDnsExt()

    // DNS monitoring logic is in CalculatorDnsLogic.kt
    private fun startDnsMonitoring() = startDnsMonitoringExt()

    // --- THEME STATE ---
    var currentTheme by mutableStateOf(AppTheme.SYSTEM); private set
    fun onThemeChange(theme: AppTheme) {
        currentTheme = theme
        viewModelScope.launch {
            dataStoreManager.saveTheme(theme.name)
        }
    }

    // --- LANGUAGE STATE ---
    var selectedLanguage by mutableStateOf(AppLanguage.EN); internal set
    val strings: AppStrings
        get() = Localization.getStrings(selectedLanguage.code)

    fun onLanguageChange(language: AppLanguage) {
        selectedLanguage = language
        viewModelScope.launch {
            dataStoreManager.saveAppLanguage(language.code)
        }
        // Calculation logic is in CalculatorCalculationLogic.kt
        calculateValues()
    }

    // --- STATE ---
    var totalLengthInput by mutableStateOf(Defaults.LENGTH); internal set
    var totalLengthDraft by mutableStateOf(Defaults.LENGTH); internal set
    var fenceHeightInput by mutableStateOf(Defaults.HEIGHT); internal set
    var poleSpacingInput by mutableStateOf(Defaults.SPACING); internal set
    var strutIntervalInput by mutableStateOf(Defaults.STRUT_INTERVAL); internal set
    var strutCountInput by mutableStateOf(Defaults.STRUT_COUNT); internal set
    var meshRollLengthInput by mutableStateOf(Defaults.MESH_ROLL); internal set
    var barbedWireRowsInput by mutableStateOf(Defaults.BARBED_ROWS); internal set
    var barbedWireRollLengthInput by mutableStateOf(Defaults.BARBED_ROLL); internal set
    var wireThicknessInput by mutableStateOf(Defaults.WIRE_THICKNESS); internal set
    var weightConstantInput by mutableStateOf(Defaults.WEIGHT_CONSTANT); internal set
    var meshEyeInput by mutableStateOf(Defaults.MESH_EYE); internal set

    // --- ADVANCED PARAMETERS (PERSISTENT) ---
    var poleLengthInput by mutableStateOf("2.4"); internal set
    fun onPoleLengthChange(v: String) = updateIfValid(v) {
        poleLengthInput = it
    }

    var pipeLengthInput by mutableStateOf("6.0"); internal set
    fun onPipeLengthChange(v: String) = updateIfValid(v) {
        pipeLengthInput = it
    }

    var tensionFactorInput by mutableStateOf("6.66"); internal set
    fun onTensionFactorChange(v: String) = updateIfValid(v) {
        tensionFactorInput = it
    }

    var bindingFactorInput by mutableStateOf("3.0"); internal set
    fun onBindingFactorChange(v: String) = updateIfValid(v) {
        bindingFactorInput = it
    }

    var cementFactorInput by mutableStateOf("6.0"); internal set
    fun onCementFactorChange(v: String) = updateIfValid(v) {
        cementFactorInput = it
    }

    var concreteFactorInput by mutableStateOf("30.0"); internal set
    fun onConcreteFactorChange(v: String) = updateIfValid(v) {
        concreteFactorInput = it
    }

    internal var priceMap = mutableMapOf<String, String>()
    var results by mutableStateOf<List<CalculationItem>>(emptyList()); internal set
    var grandTotalCost by mutableStateOf(0.0); internal set

    // --- CUSTOM CARDS ---
    internal var isFirstCustomCardLoad = true
    var customCards by mutableStateOf<List<CustomCardItem>>(emptyList()); internal set
    var customCardResults by mutableStateOf<List<CalculationItem>>(emptyList()); internal set

    // --- HIDDEN & ORDER ---
    var hiddenCardIds by mutableStateOf<Set<String>>(emptySet()); internal set
    var cardOrder by mutableStateOf<List<String>>(emptyList()); internal set
    var pinnedCardIds by mutableStateOf<Set<String>>(emptySet()); internal set

    // Pinned items derived state
    val pinnedItems: List<CalculationItem>
        get() = orderedVisibleItems.filter { it.id in pinnedCardIds }

    // Tüm kartları (varsayılan + özel) sıralı ve filtrelenmiş şekilde döndür
    var orderedVisibleItems by mutableStateOf<List<CalculationItem>>(emptyList()); internal set

    // Card management logic is in CalculatorCardLogic.kt
    internal fun rebuildOrderedVisibleItems() = rebuildOrderedVisibleItemsExt()

    // Card management logic is in CalculatorCardLogic.kt
    fun hideCard(id: String) = hideCardExt(id)

    // Card management logic is in CalculatorCardLogic.kt
    fun togglePin(id: String) = togglePinExt(id)

    // Card management logic is in CalculatorCardLogic.kt
    fun restoreDefaultCards() = restoreDefaultCardsExt()

    // Card management logic is in CalculatorCardLogic.kt
    fun moveCardUp(id: String) = moveCardUpExt(id)

    // Card management logic is in CalculatorCardLogic.kt
    fun moveCardDown(id: String) = moveCardDownExt(id)

    // Card management logic is in CalculatorCardLogic.kt
    private fun saveHiddenCards() = saveHiddenCardsExt()

    // Card management logic is in CalculatorCardLogic.kt
    private fun saveCardOrder() = saveCardOrderExt()

    // Card management logic is in CalculatorCardLogic.kt
    private fun savePinnedCards() = savePinnedCardsExt()

    // Custom card logic is in CalculatorCustomCardLogic.kt
    fun addOrUpdateCustomCard(card: CustomCardItem) = addOrUpdateCustomCardExt(card)

    // Custom card logic is in CalculatorCustomCardLogic.kt
    fun deleteCustomCard(id: String) = deleteCustomCardExt(id)

    // Custom card logic is in CalculatorCustomCardLogic.kt
    fun getCustomCardById(id: String): CustomCardItem? = getCustomCardByIdExt(id)

    // Custom card logic is in CalculatorCustomCardLogic.kt
    internal fun updateCustomCardResults() = updateCustomCardResultsExt()

    init {
        // Calculation logic is in CalculatorCalculationLogic.kt
        calculateValues()
        observeDataStore()
        // DNS monitoring logic is in CalculatorDnsLogic.kt
        checkPrivateDns()
        // DNS monitoring logic is in CalculatorDnsLogic.kt
        startDnsMonitoring()
    }

    private fun observeDataStore() {
        // Initializing with clean slate for inputs as requested
        customerName = ""
        customerPhone = ""
        totalLengthInput = Defaults.LENGTH
        totalLengthDraft = Defaults.LENGTH
        priceMap.clear()
        
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
        viewModelScope.launch {
            dataStoreManager.customCards.collectLatest { json ->
                var loaded: List<CustomCardItem> = try {
                    Json.decodeFromString(json)
                } catch (e: Exception) {
                    emptyList()
                }

                if (isFirstCustomCardLoad) {
                    isFirstCustomCardLoad = false
                    if (loaded.any { it.unitPrice != 0.0 }) {
                        loaded = loaded.map { it.copy(unitPrice = 0.0) }
                        dataStoreManager.saveCustomCards(Json.encodeToString(loaded))
                    }
                }

                customCards = loaded
                // Sync loaded custom card prices to priceMap only if not already equivalent
                loaded.forEach { card ->
                    val id = "custom_${card.id}"
                    val currentStr = priceMap[id]
                    val currentVal = currentStr?.toDoubleOrNull() ?: 0.0
                    if (card.unitPrice > 0 && (currentStr == null || currentVal != card.unitPrice)) {
                         priceMap[id] = card.unitPrice.toString().removeSuffix(".0")
                    }
                }
                // Custom card logic is in CalculatorCustomCardLogic.kt
                updateCustomCardResults()
            }
        }
        viewModelScope.launch {
            dataStoreManager.hiddenCards.collectLatest { csv ->
                hiddenCardIds = if (csv.isBlank()) emptySet() else csv.split(",").toSet()
                // Card management logic is in CalculatorCardLogic.kt
                rebuildOrderedVisibleItems()
            }
        }
        viewModelScope.launch {
            dataStoreManager.cardOrder.collectLatest { csv ->
                cardOrder = if (csv.isBlank()) emptyList() else csv.split(",")
                // Card management logic is in CalculatorCardLogic.kt
                rebuildOrderedVisibleItems()
            }
        }
        viewModelScope.launch {
            dataStoreManager.pinnedCards.collectLatest { csv ->
                pinnedCardIds = if (csv.isBlank()) emptySet() else csv.split(",").toSet()
                // Card management logic is in CalculatorCardLogic.kt
                rebuildOrderedVisibleItems()
            }
        }
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
                // Calculation logic is in CalculatorCalculationLogic.kt
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
        totalLengthInput = cleaned
        // Calculation logic is in CalculatorCalculationLogic.kt
        calculateValues()
    }

    fun applyTotalLength() {
        // Calculation logic is in CalculatorCalculationLogic.kt
        calculateValues()
    }

    fun clearTotalLength() {
        totalLengthInput = ""
        totalLengthDraft = ""
        // Calculation logic is in CalculatorCalculationLogic.kt
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

    // Calculation logic is in CalculatorCalculationLogic.kt
    fun onPriceChange(id: String, v: String) = onPriceChangeExt(id, v)

    // Calculation logic is in CalculatorCalculationLogic.kt
    private fun updateIfValid(value: String, setter: (String) -> Unit) = updateIfValidExt(value, setter)

    // Calculation logic is in CalculatorCalculationLogic.kt
    fun getPriceString(id: String): String = getPriceStringExt(id)

    // Calculation logic is in CalculatorCalculationLogic.kt
    internal fun calculateValues() = calculateValuesExt()

    // Calculation logic is in CalculatorCalculationLogic.kt
    fun scanQrCode(bitmap: android.graphics.Bitmap) = scanQrCodeExt(bitmap)
}