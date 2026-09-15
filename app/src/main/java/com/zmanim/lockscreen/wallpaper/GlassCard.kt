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

/** Code-drawn antique bronze/glass card inspired by the approved reference. */
object GlassCard {
    private val GOLD = Color.parseColor("#C89342")
    private val GOLD_LIGHT = Color.parseColor("#F0D18C")
    private val GOLD_DARK = Color.parseColor("#65401C")
    private val BRONZE = Color.parseColor("#6E4A27")
    private val TEXT = Color.parseColor("#F5DFB0")
    private val INK = Color.parseColor("#22170E")
    private val RED = Color.parseColor("#C94932")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class ZCell(val label: String, val value: Date?)

    @Suppress("UNUSED_PARAMETER")
    fun draw(canvas: Canvas, plaque: Bitmap?, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()

        // Tall antique plaque, still leaving breathing room around Samsung lock-screen UI.
        val cardW = w * .76f
        val cardH = h * .455f
        val top = h * .34f
        val rect = RectF((w - cardW) / 2f, top, (w + cardW) / 2f, top + cardH)

        drawAntiquePlate(canvas, rect, cardW * .045f)
        drawOrnateCorners(canvas, rect)
        drawTop(canvas, rect, day, locationName)
        drawGrid(canvas, rect, day)
        drawFooter(canvas, rect, day)
    }

    private fun drawAntiquePlate(canvas: Canvas, rect: RectF, radius: Float) {
        // Local drop shadow only. Nothing outside the card is darkened.
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(12, 0, 0, 0)
            setShadowLayer(rect.width() * .010f, 0f, rect.width() * .006f, Color.argb(55, 0, 0, 0))
        }
        canvas.drawRoundRect(RectF(rect.left + 1f, rect.top + 3f, rect.right + 1f, rect.bottom + 3f), radius, radius, shadow)
        shadow.clearShadowLayer()

        // Stronger antique bronze than before, but still translucent so the chosen wallpaper remains visible.
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(
                    Color.argb(108, 151, 103, 52),
                    Color.argb(96, 91, 58, 31),
                    Color.argb(88, 42, 27, 17)
                ),
                floatArrayOf(0f, .50f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, fill)

        // Hammered / scratched bronze texture like the reference image.
        canvas.save()
        val clip = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
        canvas.clipPath(clip)
        val rnd = Random(5783L)
        val texture = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(300) {
            val x = rect.left + rnd.nextFloat() * rect.width()
            val y = rect.top + rnd.nextFloat() * rect.height()
            val r = rect.width() * (.0008f + rnd.nextFloat() * .0027f)
            texture.color = if (rnd.nextBoolean()) Color.argb(10, 255, 221, 145) else Color.argb(11, 36, 18, 8)
            canvas.drawOval(RectF(x - r * 2.2f, y - r, x + r * 2.2f, y + r), texture)
        }
        repeat(65) {
            val x = rect.left + rnd.nextFloat() * rect.width()
            val y = rect.top + rnd.nextFloat() * rect.height()
            texture.color = Color.argb(9, 239, 196, 111)
            texture.strokeWidth = rect.width() * .0006f
            canvas.drawLine(x, y, x + rect.width() * (.015f + rnd.nextFloat() * .05f), y + rnd.nextFloat() * 2f - 1f, texture)
        }
        canvas.restore()

