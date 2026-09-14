package com.zmanim.lockscreen.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
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
 * Artistic vintage glass card: transparent enough to preserve the wallpaper,
 * with warm ivory typography, muted gold details and RTL-safe Hebrew layout.
 */
object TransparentGlassCard {
    private val IVORY = Color.parseColor("#FFF7E8")
    private val IVORY_SOFT = Color.parseColor("#EEDFC8")
    private val GOLD = Color.parseColor("#D3AD63")
    private val GOLD_SOFT = Color.parseColor("#B98D4D")
    private val RED = Color.parseColor("#A95549")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class ZCell(val label: String, val value: Date?)

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val card = RectF(w * 0.08f, h * 0.345f, w * 0.92f, h * 0.755f)
        val radius = w * 0.05f

        drawGlass(canvas, card, radius, w, h)
        drawOrnaments(canvas, card, w)

        val titleBox = RectF(card.left + w * .06f, card.top + h * .018f, card.right - w * .06f, card.top + h * .065f)
        drawRtl(canvas, "זמני היום", titleBox, w * .054f, IVORY, true)

        val locBox = RectF(card.left + w * .12f, card.top + h * .061f, card.right - w * .12f, card.top + h * .091f)
        drawRtl(canvas, locationName, locBox, w * .028f, IVORY_SOFT, false)

        val hebBox = RectF(card.left + w * .08f, card.top + h * .094f, card.right - w * .08f, card.top + h * .126f)
        drawRtl(canvas, day.hebrewDate, hebBox, w * .032f, IVORY, true)

        val civil = textPaint(w * .024f, IVORY_SOFT, false).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(day.gregorianDate, card.centerX(), card.top + h * .145f, civil)

        val mainTop = card.top + h * .165f
        val mainBottom = card.top + h * .295f
        val clockCx = card.left + card.width() * .26f
        val clockCy = (mainTop + mainBottom) / 2f
        val clockR = card.width() * .15f
        drawClock(canvas, clockCx, clockCy, clockR)

