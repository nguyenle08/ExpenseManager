package com.example.expensemanager.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.expensemanager.feature.report.CategoryStatUi
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfHelper {
    
    /**
     * Export report to PDF file
     */
    fun exportReportToPdf(
        context: Context,
        month: Int,
        year: Int,
        categoryStats: List<CategoryStatUi>,
        total: Long,
        isIncome: Boolean,
        language: String
    ): Boolean {
        return try {
            // Create PDF document
            val pdfDocument = PdfDocument()
            
            // Page info (A4 size)
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Paint objects
            val titlePaint = Paint().apply {
                textSize = 24f
                isFakeBoldText = true
            }
            val headerPaint = Paint().apply {
                textSize = 16f
                isFakeBoldText = true
            }
            val normalPaint = Paint().apply {
                textSize = 12f
            }
            val linePaint = Paint().apply {
                strokeWidth = 2f
            }
            
            var yPosition = 50f
            
            // Title
            val title = if (language == "vi") {
                "BÁO CÁO ${if (isIncome) "THU NHẬP" else "CHI TIÊU"} THÁNG $month/$year"
            } else {
                "${if (isIncome) "INCOME" else "EXPENSE"} REPORT $month/$year"
            }
            canvas.drawText(title, 50f, yPosition, titlePaint)
            yPosition += 40f
            
            // Draw line
            canvas.drawLine(50f, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f
            
            // Summary section
            canvas.drawText(
                if (language == "vi") "TỔNG QUAN" else "SUMMARY",
                50f,
                yPosition,
                headerPaint
            )
            yPosition += 30f
            
            // Total amount
            val totalText = if (language == "vi") {
                "Tổng ${if (isIncome) "Thu" else "Chi"}: ${if (isIncome) "+" else "-"}${CurrencyFormatter.format(total, context)}"
            } else {
                "Total ${if (isIncome) "Income" else "Expense"}: ${if (isIncome) "+" else "-"}${CurrencyFormatter.format(total, context)}"
            }
            canvas.drawText(totalText, 70f, yPosition, normalPaint)
            yPosition += 40f
            
            // Transaction details
            canvas.drawText(
                if (language == "vi") "CHI TIẾT THEO DANH MỤC" else "DETAILS BY CATEGORY",
                50f,
                yPosition,
                headerPaint
            )
            yPosition += 30f
            
            // Category statistics
            if (categoryStats.isNotEmpty()) {
                for (stat in categoryStats) {
                    if (yPosition > 780f) break // Avoid overflow
                    
                    val categoryText = "${stat.name}: ${CurrencyFormatter.format(stat.amount, context)} (${stat.percentage.toInt()}%)"
                    canvas.drawText(categoryText, 70f, yPosition, normalPaint)
                    yPosition += 25f
                }
            } else {
                canvas.drawText(
                    if (language == "vi") "Không có dữ liệu" else "No data",
                    70f,
                    yPosition,
                    normalPaint
                )
                yPosition += 25f
            }
            
            // Footer
            yPosition = 800f
            canvas.drawLine(50f, yPosition, 545f, yPosition, linePaint)
            yPosition += 20f
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val footerText = if (language == "vi") {
                "Xuất báo cáo: ${dateFormat.format(Date())}"
            } else {
                "Exported: ${dateFormat.format(Date())}"
            }
            canvas.drawText(footerText, 50f, yPosition, normalPaint)
            
            pdfDocument.finishPage(page)
            
            // Save file
            val fileName = "BaoCao_${month}_${year}_${System.currentTimeMillis()}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            
            // Show success message
            val message = if (language == "vi") {
                "Đã lưu PDF: ${file.absolutePath}"
            } else {
                "PDF saved: ${file.absolutePath}"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                if (language == "vi") "Lỗi khi tạo PDF: ${e.message}" else "Error creating PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}
