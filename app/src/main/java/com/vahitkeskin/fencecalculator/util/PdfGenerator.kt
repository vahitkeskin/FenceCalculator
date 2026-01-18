package com.vahitkeskin.fencecalculator.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object PdfGenerator {

    fun generateAndSharePdf(
        context: Context,
        results: List<CalculationItem>,
        totalCost: Double,
        length: String
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // --- TASARIM ---

        // 1. Üst Dalga
        paint.color = android.graphics.Color.parseColor("#3F51B5")
        paint.style = Paint.Style.FILL
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(0f, 180f)
        path.cubicTo(150f, 220f, 400f, 100f, 595f, 160f)
        path.lineTo(595f, 0f)
        path.close()
        canvas.drawPath(path, paint)

        // 2. Başlıklar
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ÇİT MALİYET RAPORU", 30f, 60f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.alpha = 200
        canvas.drawText("Vahit Keskin Fence Calculator", 30f, 85f, paint)
        paint.alpha = 255

        // 3. Bilgi Kartı
        paint.color = Color.WHITE
        paint.clearShadowLayer()
        canvas.drawRoundRect(30f, 140f, 565f, 200f, 10f, 10f, paint)

        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Toplam Arazi Uzunluğu:", 50f, 175f, paint)

        paint.color = android.graphics.Color.parseColor("#3F51B5")
        paint.textSize = 16f
        canvas.drawText("$length Metre", 200f, 175f, paint)

        // 4. Tablo
        var yPos = 250f
        val df = DecimalFormat("#,##0") // Adetler tam sayı görünsün
        val cf = DecimalFormat("#,##0.00")

        // Başlıklar
        paint.color = Color.DKGRAY
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MALZEME", 30f, yPos, paint)
        canvas.drawText("MİKTAR", 300f, yPos, paint)
        canvas.drawText("TUTAR", 480f, yPos, paint)

        yPos += 10f
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        canvas.drawLine(30f, yPos, 565f, yPos, paint)
        yPos += 25f

        // İçerik
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.BLACK
        paint.textSize = 12f

        results.forEach { item ->
            // FİLTRELEME: Sadece tutarı 0'dan büyük olanları yazdır
            if (item.totalCost > 0.0) {
                // Başlık
                canvas.drawText(item.title, 30f, yPos, paint)

                // Miktar
                canvas.drawText("${df.format(item.quantity)} ${item.unit}", 300f, yPos, paint)

                // Tutar
                val costStr = "${cf.format(item.totalCost)} ₺"
                val textWidth = paint.measureText(costStr)
                canvas.drawText(costStr, 565f - textWidth, yPos, paint)

                yPos += 30f
            }
        }

        // 5. Genel Toplam
        yPos += 20f
        paint.color = android.graphics.Color.parseColor("#1E1E1E")
        canvas.drawRect(0f, yPos, 595f, yPos + 60f, paint)

        paint.color = Color.WHITE
        paint.textSize = 14f
        canvas.drawText("GENEL TOPLAM MALİYET", 30f, yPos + 35f, paint)

        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val totalStr = "${cf.format(totalCost)} ₺"
        val totalWidth = paint.measureText(totalStr)
        canvas.drawText(totalStr, 565f - totalWidth, yPos + 38f, paint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Cit_Maliyet_Teklifi.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        sharePdfFile(context, file)
    }

    private fun sharePdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Çit Maliyet Teklifi")
            putExtra(Intent.EXTRA_TEXT, "Ekte çit maliyet hesaplama raporunu bulabilirsiniz.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "PDF Raporunu Paylaş"))
    }
}