package com.vahitkeskin.fencecalculator.util

import android.content.Context
import android.content.Intent
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import java.text.DecimalFormat

fun shareCalculationResults(context: Context, results: List<CalculationItem>, totalCost: Double, length: String) {
    val df = DecimalFormat("#,##0.##")
    val cf = DecimalFormat("#,##0.00")

    val report = StringBuilder()
    report.append("🏗️ ÇİT MALİYET RAPORU\n")
    report.append("=========================\n")
    report.append("Arazi Uzunluğu: $length m\n\n")

    report.append("📋 MALZEME LİSTESİ:\n")
    results.forEach { item ->
        if (item.id != "kafes_kg") {
            report.append("- ${item.title}: ${df.format(item.quantity)} ${item.unit}\n")
            if (item.totalCost > 0) {
                report.append("  Tutar: ${cf.format(item.totalCost)} ₺\n")
            }
        }
    }

    report.append("\n=========================\n")
    report.append("💰 GENEL TOPLAM: ${cf.format(totalCost)} ₺\n")
    report.append("=========================\n")
    report.append("Vahit Keskin Tel Çit Hesaplama ile hesaplandı.")

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, report.toString())
        putExtra(Intent.EXTRA_SUBJECT, "Çit Maliyet Teklifi")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Raporu Paylaş")
    context.startActivity(shareIntent)
}