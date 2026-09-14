package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
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
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

/** Fully code-drawn antique bronze plaque. The user's selected wallpaper stays visible behind it. */
object GlassCard {
    private val GOLD = Color.parseColor("#C79A4E")
    private val GOLD_LIGHT = Color.parseColor("#E8C87D")
    private val GOLD_DARK = Color.parseColor("#6D4821")
    private val INK = Color.parseColor("#24160D")
    private val IVORY = Color.parseColor("#EBD8B5")
    private val RED = Color.parseColor("#A9362C")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class ZCell(val label: String, val value: Date?)

    @Suppress("UNUSED_PARAMETER")
    fun draw(canvas: Canvas, plaque: Bitmap?, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()

        // Smaller than before so the plaque does not dominate the lock screen.
        val cardW = w * .78f
        val cardH = h * .50f
        val top = h * .37f
        val rect = RectF((w - cardW) / 2f, top, (w + cardW) / 2f, top + cardH)

        drawMetal(canvas, rect, cardW * .045f)
        drawFiligree(canvas, rect)
        drawTop(canvas, rect, day, locationName)
        drawGrid(canvas, rect, day)
        drawFooter(canvas, rect, day)
    }

    private fun drawMetal(canvas: Canvas, rect: RectF, radius: Float) {
        // Very light shadow only; no dark overlay over the user's photo.
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(12, 0, 0, 0)
            setShadowLayer(rect.width() * .010f, 0f, rect.width() * .006f, Color.argb(32, 0, 0, 0))
        }
        canvas.drawRoundRect(RectF(rect.left + 1f, rect.top + 2f, rect.right + 1f, rect.bottom + 2f), radius, radius, shadow)
        shadow.clearShadowLayer()

        // Much more transparent bronze so the selected lock-screen photo remains clearly visible.
        val base = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(
                    Color.argb(82, 126, 86, 43),
                    Color.argb(76, 83, 53, 27),
                    Color.argb(70, 43, 27, 16)
                ),
                floatArrayOf(0f, .52f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, base)