        // Triple bevel, closer to the ornate reference frame.
        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .010f
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(GOLD_LIGHT, GOLD, GOLD_DARK, GOLD_LIGHT), null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, outer)

        val bevel1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .0032f
            color = Color.argb(220, 88, 53, 22)
        }
        val inset1 = rect.width() * .012f
        canvas.drawRoundRect(RectF(rect.left + inset1, rect.top + inset1, rect.right - inset1, rect.bottom - inset1), radius * .84f, radius * .84f, bevel1)

        val bevel2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .0018f
            color = Color.argb(205, 240, 203, 126)
        }
        val inset2 = rect.width() * .022f
        canvas.drawRoundRect(RectF(rect.left + inset2, rect.top + inset2, rect.right - inset2, rect.bottom - inset2), radius * .70f, radius * .70f, bevel2)
    }

    private fun drawOrnateCorners(canvas: Canvas, rect: RectF) {
        fun corner(x: Float, y: Float, sx: Float, sy: Float) {
            val s = rect.width() * .070f
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.argb(225, 226, 178, 89)
                strokeWidth = rect.width() * .0034f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            val path = Path().apply {
                moveTo(x, y + sy * s)
                cubicTo(x + sx * s * .08f, y + sy * s * .55f, x + sx * s * .28f, y + sy * s * .18f, x + sx * s, y)
                moveTo(x + sx * s * .15f, y + sy * s * .78f)
                cubicTo(x + sx * s * .48f, y + sy * s * .60f, x + sx * s * .32f, y + sy * s * .28f, x + sx * s * .15f, y + sy * s * .18f)
                moveTo(x + sx * s * .38f, y + sy * s * .56f)
                cubicTo(x + sx * s * .72f, y + sy * s * .52f, x + sx * s * .65f, y + sy * s * .20f, x + sx * s * .48f, y + sy * s * .14f)
            }
            canvas.drawPath(path, p)
            canvas.drawCircle(x + sx * s * .36f, y + sy * s * .37f, rect.width() * .006f, p)
        }
        val m = rect.width() * .035f
        corner(rect.left + m, rect.top + m, 1f, 1f)
        corner(rect.right - m, rect.top + m, -1f, 1f)
        corner(rect.left + m, rect.bottom - m, 1f, -1f)
        corner(rect.right - m, rect.bottom - m, -1f, -1f)
    }

    private fun drawTop(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String) {
        val w = rect.width()
        val h = rect.height()

        val clockCx = rect.left + w * .28f
        val clockCy = rect.top + h * .205f
        val clockR = w * .185f
        drawClock(canvas, clockCx, clockCy, clockR)

        val right = RectF(rect.left + w * .50f, rect.top + h * .040f, rect.right - w * .045f, rect.top + h * .355f)
        embossedRtl(canvas, "זמני היום", RectF(right.left, right.top, right.right, right.top + right.height() * .27f), w * .064f, true)
        embossedRtl(canvas, locationName, RectF(right.left, right.top + right.height() * .27f, right.right, right.top + right.height() * .49f), w * .041f, false)
        drawPin(canvas, right.right - w * .035f, right.top + right.height() * .38f, w * .016f)
        embossedRtl(canvas, day.hebrewDate, RectF(right.left, right.top + right.height() * .52f, right.right, right.top + right.height() * .74f), w * .040f, true)

        val gp = textPaint(w * .030f, TEXT, false).apply {
            textAlign = Paint.Align.CENTER
            setShadowLayer(w * .003f, 0f, w * .002f, Color.argb(130, 0, 0, 0))
        }
        canvas.drawText(day.gregorianDate, right.centerX(), right.top + right.height() * .94f, gp)
    }

    private fun drawGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()
        val grid = RectF(rect.left + w * .048f, rect.top + h * .385f, rect.right - w * .048f, rect.top + h * .735f)
        val radius = w * .028f

        // Dark inset panel like the sample, still translucent.
        canvas.drawRoundRect(grid, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(72, 35, 22, 12) })
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * .0030f
            color = Color.argb(205, 207, 150, 70)
        }
        canvas.drawRoundRect(grid, radius, radius, border)

        val cw = grid.width() / 4f
        val ch = grid.height() / 2f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 177, 119, 52)
            strokeWidth = w * .0016f
        }
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
            embossedRtl(canvas, cell.label, RectF(box.left + 4f, box.top + ch * .04f, box.right - 4f, box.top + ch * .46f), cw * .155f, true)
            val tp = textPaint(cw * .235f, GOLD_LIGHT, true).apply {
                textAlign = Paint.Align.CENTER
                setShadowLayer(cw * .015f, 0f, cw * .011f, Color.argb(150, 0, 0, 0))
            }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - ch * .12f, tp)
        }
    }

    private fun drawFooter(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()
        val top = rect.top + h * .755f
        val bottom = rect.bottom - h * .045f
        val mid = top + (bottom - top) * .50f

        drawBook(canvas, rect.left + w * .21f, top + (mid - top) * .50f, w * .027f)
        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        embossedRtl(canvas, parsha, RectF(rect.left + w * .27f, top, rect.right - w * .06f, mid), w * .037f, false)

        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val bottomText = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            day.roshChodeshInDays == 0 && !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            isFriday -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
            else -> null
        }

        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(130, 190, 135, 61)
            strokeWidth = w * .0012f
        }
        canvas.drawLine(rect.left + w * .13f, mid, rect.right - w * .08f, mid, divider)
        drawDiamond(canvas, rect.centerX(), mid, w * .008f)

        if (!bottomText.isNullOrBlank()) {
            drawCandles(canvas, rect.left + w * .20f, mid + (bottom - mid) * .52f, w * .025f)
            embossedRtl(canvas, bottomText, RectF(rect.left + w * .27f, mid, rect.right - w * .06f, bottom), w * .032f, false)
        }
    }

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(35, 0, 0, 0)
            setShadowLayer(r * .10f, 0f, r * .04f, Color.argb(100, 0, 0, 0))
        }
        canvas.drawCircle(cx, cy + r * .02f, r * 1.07f, shadow)
        shadow.clearShadowLayer()

        val bezel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx - r * .15f, cy - r * .18f, r * 1.15f,
                intArrayOf(Color.parseColor("#F1CF82"), GOLD, GOLD_DARK, Color.parseColor("#2A1B10")), null, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx, cy, r * 1.06f, bezel)
        canvas.drawCircle(cx, cy, r * .96f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(210, 49, 31, 18) })

        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .23f, cy - r * .27f, r,
                intArrayOf(Color.argb(235, 241, 207, 132), Color.argb(225, 195, 148, 75), Color.argb(214, 119, 79, 35)),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r * .90f, face)

        val innerRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .015f
            color = Color.argb(180, 75, 45, 18)
        }
        canvas.drawCircle(cx, cy, r * .86f, innerRing)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 48, 29, 13)
            strokeCap = Paint.Cap.ROUND
        }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val out = r * .79f
            val inn = if (i % 5 == 0) r * .64f else r * .72f
            tick.strokeWidth = if (i % 5 == 0) r * .022f else r * .009f
            canvas.drawLine(cx + inn * cos(a).toFloat(), cy + inn * sin(a).toFloat(), cx + out * cos(a).toFloat(), cy + out * sin(a).toFloat(), tick)
        }

        val num = textPaint(r * .23f, INK, false).apply { textAlign = Paint.Align.CENTER }
        listOf(12 to 0, 3 to 90, 6 to 180, 9 to 270).forEach { (n, deg) ->
            val a = Math.toRadians((deg - 90).toDouble())
            canvas.drawText(n.toString(), cx + r * .54f * cos(a).toFloat(), cy + r * .54f * sin(a).toFloat() + num.textSize * .33f, num)
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .44f, hour * 30f, r * .060f, Color.parseColor("#1C130C"))
        hand(canvas, cx, cy, r * .64f, min * 6f, r * .040f, Color.parseColor("#1C130C"))
        hand(canvas, cx, cy, r * .72f, sec * 6f, r * .012f, RED)
        canvas.drawCircle(cx, cy, r * .052f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT })
        canvas.drawCircle(cx, cy, r * .024f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, len: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90).toDouble())
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy, cx + len * cos(a).toFloat(), cy + len * sin(a).toFloat(), p)
    }

    private fun drawPin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD_LIGHT
            style = Paint.Style.STROKE
            strokeWidth = r * .28f
        }
        val path = Path().apply {
            moveTo(cx, cy + r * 1.35f)
            cubicTo(cx - r * 1.2f, cy + r * .15f, cx - r, cy - r * 1.1f, cx, cy - r * 1.1f)
            cubicTo(cx + r, cy - r * 1.1f, cx + r * 1.2f, cy + r * .15f, cx, cy + r * 1.35f)
        }
        canvas.drawPath(path, p)
        canvas.drawCircle(cx, cy - r * .25f, r * .34f, p)
    }

    private fun drawBook(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD_LIGHT
            style = Paint.Style.STROKE
            strokeWidth = r * .18f
            strokeJoin = Paint.Join.ROUND
        }
        val path = Path().apply {
            moveTo(cx, cy - r * .5f); lineTo(cx - r, cy - r * .78f); lineTo(cx - r, cy + r * .58f); lineTo(cx, cy + r * .35f)
            moveTo(cx, cy - r * .5f); lineTo(cx + r, cy - r * .78f); lineTo(cx + r, cy + r * .58f); lineTo(cx, cy + r * .35f)
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
        for (dx in floatArrayOf(-r * .42f, r * .42f)) {
            canvas.drawRect(cx + dx - r * .15f, cy - r * .12f, cx + dx + r * .15f, cy + r * .70f, p)
            val flame = Path().apply {
                moveTo(cx + dx, cy - r * .90f)
                cubicTo(cx + dx - r * .30f, cy - r * .53f, cx + dx - r * .15f, cy - r * .29f, cx + dx, cy - r * .22f)
                cubicTo(cx + dx + r * .15f, cy - r * .29f, cx + dx + r * .30f, cy - r * .53f, cx + dx, cy - r * .90f)
            }
            canvas.drawPath(flame, p)
        }
    }

    private fun drawDiamond(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(165, 221, 166, 76)
            style = Paint.Style.STROKE
            strokeWidth = r * .20f
        }
        val path = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r, cy)
            close()
        }
        canvas.drawPath(path, p)
    }

    private fun embossedRtl(canvas: Canvas, text: String, box: RectF, size: Float, bold: Boolean) {
        // Small highlight + dark shadow gives the engraved / raised lettering seen in the reference.
        val shadowPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            color = Color.argb(150, 71, 40, 14)
            typeface = if (bold) SERIF_BOLD else SERIF
        }
        drawRtlLayout(canvas, text, box, shadowPaint, size * .018f, size * .030f)

        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            color = GOLD_LIGHT
            typeface = if (bold) SERIF_BOLD else SERIF
            setShadowLayer(size * .045f, 0f, size * .030f, Color.argb(160, 27, 14, 5))
        }
        drawRtlLayout(canvas, text, box, p, 0f, 0f)
    }

    private fun drawRtlLayout(canvas: Canvas, text: String, box: RectF, paint: TextPaint, dx: Float, dy: Float) {
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, box.width().toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setMaxLines(2)
            .build()
        canvas.save()
        canvas.translate(box.left + dx, box.top + (box.height() - layout.height) / 2f + dy)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) SERIF_BOLD else SERIF
    }
}
