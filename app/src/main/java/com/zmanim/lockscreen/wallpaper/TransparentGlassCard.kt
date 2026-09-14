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

/** Bronze vintage card matched to the approved visual reference. */
object TransparentGlassCard {
    private val BG = Color.parseColor("#3A2A1D")
    private val BG_LIGHT = Color.parseColor("#5A402A")
    private val GOLD = Color.parseColor("#D6AD67")
    private val GOLD_SOFT = Color.parseColor("#B98C4F")
    private val IVORY = Color.parseColor("#F6E7C9")
    private val IVORY_SOFT = Color.parseColor("#E2CDA8")
    private val GRID = Color.parseColor("#8D6D46")
    private val RED = Color.parseColor("#B94E3F")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class ZCell(val label: String, val value: Date?)

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val card = RectF(w * .105f, h * .34f, w * .895f, h * .755f)
        val radius = w * .045f

        drawCard(canvas, card, radius, w)
        drawCorners(canvas, card, w)

        val topAreaBottom = card.top + card.height() * .38f
        val leftArea = RectF(card.left + w * .035f, card.top + h * .024f, card.centerX() - w * .015f, topAreaBottom)
        val rightArea = RectF(card.centerX() + w * .015f, card.top + h * .022f, card.right - w * .03f, topAreaBottom)

        drawClock(canvas, leftArea.centerX(), leftArea.centerY() + h * .008f, leftArea.width() * .34f)
        drawHeader(canvas, rightArea, day, locationName, w)

        val gridTop = topAreaBottom + h * .004f
        val gridBottom = card.top + card.height() * .79f
        val grid = RectF(card.left + w * .02f, gridTop, card.right - w * .02f, gridBottom)
        drawZmanimGrid(canvas, grid, day)

