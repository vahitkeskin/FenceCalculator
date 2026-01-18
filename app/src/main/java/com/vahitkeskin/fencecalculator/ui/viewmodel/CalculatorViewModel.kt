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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    // --- CONSTANTS ---
    object Defaults {
        const val LENGTH = "300"
        const val SPACING = "3.5"
        const val HEIGHT = "1.5"
        const val STRUT_INTERVAL = "15"
        const val STRUT_COUNT = "2"
        const val MESH_ROLL = "20"
        const val BARBED_ROWS = "3"
        const val BARBED_ROLL = "250"
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

    private var priceMap = mutableMapOf<String, String>()
    var results by mutableStateOf<List<CalculationItem>>(emptyList()); private set
    var grandTotalCost by mutableStateOf(0.0); private set

    init { calculateValues() }

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
        val spacing = poleSpacingInput.toDoubleOrNull() ?: 0.0
        val strutFreq = strutIntervalInput.toDoubleOrNull() ?: 15.0
        val strutCnt = strutCountInput.toDoubleOrNull() ?: 2.0
        val meshLen = meshRollLengthInput.toDoubleOrNull() ?: 20.0
        val barbedRows = barbedWireRowsInput.toDoubleOrNull() ?: 3.0
        val barbedLen = barbedWireRollLengthInput.toDoubleOrNull() ?: 250.0
        val thickness = wireThicknessInput.toDoubleOrNull() ?: 2.5
        val constant = weightConstantInput.toDoubleOrNull() ?: 1.3
        val eye = meshEyeInput.toDoubleOrNull() ?: 6.5

        if (length == 0.0 || spacing <= 0.0 || height == 0.0) {
            results = emptyList()
            grandTotalCost = 0.0
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
        val rawGergi = length / 6.66
        val gergiTeli = ceilAndLog("Gergi Teli (Kg)", rawGergi)

        // 7. Bağlama Teli (Kg)
        val rawBaglama = gergiTeli / 3.0 // Yuvarlanmış gergi üzerinden hesaplamak daha güvenli
        val baglamaTeli = ceilAndLog("Bağlama Teli (Kg)", rawBaglama)

        println("----------------- HESAPLAMA BİTTİ -----------------")

        fun getP(id: String) = priceMap[id]?.toDoubleOrNull() ?: 0.0

        val list = mutableListOf(
            createItem("direk", "Direk", "Her $spacing m'de bir", direkSayisi, "Adet", Icons.Filled.Straighten, Color(0xFF3F51B5), ::getP),
            createItem("payanda", "Payanda", "Her $strutFreq direkte $strutCnt adet", payandaSayisi, "Adet", Icons.Filled.ChangeHistory, Color(0xFF9C27B0), ::getP),
            createItem("kafes_top", "Kafes Tel (Toplam)", "$meshLen m'lik top ($height m Yükseklik)", kafesTopSayisi, "Top", Icons.Filled.GridOn, Color(0xFF009688), ::getP),
            // Dikkat: oneRollWeight sadece bilgi amaçlı, toplam maliyete doğrudan adet olarak eklenmiyor, birim fiyat belirlemede kullanılıyor.
            createItem("kafes_kg", "1 Top Tel Ağırlığı", "1 Rulo ($meshLen m) ağırlığıdır.", oneRollWeight, "Kg", Icons.Filled.Scale, Color(0xFF00796B), ::getP),
            createItem("diken", "Dikenli Tel", "${barbedRows.toInt()} Sıra ($barbedLen m/top)", dikenliTelTopSayisi, "Top", Icons.Filled.Warning, Color(0xFFD32F2F), ::getP),
            createItem("gergi", "Gergi Teli", "Uzunluk / 6.66", gergiTeli, "Kg", Icons.Filled.LinearScale, Color(0xFFFF9800), ::getP),
            createItem("baglama", "Bağlama Teli", "Gergi Telinin 1/3'ü", baglamaTeli, "Kg", Icons.Filled.AllInclusive, Color(0xFF795548), ::getP)
        )

        results = list
        grandTotalCost = list.sumOf { it.totalCost }
    }

    private fun createItem(id: String, t: String, d: String, q: Double, u: String, i: ImageVector, c: Color, p: (String) -> Double) =
        CalculationItem(id, t, d, q, u, p(id), q * p(id), i, c)
}