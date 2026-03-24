package com.vahitkeskin.fencecalculator.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.ceil
import kotlin.math.max

fun CalculatorViewModel.ceilAndLogExt(label: String, rawValue: Double): Double {
    val roundedValue = ceil(rawValue) // Yukarı Yuvarla (3.1 -> 4.0)
    // Logcat'e Yazdır
    println("HESAPLAMA LOG: $label -> Eski: $rawValue, Yeni: $roundedValue")
    return roundedValue
}

fun CalculatorViewModel.calculateValuesExt() {
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
    val direkSayisi = ceilAndLogExt("Direk Sayısı", rawDirek)

    // 2. Payanda (Adet)
    val rawPayanda = (direkSayisi / strutFreq) * strutCnt
    val payandaSayisi = ceilAndLogExt("Payanda Sayısı", rawPayanda)

    // 3. Kafes Tel (Top)
    val rawKafesTop = length / meshLen
    val kafesTopSayisi = ceilAndLogExt("Kafes Tel (Top)", rawKafesTop)

    // 4. Kafes Tel (1 Top Ağırlığı - Kg)
    // m2 Ağırlığı
    val unitWeightPerM2 = (thickness * thickness * constant) / eye
    val oneRollArea = meshLen * height
    val rawOneRollWeight = unitWeightPerM2 * oneRollArea
    val oneRollWeight = ceilAndLogExt("1 Top Tel Ağırlığı (Kg)", rawOneRollWeight)

    // 5. Dikenli Tel (Top)
    val rawDiken = (barbedRows * length) / barbedLen
    val dikenliTelTopSayisi = ceilAndLogExt("Dikenli Tel (Top)", rawDiken)

    // 6. Gergi Teli (Kg)
    val rawGergi = length / tFactor
    val gergiTeli = ceilAndLogExt("Gergi Teli (Kg)", rawGergi)

    // 7. Bağlama Teli (Kg)
    val rawBaglama = gergiTeli / bFactor // Yuvarlanmış gergi üzerinden hesaplamak daha güvenli
    val baglamaTeli = ceilAndLogExt("Bağlama Teli (Kg)", rawBaglama)

    // 8. Çimento (50 Kg - Adet)
    val rawCimento = direkSayisi / cemFactor
    val cimentoSayisi = ceilAndLogExt("Çimento Sayısı", rawCimento)

    // 9. Hazır Beton (m3)
    val rawBeton = direkSayisi / concFactor
    val hazirBetonM3 = ceilAndLogExt("Hazır Beton (m3)", rawBeton)

    // 10. Boy Demir Boru (6m)
    // Her direk pLength, her boru pipeLen.
    val rawBoyDemir = (direkSayisi * pLength) / pipeLen
    val boyDemirSayisi = ceilAndLogExt("Boy Demir Boru ($pipeLen m)", rawBoyDemir)

    println("----------------- HESAPLAMA BİTTİ -----------------")

    fun getP(id: String) = priceMap[id]?.toDoubleOrNull() ?: 0.0

    val list = mutableListOf(
        createItemExt(
            "direk",
            strings.direkTitle,
            strings.direkSummary,
            direkSayisi,
            strings.unitPiece,
            Icons.Filled.Straighten,
            Color(0xFF3F51B5),
            strings.catMetal,
            String.format(strings.direkDesc, spacing.toString()),
            ::getP
        ),
        createItemExt(
            "boy_demir",
            strings.boyDemirTitle,
            strings.boyDemirSummary,
            boyDemirSayisi,
            strings.unitPiece,
            Icons.Filled.FormatLineSpacing,
            Color(0xFF5C6BC0),
            strings.catMetal,
            String.format(strings.boyDemirDesc, pipeLen.toString(), pLength.toString()),
            ::getP
        ),
        createItemExt(
            "payanda",
            strings.payandaTitle,
            strings.payandaSummary,
            payandaSayisi,
            strings.unitPiece,
            Icons.Filled.ChangeHistory,
            Color(0xFF9C27B0),
            strings.catMetal,
            String.format(
                strings.payandaDesc,
                strutFreq.toInt().toString(),
                strutCnt.toInt().toString()
            ),
            ::getP
        ),

        createItemExt(
            "kafes_top",
            strings.kafesTopTitle,
            strings.kafesTopSummary,
            kafesTopSayisi,
            strings.unitRoll,
            Icons.Filled.GridOn,
            Color(0xFF009688),
            strings.catWire,
            String.format(strings.kafesTopDesc, meshLen.toString(), height.toString()),
            ::getP
        ),
        createItemExt(
            "kafes_kg",
            strings.kafesKgTitle,
            strings.kafesKgSummary,
            oneRollWeight,
            strings.unitKg,
            Icons.Filled.Scale,
            Color(0xFF00796B),
            strings.catWire,
            String.format(strings.kafesKgDesc, meshLen.toString()),
            ::getP
        ),
        createItemExt(
            "diken",
            strings.dikenTitle,
            strings.dikenSummary,
            dikenliTelTopSayisi,
            strings.unitRoll,
            Icons.Filled.Warning,
            Color(0xFFD32F2F),
            strings.catWire,
            String.format(strings.dikenDesc, barbedRows.toInt(), barbedLen.toString()),
            ::getP
        ),
        createItemExt(
            "gergi",
            strings.gergiTitle,
            strings.gergiSummary,
            gergiTeli,
            strings.unitKg,
            Icons.Filled.LinearScale,
            Color(0xFFFF9800),
            strings.catWire,
            String.format(strings.gergiDesc, tFactor.toString()),
            ::getP
        ),
        createItemExt(
            "baglama",
            strings.baglamaTitle,
            strings.baglamaSummary,
            baglamaTeli,
            strings.unitKg,
            Icons.Filled.Link,
            Color(0xFF795548),
            strings.catWire,
            String.format(strings.baglamaDesc, bFactor.toInt().toString()),
            ::getP
        ),

        createItemExt(
            "cimento",
            strings.cimentoTitle,
            strings.cimentoSummary,
            cimentoSayisi,
            strings.unitPiece,
            Icons.Filled.Egg,
            Color(0xFF607D8B),
            strings.catConstruction,
            strings.cimentoDesc,
            ::getP
        ),
        createItemExt(
            "beton",
            strings.betonTitle,
            strings.betonSummary,
            hazirBetonM3,
            strings.unitM3,
            Icons.Filled.Layers,
            Color(0xFF455A64),
            strings.catConstruction,
            strings.betonDesc,
            ::getP
        )
    )

    results = list
    updateCustomCardResultsExt() // Özel kartları da güncelle (bağımlılıklar için)
    rebuildOrderedVisibleItemsExt()
}

