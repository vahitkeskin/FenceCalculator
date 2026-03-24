package com.vahitkeskin.fencecalculator.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.vahitkeskin.fencecalculator.data.model.CalculationItem
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.vahitkeskin.fencecalculator.R
import com.vahitkeskin.fencecalculator.util.AppStrings

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
        customerName: String = "",
        companyName: String = "",
        viewModel: com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
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
        val mainTitle =
            if (companyName.isNotBlank()) companyName.uppercase() else viewModel.strings.pdfDefaultTitle
        canvas.drawText(mainTitle, 30f, 75f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.alpha = 200
        canvas.drawText(customerTitle, 30f, 105f, paint)

        // Tarih Bilgisi
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale(viewModel.selectedLanguage.code))
        val currentDate = sdf.format(Date())
        paint.textSize = 12f
        canvas.drawText(currentDate, 30f, 125f, paint)
        paint.alpha = 255

        // 3. Bilgi Kartı (Uzunluk Bilgisi ve Müşteri Adı Soyadı)
        val hasCustomerName = customerName.isNotBlank()
        val cardBottom = if (hasCustomerName) 225f else 200f

        paint.color = Color.WHITE
        paint.clearShadowLayer()
        canvas.drawRoundRect(30f, 140f, 565f, cardBottom, 10f, 10f, paint)

        // Uzunluk Bilgisi
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(viewModel.strings.pdfTotalLengthLabel, 50f, 175f, paint)

        paint.color = android.graphics.Color.parseColor("#3F51B5")
        paint.textSize = 16f
        canvas.drawText("$length ${viewModel.strings.unitMeter}", 220f, 175f, paint)

        // Müşteri Adı Soyadı (Varsa)
        if (hasCustomerName) {
            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(viewModel.strings.customerNameSurname + ":", 50f, 205f, paint)

            paint.color = android.graphics.Color.parseColor("#3F51B5")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(customerName, 220f, 205f, paint)
        }

        // 4. TABLO BAŞLIKLARI
        var yPos = if (hasCustomerName) 275f else 250f
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
        canvas.drawText(viewModel.strings.pdfHeaderMaterial, xMaterial, yPos, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(viewModel.strings.pdfHeaderQty, xQtyEnd, yPos, paint)
        canvas.drawText(viewModel.strings.pdfHeaderUnitPrice, xUnitEnd, yPos, paint)
        canvas.drawText(viewModel.strings.pdfHeaderTotal, xTotalEnd, yPos, paint)

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
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val displayName =
                    if (item.title.length > 28) item.title.take(28) + "..." else item.title
                canvas.drawText(displayName, xMaterial, yPos, paint)

                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${dfQty.format(item.quantity)} ${item.unit}", xQtyEnd, yPos, paint)
                canvas.drawText(
                    String.format(
                        viewModel.strings.currencyFormat,
                        dfPrice.format(unitPrice)
                    ), xUnitEnd, yPos, paint
                )

                canvas.drawText(
                    String.format(
                        viewModel.strings.currencyFormat,
                        dfPrice.format(item.totalCost)
                    ), xTotalEnd, yPos, paint
                )

                // --- Alt Bilgi (Açıklama ve Bağımlılık) ---
                var subTextLines = 0
                if (!item.description.isNullOrBlank()) {
                    yPos += 14f
                    paint.textAlign = Paint.Align.LEFT
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8.5f
                    paint.color = Color.GRAY
                    val displayDescription = if (item.description.length > 70) item.description.take(67) + "..." else item.description
                    canvas.drawText(displayDescription, xMaterial, yPos, paint)
                    subTextLines++
                }

                if (!item.dependencyInfo.isNullOrBlank()) {
                    yPos += 11f
                    paint.textAlign = Paint.Align.LEFT
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 7.5f
                    paint.color = android.graphics.Color.parseColor("#9E9E9E") // Subtle gray
                    val displayDep = if (item.dependencyInfo.length > 85) item.dependencyInfo.take(82) + "..." else item.dependencyInfo
                    canvas.drawText(displayDep, xMaterial, yPos, paint)
                    subTextLines++
                }

                // Reset paint for next item
                paint.color = Color.BLACK
                paint.textSize = 12f
                yPos += if (subTextLines > 0) 20f else 25f
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
        canvas.drawText(viewModel.strings.pdfGrandTotal, 30f, yPos + 35f, paint)

        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = android.graphics.Color.parseColor("#3F51B5")
        canvas.drawText(
            String.format(viewModel.strings.currencyFormat, dfPrice.format(totalCost)),
            565f,
            yPos + 38f,
            paint
        )

        pdfDocument.finishPage(page)

        // Dinamik dosya adı
        val defaultFileName = viewModel.strings.pdfDefaultTitle.replace("\\s+".toRegex(), "_")
        val safeCompanyName = if (companyName.isBlank()) defaultFileName else companyName.trim()
            .replace("\\s+".toRegex(), "_")
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

    fun shareViaWhatsApp(
        context: Context,
        file: File,
        phoneNumber: String,
        iban: String,
        viewModel: com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        // Telefon numarasını temizle (sadece rakamlar)
        val cleanPhone = phoneNumber.filter { it.isDigit() }
        val finalPhone =
            if (cleanPhone.startsWith("0")) "9$cleanPhone" else if (!cleanPhone.startsWith("90")) "90$cleanPhone" else cleanPhone

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, iban)
            putExtra(Intent.EXTRA_SUBJECT, viewModel.strings.pdfSharingSubject)
            setPackage("com.whatsapp")
            putExtra("jid", "$finalPhone@s.whatsapp.net")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // WhatsApp şu an PDF'lerde Intent üzerinden gelen başlığı (caption) yoksayıyor.
        // Bu yüzden metni panoya kopyalıyoruz, kullanıcı "Açıklama ekleyin" alanına doğrudan yapıştırabilir.
        if (iban.isNotBlank()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("IBAN", iban)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, viewModel.strings.pdfIbanCopiedToast, Toast.LENGTH_LONG).show()
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // WhatsApp yüklü değilse normal paylaşımı aç
            sharePdfFile(context, file, viewModel)
        }
    }

    fun sharePdfFile(
        context: Context,
        file: File,
        viewModel: com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val strings = viewModel.strings
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, strings.pdfShareFileSubject)
            putExtra(
                Intent.EXTRA_TEXT,
                strings.pdfShareFileText
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, strings.pdfShareChooserTitle))
    }
}