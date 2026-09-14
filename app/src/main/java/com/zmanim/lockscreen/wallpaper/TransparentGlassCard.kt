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

/** Antique bronze transparent zmanim card based on the approved reference. */
object TransparentGlassCard {
    private val GOLD = Color.parseColor("#D5AE68")
    private val GOLD_DARK = Color.parseColor("#9A7140")
    private val IVORY = Color.parseColor("#F4E3C1")
    private val IVORY_SOFT = Color.parseColor("#D9C19A")
    private val BRONZE = Color.parseColor("#4A3322")
    private val RED = Color.parseColor("#B74D40")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class ZCell(val label: String, val value: Date?)

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val card = RectF(w * .095f, h * .335f, w * .905f, h * .755f)
        val radius = w * .040f

        drawCard(canvas, card, radius, w, h)
        drawCornerOrnaments(canvas, card, w)

        val topBottom = card.top + card.height() * .38f
        val clockRect = RectF(card.left + w * .035f, card.top + h * .026f, card.centerX() - w * .010f, topBottom)
        val textRect = RectF(card.centerX() + w * .008f, card.top + h * .020f, card.right - w * .026f, topBottom)

        drawClock(canvas, clockRect.centerX(), clockRect.centerY(), clockRect.width() * .37f)
        drawHeader(canvas, textRect, day, locationName, w)

        val gridTop = topBottom + h * .006f
        val gridBottom = card.top + card.height() * .79f
        val gridRect = RectF(card.left + w * .020f, gridTop, card.right - w * .020f, gridBottom)
        drawZmanimGrid(canvas, gridRect, day)