fun CalculatorViewModel.createItemExt(
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

fun CalculatorViewModel.onPriceChangeExt(id: String, v: String) {
    val cleaned = v.replace(',', '.').filter { it.isDigit() || it == '.' }
    priceMap[id] = cleaned
    if (id.startsWith("custom_")) {
        val realId = id.removePrefix("custom_")
        val card = getCustomCardByIdExt(realId)
        if (card != null) {
            val newPrice = cleaned.toDoubleOrNull() ?: 0.0
            val updatedCard = card.copy(unitPrice = newPrice)
            val current = customCards.toMutableList()
            val index = current.indexOfFirst { it.id == realId }
            if (index >= 0) {
                current[index] = updatedCard
                customCards = current
                // Güncelle ve hesabı çalıştır
                updateCustomCardResultsExt()
                viewModelScope.launch {
                    dataStoreManager.saveCustomCards(Json.encodeToString(current))
                }
            }
        }
    } else {
        calculateValuesExt()
    }
}

fun CalculatorViewModel.isValidExt(input: String): Boolean {
    if (input.isEmpty()) return true
    return input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1
}

fun CalculatorViewModel.updateIfValidExt(value: String, setter: (String) -> Unit) {
    if (isValidExt(value)) {
        setter(value)
        calculateValuesExt()
    }
}

fun CalculatorViewModel.getPriceStringExt(id: String): String {
    return priceMap[id] ?: if (id.startsWith("custom_")) {
        val realId = id.removePrefix("custom_")
        val card = getCustomCardByIdExt(realId)
        if (card != null && card.unitPrice > 0) {
            val formatted = card.unitPrice.toString()
            if (formatted.endsWith(".0")) formatted.removeSuffix(".0") else formatted
        } else ""
    } else ""
}

fun CalculatorViewModel.scanQrCodeExt(bitmap: android.graphics.Bitmap) {
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
