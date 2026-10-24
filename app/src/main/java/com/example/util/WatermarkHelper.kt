package com.example.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility helper to render an inspection watermark on photos.
 * Stamping features (Permanen & Tidak dapat diedit manual):
 * - Pojok kanan atas (Top-Right corner)
 * - No Mobil yang di-input
 * - Nama Auditor
 * - Waktu inspeksi (Timestamp)
 * - High-contrast pill badge with vibrant border for 100% visibility on any background
 */
object WatermarkHelper {

    fun drawVehicleWatermark(
        canvas: Canvas,
        width: Float,
        height: Float,
        vehicleNumber: String,
        auditorName: String? = null,
        timestampStr: String? = null
    ) {
        if (vehicleNumber.isBlank()) return

        val minDim = minOf(width, height)
        // Scaled typography relative to canvas dimensions
        val titleFontSize = (minDim * 0.038f).coerceIn(16f, 48f)
        val auditorFontSize = (titleFontSize * 0.85f).coerceIn(14f, 40f)
        val subFontSize = (titleFontSize * 0.70f).coerceIn(12f, 34f)

        val titleText = "NO. MOBIL: ${vehicleNumber.trim().uppercase()}"
        val auditorText = if (!auditorName.isNullOrBlank()) "AUDITOR: ${auditorName.trim().uppercase()}" else null
        val dateText = timestampStr ?: SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val subtitleText = "$dateText • PERMANEN"

        val titlePaint = Paint().apply {
            color = Color.parseColor("#FACC15") // Bright Yellow for clear visibility
            textSize = titleFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val auditorPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF") // Crisp White
            textSize = auditorFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.parseColor("#94A3B8") // Slate-400
            textSize = subFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val titleBounds = Rect()
        titlePaint.getTextBounds(titleText, 0, titleText.length, titleBounds)

        val auditorBounds = Rect()
        if (auditorText != null) {
            auditorPaint.getTextBounds(auditorText, 0, auditorText.length, auditorBounds)
        }

        val subBounds = Rect()
        subPaint.getTextBounds(subtitleText, 0, subtitleText.length, subBounds)

        val maxTextWidth = maxOf(
            titleBounds.width(),
            if (auditorText != null) auditorBounds.width() else 0,
            subBounds.width()
        ).toFloat()

        val lineSpacing = titleFontSize * 0.35f
        val totalTextHeight = titleBounds.height() +
                (if (auditorText != null) auditorBounds.height() + lineSpacing else 0f) +
                subBounds.height() + lineSpacing

        val padX = titleFontSize * 0.6f
        val padY = titleFontSize * 0.5f

        val badgeWidth = maxTextWidth + (padX * 2)
        val badgeHeight = totalTextHeight + (padY * 2)

        // Position: Top-Right corner (Pojok Kanan Atas) with responsive margin
        val marginX = (minDim * 0.035f).coerceIn(12f, 36f)
        val marginY = (minDim * 0.035f).coerceIn(12f, 36f)

        val right = width - marginX
        val left = right - badgeWidth
        val top = marginY
        val bottom = top + badgeHeight

        val badgeRect = RectF(left, top, right, bottom)
        val cornerRadius = (titleFontSize * 0.35f).coerceIn(6f, 20f)

        // Solid dark pill background: 90% opacity
        val bgPaint = Paint().apply {
            color = Color.argb(230, 15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Pill border: vibrant Cyan
        val borderPaint = Paint().apply {
            color = Color.argb(220, 56, 189, 248)
            style = Paint.Style.STROKE
            strokeWidth = (minDim * 0.0035f).coerceIn(1.5f, 4.5f)
            isAntiAlias = true
        }

        canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, borderPaint)

        // Text positions
        val textX = left + padX
        var currentY = top + padY + titleBounds.height()
        canvas.drawText(titleText, textX, currentY, titlePaint)

        if (auditorText != null) {
            currentY += lineSpacing + auditorBounds.height()
            canvas.drawText(auditorText, textX, currentY, auditorPaint)
        }

        currentY += lineSpacing + subBounds.height()
        canvas.drawText(subtitleText, textX, currentY, subPaint)
    }
}