        val bottomRect = RectF(card.left + w * .027f, gridBottom + h * .004f, card.right - w * .027f, card.bottom - h * .014f)
        drawBottom(canvas, bottomRect, day)
    }

    private fun drawCard(canvas: Canvas, card: RectF, radius: Float, w: Float, h: Float) {
        // Light shadow only around the card, never over the wallpaper.
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 0, 0, 0)
            setShadowLayer(w * .020f, 0f, h * .004f, Color.argb(80, 0, 0, 0))
        }
        canvas.drawRoundRect(RectF(card.left + 2f, card.top + 3f, card.right + 2f, card.bottom + 3f), radius, radius, shadow)
        shadow.clearShadowLayer()

        // Translucent bronze so the user's wallpaper remains visible through the card.
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 66, 45, 30) }
        canvas.drawRoundRect(card, radius, radius, fill)

        val warmWash = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(28, 186, 137, 74) }
        canvas.drawRoundRect(RectF(card.left + 5f, card.top + 5f, card.right - 5f, card.bottom - 5f), radius * .90f, radius * .90f, warmWash)

        // Fine antique leather/metal texture confined to the card.
        val texture = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(22, 235, 203, 154); strokeWidth = 1f }
        val step = (card.width() / 32f).coerceAtLeast(8f)
        var x = card.left + step
        var i = 0
        while (x < card.right - step) {
            val y = card.top + ((i * 29) % 101) / 101f * card.height()
            canvas.drawCircle(x, y, 0.7f + (i % 3) * .35f, texture)
            x += step
            i++
        }

        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(220, 213, 174, 104)
            style = Paint.Style.STROKE
            strokeWidth = w * .0042f
        }
        canvas.drawRoundRect(card, radius, radius, outer)

        val middle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(175, 122, 83, 45)
            style = Paint.Style.STROKE
            strokeWidth = w * .0016f
        }
        val mid = RectF(card.left + w * .009f, card.top + w * .009f, card.right - w * .009f, card.bottom - w * .009f)
        canvas.drawRoundRect(mid, radius * .80f, radius * .80f, middle)

        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(130, 230, 197, 138)
            style = Paint.Style.STROKE
            strokeWidth = w * .0011f
        }
        val inn = RectF(card.left + w * .016f, card.top + w * .016f, card.right - w * .016f, card.bottom - w * .016f)
        canvas.drawRoundRect(inn, radius * .66f, radius * .66f, inner)
    }

    private fun drawCornerOrnaments(canvas: Canvas, card: RectF, w: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(215, 213, 174, 104)
            style = Paint.Style.STROKE
            strokeWidth = w * .0025f
            strokeCap = Paint.Cap.ROUND
        }

        fun ornament(anchorX: Float, anchorY: Float, sx: Float, sy: Float) {
            val s = w * .056f
            val path = Path().apply {
                moveTo(anchorX, anchorY + sy * s)
                cubicTo(anchorX + sx * s * .03f, anchorY + sy * s * .58f, anchorX + sx * s * .28f, anchorY + sy * s * .56f, anchorX + sx * s * .43f, anchorY + sy * s * .34f)
                cubicTo(anchorX + sx * s * .58f, anchorY + sy * s * .12f, anchorX + sx * s * .56f, anchorY, anchorX + sx * s, anchorY)
            }
            canvas.drawPath(path, p)
            canvas.drawCircle(anchorX + sx * s * .27f, anchorY + sy * s * .31f, w * .007f, p)
            canvas.drawCircle(anchorX + sx * s * .50f, anchorY + sy * s * .17f, w * .0043f, p)
            val leaf = Path().apply {
                moveTo(anchorX + sx * s * .55f, anchorY + sy * s * .38f)
                cubicTo(anchorX + sx * s * .70f, anchorY + sy * s * .26f, anchorX + sx * s * .73f, anchorY + sy * s * .48f, anchorX + sx * s * .55f, anchorY + sy * s * .38f)
            }
            canvas.drawPath(leaf, p)
        }

        val off = w * .020f
        ornament(card.left + off, card.top + off, 1f, 1f)
        ornament(card.right - off, card.top + off, -1f, 1f)
        ornament(card.left + off, card.bottom - off, 1f, -1f)
        ornament(card.right - off, card.bottom - off, -1f, -1f)
    }

    private fun drawHeader(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String, w: Float) {
        drawRtl(canvas, "זמני היום", RectF(rect.left, rect.top, rect.right, rect.top + rect.height() * .28f), w * .053f, IVORY, true)
        drawRtl(canvas, locationName, RectF(rect.left, rect.top + rect.height() * .25f, rect.right, rect.top + rect.height() * .42f), w * .030f, GOLD, false)
        drawRtl(canvas, day.hebrewDate, RectF(rect.left, rect.top + rect.height() * .49f, rect.right, rect.top + rect.height() * .69f), w * .031f, IVORY_SOFT, true)
        val civil = textPaint(w * .024f, IVORY_SOFT, false).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(day.gregorianDate, rect.centerX(), rect.top + rect.height() * .84f, civil)
    }

    private fun drawZmanimGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val cells = entries(day)
        val cols = 4
        val rows = 2
        val cw = rect.width() / cols
        val ch = rect.height() / rows

        val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(30, 22, 15, 10) }
        canvas.drawRoundRect(rect, rect.width() * .026f, rect.width() * .026f, panel)

        val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(170, 177, 130, 75)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawRoundRect(rect, rect.width() * .026f, rect.width() * .026f, outline)

        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(135, 141, 103, 61); strokeWidth = 1.05f }
        for (c in 1 until cols) {
            val x = rect.left + cw * c
            canvas.drawLine(x, rect.top, x, rect.bottom, line)
        }
        canvas.drawLine(rect.left, rect.top + ch, rect.right, rect.top + ch, line)

        cells.forEachIndexed { i, cell ->
            val col = i % cols
            val row = i / cols
            val box = RectF(rect.left + col * cw, rect.top + row * ch, rect.left + (col + 1) * cw, rect.top + (row + 1) * ch)
            drawRtl(canvas, cell.label, RectF(box.left + 4f, box.top + box.height() * .07f, box.right - 4f, box.top + box.height() * .52f), cw * .133f, IVORY_SOFT, true)
            val tp = textPaint(cw * .20f, GOLD, true).apply { textAlign = Paint.Align.CENTER }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - box.height() * .12f, tp)
        }
    }

    private fun drawBottom(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val rowH = rect.height() / 2f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(125, 145, 104, 61); strokeWidth = 1f }
        canvas.drawLine(rect.left + rect.width() * .07f, rect.top + rowH, rect.right - rect.width() * .02f, rect.top + rowH, line)

        val iconBoxW = rect.width() * .18f
        drawBookIcon(canvas, RectF(rect.left + iconBoxW * .18f, rect.top + rowH * .19f, rect.left + iconBoxW * .78f, rect.top + rowH * .78f))
        drawCandlesIcon(canvas, RectF(rect.left + iconBoxW * .16f, rect.top + rowH + rowH * .12f, rect.left + iconBoxW * .82f, rect.bottom - rowH * .10f))

        val parsha = day.parshaLabel?.takeIf { it.isNotBlank() }?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        drawRtl(canvas, parsha, RectF(rect.left + iconBoxW, rect.top, rect.right, rect.top + rowH), rect.width() * .044f, IVORY, true)

        val hilula = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            else -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
        }
        drawRtl(canvas, hilula, RectF(rect.left + iconBoxW, rect.top + rowH, rect.right, rect.bottom), rect.width() * .040f, IVORY_SOFT, true)
    }

    private fun drawBookIcon(canvas: Canvas, r: RectF) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD; style = Paint.Style.STROKE; strokeWidth = r.width() * .08f; strokeJoin = Paint.Join.ROUND }
        val mid = r.centerX()
        val path = Path().apply {
            moveTo(mid, r.top + r.height() * .16f)
            cubicTo(r.left + r.width() * .62f, r.top, r.left, r.top + r.height() * .10f, r.left, r.bottom - r.height() * .08f)
            cubicTo(r.left + r.width() * .35f, r.bottom - r.height() * .20f, mid - r.width() * .04f, r.bottom - r.height() * .08f, mid, r.bottom)
            cubicTo(mid + r.width() * .04f, r.bottom - r.height() * .08f, r.right - r.width() * .35f, r.bottom - r.height() * .20f, r.right, r.bottom - r.height() * .08f)
            lineTo(r.right, r.top + r.height() * .10f)
            cubicTo(r.right - r.width() * .30f, r.top, mid + r.width() * .12f, r.top, mid, r.top + r.height() * .16f)
        }
        canvas.drawPath(path, p)
        canvas.drawLine(mid, r.top + r.height() * .18f, mid, r.bottom - r.height() * .02f, p)
    }

    private fun drawCandlesIcon(canvas: Canvas, r: RectF) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD; style = Paint.Style.STROKE; strokeWidth = r.width() * .075f; strokeCap = Paint.Cap.ROUND }
        val xs = listOf(r.left + r.width() * .34f, r.left + r.width() * .68f)
        xs.forEachIndexed { index, x ->
            val top = r.top + r.height() * (if (index == 0) .28f else .20f)
            canvas.drawLine(x, top, x, r.bottom - r.height() * .16f, p)
            canvas.drawLine(x - r.width() * .11f, r.bottom - r.height() * .16f, x + r.width() * .11f, r.bottom - r.height() * .16f, p)
            val flame = Path().apply {
                moveTo(x, top - r.height() * .22f)
                cubicTo(x - r.width() * .08f, top - r.height() * .10f, x - r.width() * .07f, top, x, top)
                cubicTo(x + r.width() * .07f, top, x + r.width() * .08f, top - r.height() * .10f, x, top - r.height() * .22f)
            }
            canvas.drawPath(flame, p)
        }
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
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(38, 231, 189, 118) }
        canvas.drawCircle(cx, cy, r * 1.10f, glow)
        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B98950") }
        canvas.drawCircle(cx, cy, r * 1.06f, outer)
        val rim = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5B412C") }
        canvas.drawCircle(cx, cy, r, rim)
        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#CDAA75") }
        canvas.drawCircle(cx, cy, r * .90f, face)
        val aged = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(28, 65, 42, 24) }
        canvas.drawCircle(cx, cy, r * .90f, aged)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#49311F"); strokeWidth = r * .014f }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val o = r * .82f
            val inn = if (i % 5 == 0) r * .70f else r * .77f
            canvas.drawLine(cx + inn * cos(a).toFloat(), cy + inn * sin(a).toFloat(), cx + o * cos(a).toFloat(), cy + o * sin(a).toFloat(), tick)
        }

        val num = textPaint(r * .23f, Color.parseColor("#3A271B"), false).apply { textAlign = Paint.Align.CENTER }
        for (n in listOf(12, 3, 6, 9)) {
            val a = Math.toRadians((n * 30 - 90).toDouble())
            canvas.drawText(n.toString(), cx + r * .60f * cos(a).toFloat(), cy + r * .60f * sin(a).toFloat() + num.textSize * .34f, num)
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .47f, hour * 30f, r * .068f, Color.parseColor("#221813"))
        hand(canvas, cx, cy, r * .68f, min * 6f, r * .045f, Color.parseColor("#221813"))
        hand(canvas, cx, cy, r * .78f, sec * 6f, r * .014f, RED)
        canvas.drawCircle(cx, cy, r * .056f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD })
        canvas.drawCircle(cx, cy, r * .023f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BRONZE })
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
            .setLineSpacing(0f, .94f)
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
