package com.zmanim.lockscreen.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
 * Ultra-transparent glass overlay. It draws only the card itself and never paints,
 * dims or blurs the area outside the card, so the selected wallpaper remains intact.
 */
object GlassOverlay {
    private val BRONZE = Color.parseColor("#C69A5A")
    private val BRONZE_LIGHT = Color.parseColor("#E8C98A")
    private val IVORY = Color.parseColor("#F4E7CF")
    private val INK = Color.parseColor("#2A1A0D")
    private val RED = Color.parseColor("#B43B34")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class Cell(val label: String, val value: Date?)

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cardW = w * .76f
        val cardH = h * .48f
        val top = h * .38f
        val card = RectF((w - cardW) / 2f, top, (w + cardW) / 2f, top + cardH)

        drawGlass(canvas, card)
        drawClock(canvas, card)
        drawHeader(canvas, card, day, locationName)
        drawGrid(canvas, card, day)
        drawFooter(canvas, card, day)
    }

    private fun drawGlass(canvas: Canvas, card: RectF) {
        val r = card.width() * .045f

        // Almost-clear glass: only a very faint warm tint inside the card.
        canvas.drawRoundRect(card, r, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(26, 132, 91, 49)
        })

        // Thin highlight gives a glass edge without darkening the wallpaper.
        val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = card.width() * .006f
            color = Color.argb(165, 231, 199, 137)
        }
        canvas.drawRoundRect(card, r, r, edge)

        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = card.width() * .0018f
            color = Color.argb(95, 255, 244, 219)
        }
        val inset = card.width() * .014f
        canvas.drawRoundRect(
            RectF(card.left + inset, card.top + inset, card.right - inset, card.bottom - inset),
            r * .78f, r * .78f, inner
        )

        // Small antique bronze corner flourishes; no opaque metal background.
        drawCorner(canvas, card.left + inset * 1.5f, card.top + inset * 1.5f, 1f, 1f, card.width())
        drawCorner(canvas, card.right - inset * 1.5f, card.top + inset * 1.5f, -1f, 1f, card.width())
        drawCorner(canvas, card.left + inset * 1.5f, card.bottom - inset * 1.5f, 1f, -1f, card.width())
        drawCorner(canvas, card.right - inset * 1.5f, card.bottom - inset * 1.5f, -1f, -1f, card.width())
    }

    private fun drawCorner(canvas: Canvas, x: Float, y: Float, sx: Float, sy: Float, base: Float) {
        val s = base * .055f
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 198, 154, 90)
            style = Paint.Style.STROKE
            strokeWidth = base * .0027f
            strokeCap = Paint.Cap.ROUND
        }
        val path = Path().apply {
            moveTo(x, y + sy * s)
            cubicTo(x + sx * s * .15f, y + sy * s * .55f, x + sx * s * .45f, y + sy * s * .58f, x + sx * s, y)
        }
        canvas.drawPath(path, p)
    }

    private fun drawHeader(canvas: Canvas, card: RectF, day: DayZmanim, locationName: String) {
        val w = card.width()
        val h = card.height()
        val right = RectF(card.left + w * .50f, card.top + h * .055f, card.right - w * .045f, card.top + h * .39f)
        rtl(canvas, "זמני היום", RectF(right.left, right.top, right.right, right.top + right.height() * .27f), w * .061f, true)
        rtl(canvas, locationName, RectF(right.left, right.top + right.height() * .28f, right.right, right.top + right.height() * .49f), w * .038f, false)
        rtl(canvas, day.hebrewDate, RectF(right.left, right.top + right.height() * .54f, right.right, right.top + right.height() * .74f), w * .034f, true)
        val p = textPaint(w * .026f, IVORY, false).apply { textAlign = Paint.Align.CENTER }
        p.setShadowLayer(w * .002f, 0f, w * .002f, Color.argb(95, 0, 0, 0))
        canvas.drawText(day.gregorianDate, right.centerX(), right.top + right.height() * .91f, p)
    }

    private fun drawGrid(canvas: Canvas, card: RectF, day: DayZmanim) {
        val w = card.width()
        val h = card.height()
        val grid = RectF(card.left + w * .05f, card.top + h * .41f, card.right - w * .05f, card.top + h * .735f)
        val radius = w * .028f

        // No dark fill: only translucent separators over the wallpaper.
        canvas.drawRoundRect(grid, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * .0022f
            color = Color.argb(125, 205, 166, 103)
        })

        val cw = grid.width() / 4f
        val ch = grid.height() / 2f
        val sep = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(85, 210, 175, 115)
            strokeWidth = w * .0011f
        }
        for (c in 1 until 4) canvas.drawLine(grid.left + cw * c, grid.top, grid.left + cw * c, grid.bottom, sep)
        canvas.drawLine(grid.left, grid.top + ch, grid.right, grid.top + ch, sep)

        val cells = listOf(
            Cell("סוף זמן ק״ש", day.sofZmanShmaGra), Cell("זריחה", day.netzHachama),
            Cell("תפילה", day.sofZmanTefila), Cell("עלות השחר", day.alosHashachar),
            Cell("צאת הכוכבים", day.tzais), Cell("שקיעה", day.shkia),
            Cell("מנחה קטנה", day.minchaKetana), Cell("מנחה גדולה", day.minchaGedola)
        )

        cells.forEachIndexed { i, cell ->
            val col = i % 4
            val row = i / 4
            val box = RectF(grid.left + col * cw, grid.top + row * ch, grid.left + (col + 1) * cw, grid.top + (row + 1) * ch)
            rtl(canvas, cell.label, RectF(box.left + 3f, box.top + ch * .06f, box.right - 3f, box.top + ch * .48f), cw * .155f, true)
            val p = textPaint(cw * .205f, BRONZE_LIGHT, true).apply {
                textAlign = Paint.Align.CENTER
                setShadowLayer(cw * .010f, 0f, cw * .008f, Color.argb(90, 0, 0, 0))
            }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - ch * .12f, p)
        }
    }

    private fun drawFooter(canvas: Canvas, card: RectF, day: DayZmanim) {
        val w = card.width()
        val h = card.height()
        val top = card.top + h * .755f
        val bottom = card.bottom - h * .055f
        val mid = top + (bottom - top) * .52f

        canvas.drawLine(card.left + w * .12f, mid, card.right - w * .07f, mid, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(75, 210, 175, 115)
            strokeWidth = w * .001f
        })

        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        rtl(canvas, parsha, RectF(card.left + w * .12f, top, card.right - w * .07f, mid), w * .033f, false)

        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val bottomText = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            day.roshChodeshInDays == 0 && !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            isFriday -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
            else -> null
        }
        if (!bottomText.isNullOrBlank()) {
            rtl(canvas, bottomText, RectF(card.left + w * .12f, mid, card.right - w * .07f, bottom), w * .030f, false)
        }
    }

    private fun drawClock(canvas: Canvas, card: RectF) {
        val w = card.width()
        val h = card.height()
        val cx = card.left + w * .27f
        val cy = card.top + h * .22f
        val r = w * .18f

        canvas.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(34, 255, 248, 232)
        })
        canvas.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .055f
            color = Color.argb(175, 198, 154, 90)
        })

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(180, 244, 220, 176); strokeCap = Paint.Cap.ROUND }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val out = r * .82f
            val inn = if (i % 5 == 0) r * .68f else r * .75f
            tick.strokeWidth = if (i % 5 == 0) r * .018f else r * .007f
            canvas.drawLine(cx + inn * cos(a).toFloat(), cy + inn * sin(a).toFloat(), cx + out * cos(a).toFloat(), cy + out * sin(a).toFloat(), tick)
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .43f, hour * 30f, r * .052f, IVORY)
        hand(canvas, cx, cy, r * .63f, min * 6f, r * .034f, IVORY)
        hand(canvas, cx, cy, r * .69f, sec * 6f, r * .012f, RED)
        canvas.drawCircle(cx, cy, r * .042f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BRONZE_LIGHT })
        canvas.drawCircle(cx, cy, r * .018f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, len: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90).toDouble())
        canvas.drawLine(cx, cy, cx + len * cos(a).toFloat(), cy + len * sin(a).toFloat(), Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        })
    }

    private fun rtl(canvas: Canvas, text: String, box: RectF, size: Float, bold: Boolean) {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            color = BRONZE_LIGHT
            typeface = if (bold) SERIF_BOLD else SERIF
            setShadowLayer(size * .045f, 0f, size * .025f, Color.argb(95, 30, 16, 7))
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, p, box.width().toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setMaxLines(2)
            .build()
        canvas.save()
        canvas.translate(box.left, box.top + (box.height() - layout.height) / 2f)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) SERIF_BOLD else SERIF
    }
}
