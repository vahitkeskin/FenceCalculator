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
        length: String,
        customerTitle: String
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Boyutu
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // --- TASARIM BAŞLANGICI ---

        // 1. Üst Dalga Tasarımı
        paint.color = android.graphics.Color.parseColor("#3F51B5")
        paint.style = Paint.Style.FILL
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(0f, 180f)
        path.cubicTo(150f, 220f, 400f, 100f, 595f, 160f)
        path.lineTo(595f, 0f)
        path.close()
        canvas.drawPath(path, paint)

        // 2. Başlık ve Alt Başlık
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ÇİT MALİYET RAPORU", 30f, 60f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.alpha = 200
        canvas.drawText(customerTitle, 30f, 85f, paint)
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
        canvas.drawText("BİRİM FİYAT", xUnitEnd, yPos, paint) // YENİ SÜTUN
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
            // Sadece tutarı 0'dan büyük olan kalemleri göster
            if (item.totalCost > 0.0) {

                // Birim Fiyatı Hesapla (Tutar / Miktar)
                val unitPrice = if (item.quantity != 0.0) item.totalCost / item.quantity else 0.0

                // 1. Malzeme Adı (Sola Dayalı)
                paint.textAlign = Paint.Align.LEFT
                // Eğer isim çok uzunsa diye basit bir kısaltma önlemi (opsiyonel)
                val displayName = if (item.title.length > 28) item.title.take(28) + "..." else item.title
                canvas.drawText(displayName, xMaterial, yPos, paint)

                // 2. Miktar (Sağa Dayalı - Birim ile beraber)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${dfQty.format(item.quantity)} ${item.unit}", xQtyEnd, yPos, paint)

                // 3. Birim Fiyat (Sağa Dayalı) - YENİ EKLENEN KISIM
                canvas.drawText("${dfPrice.format(unitPrice)} ₺", xUnitEnd, yPos, paint)

                // 4. Toplam Tutar (Sağa Dayalı)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) // Tutarı kalın yapalım
                canvas.drawText("${dfPrice.format(item.totalCost)} ₺", xTotalEnd, yPos, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) // Normale dön

                // Satır boşluğu
                yPos += 30f
            }
        }

        // 6. GENEL TOPLAM ALANI
        yPos += 20f

        // Gri arka plan şeridi
        paint.color = android.graphics.Color.parseColor("#F5F5F5")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, yPos, 595f, yPos + 60f, paint)

        // Koyu şerit çizgisi
        paint.color = android.graphics.Color.parseColor("#3F51B5")
        canvas.drawRect(0f, yPos, 5f, yPos + 60f, paint)

        // Etiket
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("GENEL TOPLAM MALİYET", 30f, yPos + 35f, paint)

        // Değer
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = android.graphics.Color.parseColor("#3F51B5")
        canvas.drawText("${dfPrice.format(totalCost)} ₺", 565f, yPos + 38f, paint)

        // --- BİTİŞ ---
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
            putExtra(Intent.EXTRA_TEXT, "Ekte çit maliyet hesaplama detaylı raporunu bulabilirsiniz.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "PDF Raporunu Paylaş"))
    }
}