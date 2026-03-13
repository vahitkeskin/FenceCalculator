package com.vahitkeskin.fencecalculator.util

import android.content.Context
import android.content.Intent
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import java.text.DecimalFormat

fun shareCalculationResults(
    context: Context,
    results: List<CalculationItem>,
    totalCost: Double,
    length: String,
    strings: com.vahitkeskin.fencecalculator.util.AppStrings? = null
) {
    val currentStrings = strings ?: com.vahitkeskin.fencecalculator.util.Localization.getStrings(java.util.Locale.getDefault().language)
    val df = java.text.DecimalFormat("#,##0.##")
    val cf = java.text.DecimalFormat("#,##0.00")

    val report = StringBuilder()
    report.append("🏗️ ${currentStrings.shareReportTitle}\n")
    report.append("=========================\n")
    report.append("${currentStrings.shareLandLength} $length ${currentStrings.unitMeter}\n\n")

    report.append("📋 ${currentStrings.shareMaterialList}\n")
    results.forEach { item ->
        if (item.id != "kafes_kg") {
            report.append("- ${item.title}: ${df.format(item.quantity)} ${item.unit}\n")
            if (item.totalCost > 0) {
                report.append("  ${String.format(currentStrings.shareAmountLabel, cf.format(item.totalCost))}\n")
            }
        }
    }

    report.append("\n=========================\n")
    report.append("${String.format(currentStrings.shareGrandTotal, cf.format(totalCost))}\n")
    report.append("=========================\n")
    report.append(currentStrings.shareCalculatedWith)

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, report.toString())
        putExtra(Intent.EXTRA_SUBJECT, currentStrings.shareReportSubject)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, currentStrings.shareReportChooser)
    context.startActivity(shareIntent)
}