        val nextRect = RectF(card.left + card.width() * .48f, mainTop, card.right - w * .04f, mainBottom)
        drawNext(canvas, nextRect, day)

        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 225, 196, 145)
            strokeWidth = 1.2f
        }
        val divY = card.top + h * .315f
        canvas.drawLine(card.left + w * .045f, divY, card.right - w * .045f, divY, divider)

        val grid = RectF(card.left + w * .035f, divY + h * .012f, card.right - w * .035f, card.top + h * .382f)
        drawZmanimGrid(canvas, grid, day)

        val bottomDividerY = card.bottom - h * .054f
        canvas.drawLine(card.left + w * .045f, bottomDividerY, card.right - w * .045f, bottomDividerY, divider)
        drawSpecialRow(canvas, RectF(card.left + w * .045f, bottomDividerY + h * .006f, card.right - w * .045f, card.bottom - h * .01f), day)
    }

    private fun drawGlass(canvas: Canvas, card: RectF, radius: Float, w: Float, h: Float) {
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(28, 0, 0, 0)
            setShadowLayer(w * .022f, 0f, h * .004f, Color.argb(90, 0, 0, 0))
        }
        canvas.drawRoundRect(RectF(card.left, card.top + h * .004f, card.right, card.bottom + h * .004f), radius, radius, shadow)
        shadow.clearShadowLayer()

        val glass = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                card.left, card.top, card.right, card.bottom,
                intArrayOf(Color.argb(72, 42, 38, 32), Color.argb(50, 26, 24, 22), Color.argb(62, 54, 47, 38)),
                floatArrayOf(0f, .55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(card, radius, radius, glass)

        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(28, 255, 246, 224)
            style = Paint.Style.STROKE
            strokeWidth = w * .005f
        }
        canvas.drawRoundRect(RectF(card.left + 2f, card.top + 2f, card.right - 2f, card.bottom - 2f), radius * .94f, radius * .94f, highlight)

        val goldBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 211, 173, 99)
            style = Paint.Style.STROKE
            strokeWidth = w * .0018f
        }
        canvas.drawRoundRect(RectF(card.left + w * .008f, card.top + w * .008f, card.right - w * .008f, card.bottom - w * .008f), radius * .82f, radius * .82f, goldBorder)
    }

    private fun drawOrnaments(canvas: Canvas, card: RectF, w: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(170, 211, 173, 99)
            strokeWidth = w * .0018f
        }
        val y = card.top + card.height() * .12f
        val cx = card.centerX()
        val gap = card.width() * .11f
        val len = card.width() * .21f
        canvas.drawLine(cx - gap - len, y, cx - gap, y, p)
        canvas.drawLine(cx + gap, y, cx + gap + len, y, p)
        drawDiamond(canvas, cx - gap * .78f, y, w * .006f, p)
        drawDiamond(canvas, cx + gap * .78f, y, w * .006f, p)
    }

    private fun drawDiamond(canvas: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        val path = android.graphics.Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r, cy)
            close()
        }
        canvas.drawPath(path, p)
    }

    private fun drawNext(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val next = nextUpcoming(day)
        val now = Date()
        val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(24, 238, 211, 164) }
        canvas.drawRoundRect(rect, rect.height() * .16f, rect.height() * .16f, panel)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 211, 173, 99)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rect, rect.height() * .16f, rect.height() * .16f, stroke)

        drawRtl(canvas, "הזמן הקרוב", RectF(rect.left, rect.top + rect.height() * .08f, rect.right, rect.top + rect.height() * .30f), rect.width() * .075f, IVORY_SOFT, false)
        drawRtl(canvas, next.first, RectF(rect.left + 4f, rect.top + rect.height() * .31f, rect.right - 4f, rect.top + rect.height() * .56f), rect.width() * .095f, IVORY, true)

        val time = textPaint(rect.width() * .14f, GOLD, true).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(ZmanimProvider.formatTime(next.second), rect.centerX(), rect.top + rect.height() * .76f, time)

        val mins = next.second?.let { ((it.time - now.time) / 60000L).coerceAtLeast(0) }
        if (mins != null) {
            drawRtl(canvas, "בעוד $mins דקות", RectF(rect.left, rect.bottom - rect.height() * .18f, rect.right, rect.bottom - rect.height() * .02f), rect.width() * .064f, IVORY_SOFT, false)
        }
    }

    private fun drawZmanimGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val cells = entries(day)
        val cols = 4
        val rows = 2
        val cellW = rect.width() / cols
        val cellH = rect.height() / rows
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(42, 238, 223, 200); strokeWidth = 1f }

        for (c in 1 until cols) {
            val x = rect.left + cellW * c
            canvas.drawLine(x, rect.top + 4f, x, rect.bottom - 4f, line)
        }
        canvas.drawLine(rect.left + 4f, rect.top + cellH, rect.right - 4f, rect.top + cellH, line)

        cells.forEachIndexed { index, cell ->
            val col = index % cols
            val row = index / cols
            val box = RectF(rect.left + col * cellW, rect.top + row * cellH, rect.left + (col + 1) * cellW, rect.top + (row + 1) * cellH)
            drawRtl(canvas, cell.label, RectF(box.left + 4f, box.top + box.height() * .08f, box.right - 4f, box.top + box.height() * .50f), cellW * .12f, IVORY_SOFT, false)
            val time = textPaint(cellW * .15f, IVORY, true).apply { textAlign = Paint.Align.CENTER }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - box.height() * .12f, time)
        }
    }

    private fun drawSpecialRow(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val mid = rect.centerX()
        val sep = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(50, 238, 223, 200); strokeWidth = 1f }
        canvas.drawLine(mid, rect.top + 3f, mid, rect.bottom - 3f, sep)

        val left = RectF(rect.left, rect.top, mid - 4f, rect.bottom)
        val right = RectF(mid + 4f, rect.top, rect.right, rect.bottom)
        drawRtl(canvas, "כניסת שבת\n${ZmanimProvider.formatTime(day.candleLighting)}", left, rect.width() * .035f, IVORY, true)

        val special = when {
            !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            !day.parshaLabel.isNullOrBlank() -> "פרשת ${day.parshaLabel}"
            else -> "שבת שלום"
        }
        drawRtl(canvas, special ?: "", right, rect.width() * .033f, GOLD, true)
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

    private fun nextUpcoming(day: DayZmanim): Pair<String, Date?> {
        val now = Date()
        return entries(day).firstOrNull { it.value?.after(now) == true }?.let { it.label to it.value }
            ?: ("מחר" to null)
    }

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(24, 255, 244, 218) }
        canvas.drawCircle(cx, cy, r * 1.05f, glow)

        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(26, 255, 249, 237) }
        canvas.drawCircle(cx, cy, r, face)
        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 211, 173, 99)
            style = Paint.Style.STROKE
            strokeWidth = r * .028f
        }
        canvas.drawCircle(cx, cy, r, outer)
        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 255, 245, 220)
            style = Paint.Style.STROKE
            strokeWidth = r * .012f
        }
        canvas.drawCircle(cx, cy, r * .89f, inner)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 255, 244, 218); strokeWidth = r * .01f }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val o = r * .82f
            val inn = if (i % 5 == 0) r * .73f else r * .78f
            canvas.drawLine(cx + inn * cos(a).toFloat(), cy + inn * sin(a).toFloat(), cx + o * cos(a).toFloat(), cy + o * sin(a).toFloat(), tick)
        }

        val num = textPaint(r * .20f, IVORY, false).apply { textAlign = Paint.Align.CENTER }
        for (n in 1..12) {
            val a = Math.toRadians((n * 30 - 90).toDouble())
            canvas.drawText(n.toString(), cx + r * .61f * cos(a).toFloat(), cy + r * .61f * sin(a).toFloat() + num.textSize * .33f, num)
        }

        val cal = Calendar.getInstance()
        val sec = cal.get(Calendar.SECOND)
        val min = cal.get(Calendar.MINUTE) + sec / 60f
        val hour = cal.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .48f, hour * 30f, r * .055f, IVORY)
        hand(canvas, cx, cy, r * .68f, min * 6f, r * .035f, IVORY_SOFT)
        hand(canvas, cx, cy, r * .76f, sec * 6f, r * .012f, RED)
        canvas.drawCircle(cx, cy, r * .055f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, length: Float, angle: Float, width: Float, color: Int) {
        val a = Math.toRadians((angle - 90f).toDouble())
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; strokeWidth = width; strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(cx, cy, cx + length * cos(a).toFloat(), cy + length * sin(a).toFloat(), p)
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) SERIF_BOLD else SERIF
        setShadowLayer(2.5f, 0f, 1f, Color.argb(120, 0, 0, 0))
    }

    private fun drawRtl(canvas: Canvas, text: String, box: RectF, size: Float, color: Int, bold: Boolean) {
        if (text.isBlank() || box.width() <= 0f || box.height() <= 0f) return
        val paint = textPaint(size, color, bold)
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, box.width().toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setLineSpacing(0f, 0.92f)
            .build()
        val y = box.top + ((box.height() - layout.height) / 2f).coerceAtLeast(0f)
        canvas.save()
        canvas.translate(box.left, y)
        layout.draw(canvas)
        canvas.restore()
    }
}
