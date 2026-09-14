package com.zmanim.lockscreen.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin

/**
 * Clean vintage-glass zmanim card.
 * Only the card area is translucent; the wallpaper itself is never darkened or blurred.
 */
object TransparentGlassCard {
    private val IVORY = Color.parseColor("#FFF8EA")
    private val IVORY_SOFT = Color.parseColor("#F1E4CF")
    private val GOLD = Color.parseColor("#D5B06A")
    private val RED = Color.parseColor("#A95A50")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class ZCell(val label: String, val value: Date?)

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()

        // Smaller, lighter card so the wallpaper remains dominant.
        val card = RectF(w * 0.075f, h * 0.355f, w * 0.925f, h * 0.715f)
        val radius = w * 0.045f

        drawGlass(canvas, card, radius, w)
        drawHeader(canvas, card, w, h, day, locationName)

        val clockCx = card.left + card.width() * 0.22f
        val clockCy = card.top + card.height() * 0.43f
        val clockR = card.width() * 0.125f
        drawClock(canvas, clockCx, clockCy, clockR)

        // No "הזמן הקרוב" box. Use the free space for a clean decorative date area.
        val dateBox = RectF(
            card.left + card.width() * 0.42f,
            card.top + card.height() * 0.25f,
            card.right - w * 0.035f,
            card.top + card.height() * 0.50f
        )
        drawRtl(canvas, day.hebrewDate, RectF(dateBox.left, dateBox.top, dateBox.right, dateBox.top + dateBox.height() * .45f), w * .031f, IVORY, true)
        val civil = textPaint(w * .022f, IVORY_SOFT, false).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(day.gregorianDate, dateBox.centerX(), dateBox.top + dateBox.height() * .72f, civil)

        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 221, 191, 135)
            strokeWidth = 1.2f
        }
        val divY = card.top + card.height() * 0.56f
        canvas.drawLine(card.left + w * .04f, divY, card.right - w * .04f, divY, divider)

        // Large 4x2 grid with readable labels and times.
        val grid = RectF(
            card.left + w * .025f,
            divY + h * .006f,
            card.right - w * .025f,
            card.bottom - h * .044f
        )
        drawZmanimGrid(canvas, grid, day)

        val specialTop = card.bottom - h * .038f
        canvas.drawLine(card.left + w * .04f, specialTop, card.right - w * .04f, specialTop, divider)
        drawSpecialRow(
            canvas,
            RectF(card.left + w * .04f, specialTop + h * .004f, card.right - w * .04f, card.bottom - h * .005f),
            day
        )
    }

    private fun drawGlass(canvas: Canvas, card: RectF, radius: Float, w: Float) {
        // Very light fill only inside the card. No heavy shadow, no full-screen effect.
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(34, 34, 30, 26)
        }
        canvas.drawRoundRect(card, radius, radius, fill)

        val softInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(18, 255, 248, 233)
        }
        canvas.drawRoundRect(
            RectF(card.left + 4f, card.top + 4f, card.right - 4f, card.bottom - 4f),
            radius * .9f,
            radius * .9f,
            softInner
        )

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 213, 176, 106)
            style = Paint.Style.STROKE
            strokeWidth = w * .0018f
        }
        canvas.drawRoundRect(card, radius, radius, border)
    }

    private fun drawHeader(canvas: Canvas, card: RectF, w: Float, h: Float, day: DayZmanim, locationName: String) {
        val title = textPaint(w * .052f, IVORY, true).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText("זמני היום", card.centerX(), card.top + h * .035f, title)

        val ornament = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 213, 176, 106)
            strokeWidth = 1.4f
        }
        val oy = card.top + h * .043f
        val gap = w * .11f
        canvas.drawLine(card.centerX() - w * .25f, oy, card.centerX() - gap, oy, ornament)
        canvas.drawLine(card.centerX() + gap, oy, card.centerX() + w * .25f, oy, ornament)

        drawRtl(
            canvas,
            locationName,
            RectF(card.left + w * .12f, card.top + h * .048f, card.right - w * .12f, card.top + h * .075f),
            w * .027f,
            IVORY_SOFT,
            false
        )
    }

    private fun drawZmanimGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val cells = entries(day)
        val cols = 4
        val rows = 2
        val cellW = rect.width() / cols
        val cellH = rect.height() / rows

        val gridLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(42, 235, 220, 195)
            strokeWidth = 1f
        }
        for (c in 1 until cols) {
            val x = rect.left + cellW * c
            canvas.drawLine(x, rect.top + 2f, x, rect.bottom - 2f, gridLine)
        }
        canvas.drawLine(rect.left + 2f, rect.top + cellH, rect.right - 2f, rect.top + cellH, gridLine)

        cells.forEachIndexed { index, cell ->
            val col = index % cols
            val row = index / cols
            val box = RectF(
                rect.left + col * cellW,
                rect.top + row * cellH,
                rect.left + (col + 1) * cellW,
                rect.top + (row + 1) * cellH
            )

            // One compact RTL-safe label, then a clearly separated time.
            drawRtl(
                canvas,
                cell.label,
                RectF(box.left + 6f, box.top + box.height() * .10f, box.right - 6f, box.top + box.height() * .48f),
                cellW * .115f,
                IVORY_SOFT,
                false
            )

            val time = textPaint(cellW * .155f, IVORY, true).apply { textAlign = Paint.Align.CENTER }
            canvas.drawText(
                ZmanimProvider.formatTime(cell.value),
                box.centerX(),
                box.bottom - box.height() * .14f,
                time
            )
        }
    }

    private fun drawSpecialRow(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val left = RectF(rect.left, rect.top, rect.centerX() - 4f, rect.bottom)
        val right = RectF(rect.centerX() + 4f, rect.top, rect.right, rect.bottom)

        drawRtl(
            canvas,
            "כניסת שבת  ${ZmanimProvider.formatTime(day.candleLighting)}",
            left,
            rect.width() * .032f,
            IVORY,
            true
        )

        val special = when {
            !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            !day.parshaLabel.isNullOrBlank() -> "פרשת ${day.parshaLabel}"
            else -> "שבת שלום"
        }
        drawRtl(canvas, special ?: "", right, rect.width() * .032f, GOLD, true)
    }

    private fun entries(day: DayZmanim): List<ZCell> = listOf(
        ZCell("עלות השחר", day.alosHashachar),
        ZCell("הנץ החמה", day.netzHachama),
        ZCell("סוף זמן ק״ש", day.sofZmanShmaGra),
        ZCell("חצות היום", day.chatzos),
        ZCell("מנחה גדולה", day.minchaGedola),
        ZCell("פלג המנחה", day.plagHamincha),
        ZCell("שקיעה", day.shkia),
        ZCell("צאת הכוכבים", day.tzais)
    )

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(20, 255, 249, 237) }
        canvas.drawCircle(cx, cy, r, face)

        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 213, 176, 106)
            style = Paint.Style.STROKE
            strokeWidth = r * .028f
        }
        canvas.drawCircle(cx, cy, r, outer)

        val ticks = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(145, 255, 244, 218)
            strokeWidth = r * .01f
        }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val outerR = r * .82f
            val innerR = if (i % 5 == 0) r * .72f else r * .77f
            canvas.drawLine(
                cx + innerR * cos(a).toFloat(),
                cy + innerR * sin(a).toFloat(),
                cx + outerR * cos(a).toFloat(),
                cy + outerR * sin(a).toFloat(),
                ticks
            )
        }

        val number = textPaint(r * .20f, IVORY, false).apply { textAlign = Paint.Align.CENTER }
        for (n in 1..12) {
            val a = Math.toRadians((n * 30 - 90).toDouble())
            canvas.drawText(
                n.toString(),
                cx + r * .60f * cos(a).toFloat(),
                cy + r * .60f * sin(a).toFloat() + number.textSize * .33f,
                number
            )
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .47f, hour * 30f, r * .055f, IVORY)
        hand(canvas, cx, cy, r * .67f, min * 6f, r * .035f, IVORY_SOFT)
        hand(canvas, cx, cy, r * .76f, sec * 6f, r * .012f, RED)
        canvas.drawCircle(cx, cy, r * .045f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, length: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90f).toDouble())
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(
            cx,
            cy,
            cx + length * cos(a).toFloat(),
            cy + length * sin(a).toFloat(),
            p
        )
    }

    private fun drawRtl(
        canvas: Canvas,
        text: String,
        box: RectF,
        size: Float,
        color: Int,
        bold: Boolean
    ) {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) SERIF_BOLD else SERIF
        }
        val width = box.width().toInt().coerceAtLeast(1)
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, p, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setMaxLines(2)
            .setLineSpacing(0f, 0.92f)
            .build()

        canvas.save()
        val y = box.top + (box.height() - layout.height) / 2f
        canvas.translate(box.left, y)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) SERIF_BOLD else SERIF
    }
}
