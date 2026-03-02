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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    /**
     * Generates a PDF and returns the file path.
     */
    fun generatePdf(
        context: Context,
        results: List<CalculationItem>,
        totalCost: Double,
        length: String,
        customerTitle: String,
        companyName: String = ""
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Boyutu
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // --- TASARIM BAŞLANGICI ---

        // 1. Üst Dalga Tasarımı
        paint.color = android.graphics.Color.parseColor("#3F51B5")
        paint.style = Paint.Style.FILL
        val pathBoundary = Path()
        pathBoundary.moveTo(0f, 0f)
        pathBoundary.lineTo(0f, 180f)
        pathBoundary.cubicTo(150f, 220f, 400f, 100f, 595f, 160f)
        pathBoundary.lineTo(595f, 0f)
        pathBoundary.close()
        canvas.drawPath(pathBoundary, paint)

        // 2. Başlık ve Alt Başlık
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val mainTitle = if (companyName.isNotBlank()) companyName.uppercase() else "ÇİT HESAPLAMA"
        canvas.drawText(mainTitle, 30f, 75f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.alpha = 200
        canvas.drawText(customerTitle, 30f, 105f, paint)

        // Tarih Bilgisi
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
        val currentDate = sdf.format(Date())
        paint.textSize = 12f
        canvas.drawText(currentDate, 30f, 125f, paint)
        paint.alpha = 255

        // 3. Bilgi Kartı (Uzunluk Bilgisi)
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

        // 4. TABLO BAŞLIKLARI
        var yPos = 250f
        val dfQty = DecimalFormat("#,##0")     // Miktar için (Virgülsüz)
        val dfPrice = DecimalFormat("#,##0.00") // Para birimleri için (Kuruşlu)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.DKGRAY

        // Sütun X Koordinatları
        val xMaterial = 30f
        val xQtyEnd = 320f      // Miktar sütunu bitişi (Sağa dayalı olacak)
        val xUnitEnd = 430f     // Birim Fiyat sütunu bitişi (Sağa dayalı)
        val xTotalEnd = 565f    // Tutar sütunu bitişi (Sağa dayalı)

        // Başlıkları Çiz
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("MALZEME", xMaterial, yPos, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("MİKTAR", xQtyEnd, yPos, paint)
        canvas.drawText("BİRİM FİYAT", xUnitEnd, yPos, paint) 
        canvas.drawText("TUTAR", xTotalEnd, yPos, paint)

        // Çizgi Çek
        yPos += 10f
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        canvas.drawLine(30f, yPos, 565f, yPos, paint)
        yPos += 25f

        // 5. TABLO İÇERİĞİ
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.BLACK
        paint.textSize = 12f

        results.forEach { item ->
            if (item.totalCost > 0.0) {
                val unitPrice = if (item.quantity != 0.0) item.totalCost / item.quantity else 0.0

                paint.textAlign = Paint.Align.LEFT
                val displayName = if (item.title.length > 28) item.title.take(28) + "..." else item.title
                canvas.drawText(displayName, xMaterial, yPos, paint)

                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${dfQty.format(item.quantity)} ${item.unit}", xQtyEnd, yPos, paint)
                canvas.drawText("${dfPrice.format(unitPrice)} ₺", xUnitEnd, yPos, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("${dfPrice.format(item.totalCost)} ₺", xTotalEnd, yPos, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                yPos += 30f
            }
        }

        // 6. GENEL TOPLAM ALANI
        yPos += 20f
        paint.color = android.graphics.Color.parseColor("#F5F5F5")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, yPos, 595f, yPos + 60f, paint)

        paint.color = android.graphics.Color.parseColor("#3F51B5")
        canvas.drawRect(0f, yPos, 5f, yPos + 60f, paint)

        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("GENEL TOPLAM MALİYET", 30f, yPos + 35f, paint)

        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = android.graphics.Color.parseColor("#3F51B5")
        canvas.drawText("${dfPrice.format(totalCost)} ₺", 565f, yPos + 38f, paint)

        pdfDocument.finishPage(page)

        // Dinamik dosya adı
        val safeCompanyName = if (companyName.isBlank()) "Cit_Hesaplama" else companyName.trim().replace("\\s+".toRegex(), "_")
        val fileName = "${safeCompanyName}.pdf"
        
        val file = File(context.cacheDir, fileName)
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }

    fun sharePdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Çit Maliyet Teklifi")
            putExtra(Intent.EXTRA_TEXT, "Ekte çit maliyet hesaplama detaylı raporunu bulabilirsiniz.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "PDF Raporunu Paylaş"))
    }
}