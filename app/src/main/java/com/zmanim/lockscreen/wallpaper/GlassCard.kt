package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin

/** Compact parchment card modeled after the approved lock-screen mockup. */
object GlassCard {
    private val PAPER = Color.parseColor("#F4E5C3")
    private val PAPER_DARK = Color.parseColor("#E6CF9B")
    private val INK = Color.parseColor("#3B2818")
    private val INK_SOFT = Color.parseColor("#694A2C")
    private val BRONZE = Color.parseColor("#8A5E2A")
    private val GOLD = Color.parseColor("#B9853B")
    private val RED = Color.parseColor("#A33B2B")

    private data class ZCell(val label: String, val value: Date?)

    fun draw(
        canvas: Canvas,
        background: Bitmap,
        width: Int,
        height: Int,
        day: DayZmanim,
        locationName: String,
        isBrowsing: Boolean = false
    ) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cardW = w * 0.74f
        val cardH = h * 0.42f
        val left = (w - cardW) / 2f
        val top = h * 0.365f
        val rect = RectF(left, top, left + cardW, top + cardH)
        val radius = w * 0.035f

        drawShadow(canvas, rect, radius)
        drawParchment(canvas, rect, radius)
        drawOrnateBorder(canvas, rect, radius)
        drawContent(canvas, rect, day, locationName, isBrowsing)
    }

    private fun drawShadow(canvas: Canvas, rect: RectF, radius: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 38, 20, 8)
            setShadowLayer(24f, 0f, 12f, Color.argb(120, 25, 14, 5))
        }
        canvas.drawRoundRect(RectF(rect.left + 3f, rect.top + 8f, rect.right + 3f, rect.bottom + 8f), radius, radius, p)
        p.clearShadowLayer()
    }

    private fun drawParchment(canvas: Canvas, rect: RectF, radius: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PAPER }
        canvas.drawRoundRect(rect, radius, radius, p)

        val wash = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(28, 139, 91, 39) }
        canvas.drawRoundRect(RectF(rect.left + 6f, rect.top + 6f, rect.right - 6f, rect.bottom - 6f), radius * .85f, radius * .85f, wash)

        val grain = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(25, 80, 45, 20); strokeWidth = 1f }
        val step = (rect.width() / 34f).coerceAtLeast(8f)
        var x = rect.left + step
        var i = 0
        while (x < rect.right - step) {
            val y = rect.top + ((i * 37) % 97) / 97f * rect.height()
            canvas.drawCircle(x, y, 0.7f + (i % 3) * 0.35f, grain)
            x += step
            i++
        }
    }

    private fun drawOrnateBorder(canvas: Canvas, rect: RectF, radius: Float) {
        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BRONZE; style = Paint.Style.STROKE; strokeWidth = 2.4f
        }
        canvas.drawRoundRect(rect, radius, radius, outer)
        val innerRect = RectF(rect.left + 7f, rect.top + 7f, rect.right - 7f, rect.bottom - 7f)
        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(125, 111, 72, 34); style = Paint.Style.STROKE; strokeWidth = 1f
        }
        canvas.drawRoundRect(innerRect, radius * .76f, radius * .76f, inner)

        val flourish = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BRONZE; strokeWidth = 1.6f }
        val y = rect.top + rect.height() * .105f
        val cx = rect.centerX()
        canvas.drawLine(cx - rect.width() * .24f, y, cx - rect.width() * .10f, y, flourish)
        canvas.drawLine(cx + rect.width() * .10f, y, cx + rect.width() * .24f, y, flourish)
        canvas.drawCircle(cx - rect.width() * .09f, y, 2.2f, flourish)
        canvas.drawCircle(cx + rect.width() * .09f, y, 2.2f, flourish)
    }

    private fun drawContent(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String, isBrowsing: Boolean) {
        val w = rect.width()
        val h = rect.height()

        val title = paint(w * .060f, INK, Paint.Align.CENTER, bold = true)
        canvas.drawText("זמני היום", rect.centerX(), rect.top + h * .087f, title)

        val small = paint(w * .023f, INK_SOFT, Paint.Align.CENTER)
        canvas.drawText(locationName, rect.left + w * .11f, rect.top + h * .08f, small)
        canvas.drawText("תורה תמיד", rect.right - w * .11f, rect.top + h * .08f, small)

        val heb = paint(w * .035f, INK, Paint.Align.CENTER, bold = true)
        canvas.drawText(day.hebrewDate, rect.centerX(), rect.top + h * .155f, heb)
        val civil = paint(w * .025f, INK_SOFT, Paint.Align.CENTER)
        canvas.drawText(day.gregorianDate, rect.centerX(), rect.top + h * .19f, civil)

        val arrow = paint(w * .055f, INK_SOFT, Paint.Align.CENTER)
        canvas.drawText("‹", rect.left + w * .19f, rect.top + h * .17f, arrow)
        canvas.drawText("›", rect.right - w * .19f, rect.top + h * .17f, arrow)
        if (isBrowsing) {
            val browse = paint(w * .018f, GOLD, Paint.Align.CENTER, bold = true)
            canvas.drawText("דפדוף בתאריך", rect.centerX(), rect.top + h * .218f, browse)
        }

        val mainTop = rect.top + h * .235f
        val mainBottom = rect.top + h * .555f
        val clockCx = rect.left + w * .25f
        val clockCy = (mainTop + mainBottom) / 2f
        val clockR = h * .145f
        drawClock(canvas, clockCx, clockCy, clockR)

        val nextRect = RectF(rect.left + w * .48f, mainTop + h * .015f, rect.right - w * .055f, mainBottom - h * .015f)
        drawNext(canvas, nextRect, day)

        val dividerY = rect.top + h * .59f
        drawDivider(canvas, rect.left + w * .055f, rect.right - w * .055f, dividerY)

        val gridRect = RectF(rect.left + w * .055f, dividerY + h * .018f, rect.right - w * .055f, rect.top + h * .79f)
        drawZmanimStrip(canvas, gridRect, day)

        val bottomY = rect.top + h * .815f
        drawDivider(canvas, rect.left + w * .055f, rect.right - w * .055f, bottomY)
        val bottomRect = RectF(rect.left + w * .07f, bottomY + h * .02f, rect.right - w * .07f, rect.bottom - h * .035f)
        drawSpecialRow(canvas, bottomRect, day)
    }

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(35, 72, 42, 18) }
        canvas.drawCircle(cx + 3f, cy + 5f, r * 1.03f, shadow)

        val ring1 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BRONZE; style = Paint.Style.STROKE; strokeWidth = r * .075f }
        canvas.drawCircle(cx, cy, r, ring1)
        val ring2 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD; style = Paint.Style.STROKE; strokeWidth = r * .025f }
        canvas.drawCircle(cx, cy, r * .92f, ring2)
        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F7EBCF") }
        canvas.drawCircle(cx, cy, r * .88f, face)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK_SOFT; strokeWidth = r * .012f }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val outer = r * .84f
            val inner = if (i % 5 == 0) r * .75f else r * .80f
            canvas.drawLine(
                cx + inner * cos(a).toFloat(), cy + inner * sin(a).toFloat(),
                cx + outer * cos(a).toFloat(), cy + outer * sin(a).toFloat(), tick
            )
        }

        val num = paint(r * .22f, INK, Paint.Align.CENTER)
        for (n in 1..12) {
            val a = Math.toRadians((n * 30 - 90).toDouble())
            val nr = r * .61f
            val x = cx + nr * cos(a).toFloat()
            val y = cy + nr * sin(a).toFloat() + num.textSize * .34f
            canvas.drawText(n.toString(), x, y, num)
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .48f, hour * 30f - 90f, r * .055f, INK)
        hand(canvas, cx, cy, r * .68f, min * 6f - 90f, r * .035f, INK_SOFT)
        hand(canvas, cx, cy, r * .77f, sec * 6f - 90f, r * .013f, RED)
        canvas.drawCircle(cx, cy, r * .07f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD })
        canvas.drawCircle(cx, cy, r * .035f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, length: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians(deg.toDouble())
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(cx, cy, cx + length * cos(a).toFloat(), cy + length * sin(a).toFloat(), p)
    }

    private fun drawNext(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val entries = entries(day)
        val now = Date()
        val next = entries.firstOrNull { it.value != null && it.value.after(now) } ?: entries.last()

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(95, 232, 207, 158) }
        canvas.drawRoundRect(rect, rect.height() * .10f, rect.height() * .10f, bg)
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(100, 124, 82, 36); style = Paint.Style.STROKE; strokeWidth = 1.3f }
        canvas.drawRoundRect(rect, rect.height() * .10f, rect.height() * .10f, border)

        val small = paint(rect.width() * .07f, INK_SOFT, Paint.Align.CENTER)
        val label = paint(rect.width() * .095f, INK, Paint.Align.CENTER, bold = true)
        val time = paint(rect.width() * .145f, INK, Paint.Align.CENTER, bold = true)
        canvas.drawText("הזמן הקרוב", rect.centerX(), rect.top + rect.height() * .25f, small)
        canvas.drawText(next.label, rect.centerX(), rect.top + rect.height() * .50f, label)
        canvas.drawText(ZmanimProvider.formatTime(next.value), rect.centerX(), rect.top + rect.height() * .76f, time)

        val mins = next.value?.let { ((it.time - now.time) / 60000L).coerceAtLeast(0) }
        if (mins != null) {
            val chip = RectF(rect.left + rect.width() * .19f, rect.bottom - rect.height() * .18f, rect.right - rect.width() * .19f, rect.bottom - rect.height() * .045f)
            canvas.drawRoundRect(chip, chip.height() / 2f, chip.height() / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(70, 144, 94, 38) })
            val p = paint(rect.width() * .055f, INK_SOFT, Paint.Align.CENTER, bold = true)
            canvas.drawText("בעוד $mins דקות", chip.centerX(), chip.centerY() + p.textSize * .35f, p)
        }
    }

    private fun entries(day: DayZmanim): List<ZCell> = listOf(
        ZCell("עלות השחר", day.alosHashachar),
        ZCell("הנץ החמה", day.netzHachama),
        ZCell("סוף זמן ק״ש", day.sofZmanShmaGra),
        ZCell("מנחה גדולה", day.minchaGedola),
        ZCell("חצות", day.chatzos),
        ZCell("פלג המנחה", day.plagHamincha),
        ZCell("שקיעה", day.shkia),
        ZCell("צאת הכוכבים", day.tzais)
    )

    private fun drawZmanimStrip(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val cells = entries(day)
        val cellW = rect.width() / cells.size
        val label = paint(cellW * .22f, INK_SOFT, Paint.Align.CENTER)
        val time = paint(cellW * .28f, INK, Paint.Align.CENTER, bold = true)
        cells.forEachIndexed { i, c ->
            val cx = rect.left + cellW * (i + .5f)
            if (i > 0) canvas.drawLine(rect.left + cellW * i, rect.top + 2f, rect.left + cellW * i, rect.bottom - 2f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(65, 111, 72, 34); strokeWidth = 1f })
            val parts = c.label.split(" ")
            canvas.drawText(parts.first(), cx, rect.top + rect.height() * .33f, label)
            if (parts.size > 1) canvas.drawText(parts.drop(1).joinToString(" "), cx, rect.top + rect.height() * .57f, label)
            canvas.drawText(ZmanimProvider.formatTime(c.value), cx, rect.bottom - rect.height() * .06f, time)
        }
    }

    private fun drawSpecialRow(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val mid = rect.centerX()
        val sep = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(70, 111, 72, 34); strokeWidth = 1f }
        canvas.drawLine(mid, rect.top, mid, rect.bottom, sep)

        val title = paint(rect.width() * .034f, INK, Paint.Align.CENTER, bold = true)
        val value = paint(rect.width() * .047f, INK, Paint.Align.CENTER, bold = true)
        val note = paint(rect.width() * .028f, INK_SOFT, Paint.Align.CENTER)

        val leftCx = rect.left + rect.width() * .25f
        canvas.drawText("🕯  כניסת שבת", leftCx, rect.top + rect.height() * .32f, title)
        canvas.drawText(ZmanimProvider.formatTime(day.candleLighting), leftCx, rect.top + rect.height() * .73f, value)

        val rightCx = rect.left + rect.width() * .75f
        val special = day.parshaLabel ?: day.roshChodeshLabel ?: "אין אירוע מיוחד"
        val specialTitle = if (day.parshaLabel != null) "פרשת השבוע" else "מאורע מיוחד"
        canvas.drawText(specialTitle, rightCx, rect.top + rect.height() * .32f, title)
        canvas.drawText(special, rightCx, rect.top + rect.height() * .67f, value)
        if (day.roshChodeshLabel != null && day.roshChodeshInDays != null && day.parshaLabel != null) {
            val suffix = if (day.roshChodeshInDays == 0) "היום" else "בעוד ${day.roshChodeshInDays} ימים"
            canvas.drawText("${day.roshChodeshLabel} · $suffix", rightCx, rect.bottom - 2f, note)
        }
    }

    private fun drawDivider(canvas: Canvas, x1: Float, x2: Float, y: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(90, 111, 72, 34); strokeWidth = 1.2f }
        canvas.drawLine(x1, y, x2, y, p)
    }

    private fun paint(size: Float, color: Int, align: Paint.Align, bold: Boolean = false): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            textAlign = align
            typeface = if (bold) Typeface.create(Typeface.SERIF, Typeface.BOLD) else Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
}
