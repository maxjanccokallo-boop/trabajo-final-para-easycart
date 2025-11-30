package com.example.easycart.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.easycart.data.model.CartItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object InvoicePdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        items: List<CartItem>,
        total: Double
    ): File? {

        if (items.isEmpty()) return null

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 22f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 13f
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val now = dateFormat.format(Date())

        var y = 60f

        // ENCABEZADO -------------------------
        canvas.drawText("Boleta de Venta - EasyCart", 40f, y, titlePaint)
        y += 25f

        canvas.drawText("Fecha: $now", 40f, y, textPaint)
        y += 20f

        canvas.drawText("---------------------------------------------", 40f, y, textPaint)
        y += 20f

        // COLUMNAS -------------------------
        canvas.drawText("Producto", 40f, y, textPaint)
        canvas.drawText("Cant.", 250f, y, textPaint)
        canvas.drawText("P. Unit", 320f, y, textPaint)
        canvas.drawText("Total", 430f, y, textPaint)
        y += 15f

        canvas.drawText("---------------------------------------------", 40f, y, textPaint)
        y += 20f

        // ITEMS ----------------------------
        items.forEach { item ->
            if (y > 750f) return@forEach

            canvas.drawText(item.productName, 40f, y, textPaint)
            canvas.drawText(item.quantity.toString(), 250f, y, textPaint)
            canvas.drawText("S/ %.2f".format(item.finalUnitPrice), 320f, y, textPaint)
            canvas.drawText("S/ %.2f".format(item.totalPrice), 430f, y, textPaint)

            y += 18f
        }

        y += 10f
        canvas.drawText("---------------------------------------------", 40f, y, textPaint)
        y += 25f

        // TOTAL ----------------------------
        titlePaint.textSize = 18f
        canvas.drawText("TOTAL: S/ %.2f".format(total), 40f, y, titlePaint)

        document.finishPage(page)

        // GUARDAR PDF ------------------------
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir

        val fileName = "boleta_easycart_${System.currentTimeMillis()}.pdf"
        val file = File(dir, fileName)

        return try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }
}