        // Faint antique metal grain that does not hide the background image.
        canvas.save()
        val clip = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
        canvas.clipPath(clip)
        val rnd = Random(1776L)
        val grain = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(240) {
            val x = rect.left + rnd.nextFloat() * rect.width()
            val y = rect.top + rnd.nextFloat() * rect.height()
            val rr = rect.width() * (.0012f + rnd.nextFloat() * .003f)
            grain.color = if (rnd.nextBoolean()) Color.argb(6, 255, 220, 150) else Color.argb(7, 24, 11, 4)
            canvas.drawOval(RectF(x - rr * 2f, y - rr, x + rr * 2f, y + rr), grain)
        }
        repeat(45) {
            val x = rect.left + rnd.nextFloat() * rect.width()
            val y = rect.top + rnd.nextFloat() * rect.height()
            grain.color = Color.argb(6, 236, 190, 110)
            grain.strokeWidth = rect.width() * .00055f
            canvas.drawLine(x, y, x + rect.width() * (.02f + rnd.nextFloat() * .04f), y + rnd.nextFloat() * 2f - 1f, grain)
        }
        canvas.restore()

        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .009f
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(GOLD_LIGHT, GOLD, GOLD_DARK, GOLD_LIGHT), null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, outer)

        val secondRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .003f
            color = Color.argb(170, 111, 72, 31)
        }
        val rimInset = rect.width() * .010f
        canvas.drawRoundRect(RectF(rect.left + rimInset, rect.top + rimInset, rect.right - rimInset, rect.bottom - rimInset), radius * .86f, radius * .86f, secondRim)

        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .0017f
            color = Color.argb(160, 232, 190, 107)
        }
        val inset = rect.width() * .020f
        canvas.drawRoundRect(RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset), radius * .72f, radius * .72f, inner)
    }

    private fun drawTop(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String) {
        val w = rect.width()
        val h = rect.height()
        val clockCx = rect.left + w * .27f
        val clockCy = rect.top + h * .22f
        val clockR = w * .19f
        drawClock(canvas, clockCx, clockCy, clockR)

        val right = RectF(rect.left + w * .50f, rect.top + h * .055f, rect.right - w * .055f, rect.top + h * .39f)
        embossRtl(canvas, "זמני היום", RectF(right.left, right.top, right.right, right.top + right.height() * .27f), w * .064f, true)
        embossRtl(canvas, locationName, RectF(right.left, right.top + right.height() * .28f, right.right, right.top + right.height() * .48f), w * .040f, false)
        drawPin(canvas, right.right - w * .035f, right.top + right.height() * .38f, w * .016f)
        embossRtl(canvas, day.hebrewDate, RectF(right.left, right.top + right.height() * .54f, right.right, right.top + right.height() * .73f), w * .036f, true)

        val p = textPaint(w * .027f, IVORY, false).apply { textAlign = Paint.Align.CENTER }
        p.setShadowLayer(w * .0025f, 0f, w * .0025f, Color.argb(115, 0, 0, 0))
        canvas.drawText(day.gregorianDate, right.centerX(), right.top + right.height() * .90f, p)
    }

    private fun drawGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()
        val grid = RectF(rect.left + w * .055f, rect.top + h * .405f, rect.right - w * .055f, rect.top + h * .735f)
        val radius = w * .03f
        canvas.drawRoundRect(grid, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(18, 22, 13, 7) })
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * .0025f
            color = Color.argb(175, 190, 139, 70)
        }
        canvas.drawRoundRect(grid, radius, radius, border)

        val cw = grid.width() / 4f
        val ch = grid.height() / 2f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(110, 166, 119, 60); strokeWidth = w * .0012f }
        for (c in 1 until 4) canvas.drawLine(grid.left + cw * c, grid.top, grid.left + cw * c, grid.bottom, line)
        canvas.drawLine(grid.left, grid.top + ch, grid.right, grid.top + ch, line)

        val cells = listOf(
            ZCell("סוף זמן ק״ש", day.sofZmanShmaGra), ZCell("זריחה", day.netzHachama),
            ZCell("תפילה", day.sofZmanTefila), ZCell("עלות השחר", day.alosHashachar),
            ZCell("צאת הכוכבים", day.tzais), ZCell("שקיעה", day.shkia),
            ZCell("מנחה קטנה", day.minchaKetana), ZCell("מנחה גדולה", day.minchaGedola)
        )
        cells.forEachIndexed { i, cell ->
            val col = i % 4
            val row = i / 4
            val box = RectF(grid.left + col * cw, grid.top + row * ch, grid.left + (col + 1) * cw, grid.top + (row + 1) * ch)
            embossRtl(canvas, cell.label, RectF(box.left + 4f, box.top + ch * .07f, box.right - 4f, box.top + ch * .50f), cw * .16f, true)
            val tp = textPaint(cw * .215f, GOLD_LIGHT, true).apply {
                textAlign = Paint.Align.CENTER
                setShadowLayer(cw * .014f, 0f, cw * .010f, Color.argb(105, 0, 0, 0))
            }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - ch * .12f, tp)
        }
    }

    private fun drawFooter(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()
        val top = rect.top + h * .755f
        val bottom = rect.bottom - h * .055f
        val mid = top + (bottom - top) * .52f
        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(95, 168, 119, 59); strokeWidth = w * .0011f }
        canvas.drawLine(rect.left + w * .13f, mid, rect.right - w * .08f, mid, divider)

        drawBook(canvas, rect.left + w * .18f, top + (mid - top) * .48f, w * .028f)
        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        embossRtl(canvas, parsha, RectF(rect.left + w * .25f, top, rect.right - w * .07f, mid), w * .035f, false)

        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val bottomText: String? = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            day.roshChodeshInDays == 0 && !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            isFriday -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
            else -> null
        }

        if (!bottomText.isNullOrBlank()) {
            drawCandles(canvas, rect.left + w * .18f, mid + (bottom - mid) * .50f, w * .026f)
            embossRtl(canvas, bottomText, RectF(rect.left + w * .25f, mid, rect.right - w * .07f, bottom), w * .031f, false)
        }
    }

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val bezel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, r * 1.05f, intArrayOf(GOLD_LIGHT, GOLD_DARK, Color.parseColor("#3D2918")), null, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx, cy, r * 1.05f, bezel)
        canvas.drawCircle(cx, cy, r * .94f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(160, 61, 41, 24) })
        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .2f, cy - r * .25f, r,
                intArrayOf(Color.argb(225, 225, 194, 125), Color.argb(220, 192, 150, 82), Color.argb(215, 134, 96, 47)), null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r * .88f, face)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(210, 49, 29, 13); strokeCap = Paint.Cap.ROUND }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val out = r * .78f
            val inn = if (i % 5 == 0) r * .66f else r * .72f
            tick.strokeWidth = if (i % 5 == 0) r * .018f else r * .008f
            canvas.drawLine(cx + inn * cos(a).toFloat(), cy + inn * sin(a).toFloat(), cx + out * cos(a).toFloat(), cy + out * sin(a).toFloat(), tick)
        }
        val np = textPaint(r * .22f, INK, false).apply { textAlign = Paint.Align.CENTER }
        listOf(12 to 0, 3 to 90, 6 to 180, 9 to 270).forEach { (n, d) ->
            val a = Math.toRadians((d - 90).toDouble())
            canvas.drawText(n.toString(), cx + r * .55f * cos(a).toFloat(), cy + r * .55f * sin(a).toFloat() + np.textSize * .32f, np)
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .43f, hour * 30f, r * .060f, Color.parseColor("#21150C"))
        hand(canvas, cx, cy, r * .64f, min * 6f, r * .040f, Color.parseColor("#21150C"))
        hand(canvas, cx, cy, r * .70f, sec * 6f, r * .012f, RED)
        canvas.drawCircle(cx, cy, r * .05f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT })
        canvas.drawCircle(cx, cy, r * .024f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, len: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90).toDouble())
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(cx, cy, cx + len * cos(a).toFloat(), cy + len * sin(a).toFloat(), p)
    }

    private fun drawFiligree(canvas: Canvas, rect: RectF) {
        fun one(x: Float, y: Float, sx: Float, sy: Float) {
            val s = rect.width() * .065f
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = GOLD
                style = Paint.Style.STROKE
                strokeWidth = rect.width() * .0036f
                strokeCap = Paint.Cap.ROUND
            }
            val path = Path().apply {
                moveTo(x, y + sy * s)
                cubicTo(x + sx * s * .12f, y + sy * s * .58f, x + sx * s * .42f, y + sy * s * .62f, x + sx * s * .55f, y + sy * s * .34f)
                cubicTo(x + sx * s * .68f, y + sy * s * .05f, x + sx * s * .74f, y, x + sx * s, y)
                moveTo(x + sx * s * .18f, y + sy * s * .75f)
                cubicTo(x + sx * s * .38f, y + sy * s * .45f, x + sx * s * .18f, y + sy * s * .28f, x + sx * s * .08f, y + sy * s * .16f)
            }
            canvas.drawPath(path, p)
            canvas.drawCircle(x + sx * s * .42f, y + sy * s * .38f, rect.width() * .0055f, p)
        }
        val m = rect.width() * .026f
        one(rect.left + m, rect.top + m, 1f, 1f)
        one(rect.right - m, rect.top + m, -1f, 1f)
        one(rect.left + m, rect.bottom - m, 1f, -1f)
        one(rect.right - m, rect.bottom - m, -1f, -1f)
    }

    private fun drawPin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT; style = Paint.Style.STROKE; strokeWidth = r * .25f }
        val path = Path().apply {
            moveTo(cx, cy + r * 1.35f)
            cubicTo(cx - r * 1.2f, cy + r * .15f, cx - r, cy - r * 1.1f, cx, cy - r * 1.1f)
            cubicTo(cx + r, cy - r * 1.1f, cx + r * 1.2f, cy + r * .15f, cx, cy + r * 1.35f)
        }
        canvas.drawPath(path, p)
        canvas.drawCircle(cx, cy - r * .25f, r * .35f, p)
    }

    private fun drawBook(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD_LIGHT
            style = Paint.Style.STROKE
            strokeWidth = r * .18f
            strokeJoin = Paint.Join.ROUND
        }
        val path = Path().apply {
            moveTo(cx, cy - r * .5f); lineTo(cx - r, cy - r * .75f); lineTo(cx - r, cy + r * .55f); lineTo(cx, cy + r * .35f)
            moveTo(cx, cy - r * .5f); lineTo(cx + r, cy - r * .75f); lineTo(cx + r, cy + r * .55f); lineTo(cx, cy + r * .35f)
            moveTo(cx, cy - r * .5f); lineTo(cx, cy + r * .35f)
        }
        canvas.drawPath(path, p)
    }

    private fun drawCandles(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD_LIGHT
            style = Paint.Style.STROKE
            strokeWidth = r * .16f
            strokeCap = Paint.Cap.ROUND
        }
        for (dx in floatArrayOf(-r * .45f, r * .45f)) {
            canvas.drawRect(cx + dx - r * .16f, cy - r * .15f, cx + dx + r * .16f, cy + r * .75f, p)
            val flame = Path().apply {
                moveTo(cx + dx, cy - r * .95f)
                cubicTo(cx + dx - r * .32f, cy - r * .55f, cx + dx - r * .16f, cy - r * .3f, cx + dx, cy - r * .22f)
                cubicTo(cx + dx + r * .16f, cy - r * .3f, cx + dx + r * .32f, cy - r * .55f, cx + dx, cy - r * .95f)
            }
            canvas.drawPath(flame, p)
        }
    }

    private fun embossRtl(canvas: Canvas, text: String, box: RectF, size: Float, bold: Boolean) {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            color = GOLD_LIGHT
            typeface = if (bold) SERIF_BOLD else SERIF
            setShadowLayer(size * .055f, size * .018f, size * .038f, Color.argb(135, 33, 18, 7))
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