        val bottom = RectF(card.left + w * .025f, gridBottom + h * .008f, card.right - w * .025f, card.bottom - h * .016f)
        drawBottom(canvas, bottom, day)
    }

    private fun drawCard(canvas: Canvas, card: RectF, radius: Float, w: Float) {
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 0, 0, 0)
            setShadowLayer(w * .025f, 0f, w * .012f, Color.argb(135, 0, 0, 0))
        }
        canvas.drawRoundRect(RectF(card.left + 3f, card.top + 6f, card.right + 3f, card.bottom + 6f), radius, radius, shadow)
        shadow.clearShadowLayer()

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(235, 58, 42, 29) }
        canvas.drawRoundRect(card, radius, radius, fill)

        val innerWash = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(42, 201, 157, 92) }
        canvas.drawRoundRect(RectF(card.left + 6f, card.top + 6f, card.right - 6f, card.bottom - 6f), radius * .88f, radius * .88f, innerWash)

        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD
            style = Paint.Style.STROKE
            strokeWidth = w * .0042f
        }
        canvas.drawRoundRect(card, radius, radius, outer)

        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 170, 122, 65)
            style = Paint.Style.STROKE
            strokeWidth = w * .0015f
        }
        canvas.drawRoundRect(RectF(card.left + w * .012f, card.top + w * .012f, card.right - w * .012f, card.bottom - w * .012f), radius * .75f, radius * .75f, inner)
    }

    private fun drawCorners(canvas: Canvas, card: RectF, w: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD_SOFT
            style = Paint.Style.STROKE
            strokeWidth = w * .003f
            strokeCap = Paint.Cap.ROUND
        }
        fun corner(cx: Float, cy: Float, sx: Float, sy: Float) {
            val s = w * .045f
            val path = Path().apply {
                moveTo(cx, cy + sy * s)
                cubicTo(cx + sx * s * .15f, cy + sy * s * .65f, cx + sx * s * .4f, cy + sy * s * .62f, cx + sx * s * .55f, cy + sy * s * .38f)
                cubicTo(cx + sx * s * .72f, cy + sy * s * .12f, cx + sx * s * .7f, cy, cx + sx * s, cy)
            }
            canvas.drawPath(path, p)
            canvas.drawCircle(cx + sx * s * .34f, cy + sy * s * .38f, w * .0045f, p)
        }
        corner(card.left + w * .018f, card.top + w * .018f, 1f, 1f)
        corner(card.right - w * .018f, card.top + w * .018f, -1f, 1f)
        corner(card.left + w * .018f, card.bottom - w * .018f, 1f, -1f)
        corner(card.right - w * .018f, card.bottom - w * .018f, -1f, -1f)
    }

    private fun drawHeader(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String, w: Float) {
        drawRtl(canvas, "זמני היום", RectF(rect.left, rect.top, rect.right, rect.top + rect.height() * .28f), w * .050f, IVORY, true)
        drawRtl(canvas, locationName, RectF(rect.left, rect.top + rect.height() * .25f, rect.right, rect.top + rect.height() * .43f), w * .028f, GOLD, false)
        drawRtl(canvas, day.hebrewDate, RectF(rect.left, rect.top + rect.height() * .50f, rect.right, rect.top + rect.height() * .70f), w * .031f, IVORY_SOFT, true)
        val civil = textPaint(w * .024f, IVORY_SOFT, false).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(day.gregorianDate, rect.centerX(), rect.top + rect.height() * .84f, civil)
    }

    private fun drawZmanimGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val cells = entries(day)
        val cols = 4
        val rows = 2
        val cw = rect.width() / cols
        val ch = rect.height() / rows

        val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(55, 25, 18, 12) }
        canvas.drawRoundRect(rect, rect.width() * .025f, rect.width() * .025f, panel)

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(155, 154, 116, 72)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawRoundRect(rect, rect.width() * .025f, rect.width() * .025f, stroke)

        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 126, 94, 58); strokeWidth = 1.1f }
        for (c in 1 until cols) {
            val x = rect.left + cw * c
            canvas.drawLine(x, rect.top, x, rect.bottom, line)
        }
        canvas.drawLine(rect.left, rect.top + ch, rect.right, rect.top + ch, line)

        cells.forEachIndexed { i, cell ->
            val col = i % cols
            val row = i / cols
            val box = RectF(rect.left + col * cw, rect.top + row * ch, rect.left + (col + 1) * cw, rect.top + (row + 1) * ch)
            drawRtl(canvas, cell.label, RectF(box.left + 3f, box.top + box.height() * .06f, box.right - 3f, box.top + box.height() * .52f), cw * .135f, IVORY_SOFT, true)
            val tp = textPaint(cw * .20f, GOLD, true).apply { textAlign = Paint.Align.CENTER }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - box.height() * .12f, tp)
        }
    }

    private fun drawBottom(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(130, 135, 100, 61); strokeWidth = 1f }
        val rowH = rect.height() / 2f
        canvas.drawLine(rect.left + rect.width() * .08f, rect.top + rowH, rect.right - rect.width() * .08f, rect.top + rowH, line)

        val bookX = rect.left + rect.width() * .12f
        val candleX = rect.left + rect.width() * .12f
        val icon = textPaint(rect.width() * .06f, GOLD, true).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText("▣", bookX, rect.top + rowH * .62f, icon)
        canvas.drawText("♨", candleX, rect.top + rowH + rowH * .64f, icon)

        val parsha = day.parshaLabel?.takeIf { it.isNotBlank() }?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        drawRtl(canvas, parsha, RectF(rect.left + rect.width() * .2f, rect.top, rect.right, rect.top + rowH), rect.width() * .045f, IVORY, true)

        val hilula = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            else -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
        }
        drawRtl(canvas, hilula, RectF(rect.left + rect.width() * .2f, rect.top + rowH, rect.right, rect.bottom), rect.width() * .041f, IVORY_SOFT, true)
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
        val bezel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B58A52") }
        canvas.drawCircle(cx, cy, r * 1.06f, bezel)
        val rim = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5B422D") }
        canvas.drawCircle(cx, cy, r, rim)
        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#C9A773") }
        canvas.drawCircle(cx, cy, r * .90f, face)
        val faceWash = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(35, 60, 37, 22) }
        canvas.drawCircle(cx, cy, r * .90f, faceWash)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4C3423"); strokeWidth = r * .015f }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val o = r * .82f
            val inn = if (i % 5 == 0) r * .72f else r * .77f
            canvas.drawLine(cx + inn * cos(a).toFloat(), cy + inn * sin(a).toFloat(), cx + o * cos(a).toFloat(), cy + o * sin(a).toFloat(), tick)
        }

        val num = textPaint(r * .22f, Color.parseColor("#3A271B"), false).apply { textAlign = Paint.Align.CENTER }
        for (n in listOf(12, 3, 6, 9)) {
            val a = Math.toRadians((n * 30 - 90).toDouble())
            canvas.drawText(n.toString(), cx + r * .61f * cos(a).toFloat(), cy + r * .61f * sin(a).toFloat() + num.textSize * .33f, num)
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .48f, hour * 30f, r * .065f, Color.parseColor("#241A14"))
        hand(canvas, cx, cy, r * .68f, min * 6f, r * .045f, Color.parseColor("#241A14"))
        hand(canvas, cx, cy, r * .77f, sec * 6f, r * .014f, RED)
        canvas.drawCircle(cx, cy, r * .055f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD })
        canvas.drawCircle(cx, cy, r * .022f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2B1F16") })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, length: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90f).toDouble())
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(cx, cy, cx + length * cos(a).toFloat(), cy + length * sin(a).toFloat(), p)
    }

    private fun drawRtl(canvas: Canvas, text: String, box: RectF, size: Float, color: Int, bold: Boolean) {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) SERIF_BOLD else SERIF
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, p, box.width().toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setMaxLines(2)
            .setLineSpacing(0f, .95f)
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
