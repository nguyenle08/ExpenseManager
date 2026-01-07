package com.example.expensemanager.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
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
    ): File? {
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
                "Đã lưu PDF: ${file.name}. Nhấn để mở."
            } else {
                "PDF saved: ${file.name}. Tap to open."
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                if (language == "vi") "Lỗi khi tạo PDF: ${e.message}" else "Error creating PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            null
        }
    }
    
    /**
     * Open PDF file with external viewer
     */
    fun openPdfFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            
            // Check if there's an app that can handle PDF
            if (intent.resolveActivity(context.packageManager) != null) {
                val chooser = Intent.createChooser(intent, "Mở PDF bằng")
                context.startActivity(chooser)
            } else {
                // No PDF viewer found, offer to download one
                showNoPdfViewerDialog(context, file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Lỗi khi mở file PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Show dialog when no PDF viewer is installed
     */
    private fun showNoPdfViewerDialog(context: Context, file: File) {
        // Show detailed message with file location
        val message = """
            Không tìm thấy ứng dụng xem PDF!
            
            File đã được lưu tại:
            ${file.absolutePath}
            
            Vui lòng cài đặt ứng dụng xem PDF như:
            - Google PDF Viewer
            - Adobe Acrobat Reader
            
            Sau đó vào: Files → Download → ${file.name}
        """.trimIndent()
        
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        
        // Try to open Play Store to download PDF viewer
        try {
            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("market://search?q=pdf viewer&c=apps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (playStoreIntent.resolveActivity(context.packageManager) != null) {
                // Wait a bit before opening Play Store so user can read the message
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        context.startActivity(playStoreIntent)
                    } catch (e: Exception) {
                        // If Play Store fails, try web browser
                        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("https://play.google.com/store/search?q=pdf viewer&c=apps")
                        }
                        context.startActivity(browserIntent)
                    }
                }, 3000) // 3 seconds delay
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Open Downloads folder to view saved PDFs
     */
    fun openDownloadsFolder(context: Context) {
        try {
            // Try to open Downloads folder with file manager
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val uri = android.net.Uri.parse(downloadsDir.absolutePath)
                setDataAndType(uri, "resource/folder")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback: Try to open with common file managers
                tryOpenWithFileManager(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If all else fails, show the file path
            tryOpenWithFileManager(context)
        }
    }
    
    /**
     * Try to open Downloads folder with common file managers
     */
    private fun tryOpenWithFileManager(context: Context) {
        try {
            // Try Files by Google or other file managers
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "resource/folder"
                data = android.net.Uri.parse("content://com.android.externalstorage.documents/document/primary:Download")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Mở thư mục Downloads"))
            } else {
                // Last resort: show message with path
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                Toast.makeText(
                    context, 
                    "Vui lòng mở ứng dụng Files → Download\nĐường dẫn: ${downloadsDir.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            Toast.makeText(
                context,
                "Vui lòng mở ứng dụng Files → Download\nĐường dẫn: ${downloadsDir.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
