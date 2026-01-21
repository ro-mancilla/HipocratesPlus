package com.example.hipocrates.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.hipocrates.model.Appointment
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 50f

    fun generateAppointmentPdf(context: Context, appointment: Appointment, userName: String): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var yPosition = MARGIN

            // Title
            val titlePaint = Paint().apply {
                color = Color.rgb(67, 164, 67)
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Hipócrates+", MARGIN, yPosition, titlePaint)
            yPosition += 40f

            // Document title
            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Cita médica", MARGIN, yPosition, headerPaint)
            yPosition += 20f

            // Divider line
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 2f
            }
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, linePaint)
            yPosition += 30f

            // Regular text paint
            val labelPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val valuePaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
            }

            // Patient info
            canvas.drawText("Paciente:", MARGIN, yPosition, labelPaint)
            yPosition += 20f
            canvas.drawText(userName, MARGIN + 20f, yPosition, valuePaint)
            yPosition += 35f

            // Status
            canvas.drawText("Estado:", MARGIN, yPosition, labelPaint)
            yPosition += 20f
            canvas.drawText(appointment.estado.displayName, MARGIN + 20f, yPosition, valuePaint)
            yPosition += 35f

            // Specialty
            canvas.drawText("Especialidad:", MARGIN, yPosition, labelPaint)
            yPosition += 20f
            canvas.drawText(appointment.especialidad.displayName, MARGIN + 20f, yPosition, valuePaint)
            yPosition += 35f

            // Doctor
            canvas.drawText("Médico:", MARGIN, yPosition, labelPaint)
            yPosition += 20f
            canvas.drawText(appointment.doctorNombre, MARGIN + 20f, yPosition, valuePaint)
            yPosition += 35f

            // Date
            canvas.drawText("Fecha:", MARGIN, yPosition, labelPaint)
            yPosition += 20f
            val formattedDate = try {
                LocalDate.parse(appointment.fecha)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (e: Exception) {
                appointment.fecha
            }
            canvas.drawText(formattedDate, MARGIN + 20f, yPosition, valuePaint)
            yPosition += 35f

            // Time
            canvas.drawText("Hora:", MARGIN, yPosition, labelPaint)
            yPosition += 20f
            canvas.drawText(appointment.hora, MARGIN + 20f, yPosition, valuePaint)
            yPosition += 35f

            // Reason
            canvas.drawText("Motivo de consulta:", MARGIN, yPosition, labelPaint)
            yPosition += 20f
            val motivoLines = splitTextToFit(appointment.motivo, valuePaint, PAGE_WIDTH - MARGIN * 2 - 20f)
            for (line in motivoLines) {
                canvas.drawText(line, MARGIN + 20f, yPosition, valuePaint)
                yPosition += 20f
            }
            yPosition += 15f

            // Notes
            if (appointment.notas.isNotBlank()) {
                canvas.drawText("Notas adicionales:", MARGIN, yPosition, labelPaint)
                yPosition += 20f
                val notasLines = splitTextToFit(appointment.notas, valuePaint, PAGE_WIDTH - MARGIN * 2 - 20f)
                for (line in notasLines) {
                    canvas.drawText(line, MARGIN + 20f, yPosition, valuePaint)
                    yPosition += 20f
                }
                yPosition += 15f
            }

            // Footer
            yPosition = PAGE_HEIGHT - MARGIN - 20f
            canvas.drawLine(MARGIN, yPosition - 10f, PAGE_WIDTH - MARGIN, yPosition - 10f, linePaint)
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
            }
            val footerText = "PDF generado el ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
            canvas.drawText(footerText, MARGIN, yPosition, footerPaint)

            pdfDocument.finishPage(page)

            val fileName = "H+_${appointment.especialidad.displayName.replace(" ", "")}_${formattedDate.replace("/", "")}.pdf"
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()
            return file

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun splitTextToFit(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)

            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }
}
