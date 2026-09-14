package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
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
 * Draws the approved antique bronze reference as the live zmanim plaque.
 *
 * The reference bitmap supplies the real metal texture, bevel, ornate corners, book/candle
 * illustrations and engraved labels. Only values that must stay live are redrawn: clock hands,
 * location, dates, zmanim times, weekly parsha and hilula. The whole plaque is rendered with
 * controlled alpha so the user's lock-screen photo still shows through the metal instead of
 * being replaced or blurred.
 */
object GlassCard {

    private val GOLD = Color.parseColor("#E6C37A")
    private val IVORY = Color.parseColor("#F6E7C8")
    private val DARK = Color.parseColor("#2B1B0D")
    private val RED = Color.parseColor("#B2382C")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private const val REFERENCE_ASPECT = 826f / 1388f
    private const val PLAQUE_ALPHA = 208

    private data class Cell(val value: Date?, val xFrac: Float)

    fun draw(
        canvas: Canvas,
        plaque: Bitmap?,
        width: Int,
        height: Int,
        day: DayZmanim,
        locationName: String
    ) {
        val w = width.toFloat()
        val h = height.toFloat()

        // Preserve the exact tall proportions of the approved reference and keep it clear of
        // the system clock/weather area at the top and unlock controls at the bottom.
        val maxW = w * 0.79f
        val maxH = h * 0.53f
        val cardW = minOf(maxW, maxH * REFERENCE_ASPECT)
        val cardH = cardW / REFERENCE_ASPECT
        val left = (w - cardW) / 2f
        val top = h * 0.30f
        val rect = RectF(left, top, left + cardW, top + cardH)

        if (plaque != null) {
            val plaquePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                alpha = PLAQUE_ALPHA
            }
            canvas.drawBitmap(plaque, null, rect, plaquePaint)
        } else {
            drawFallbackPlaque(canvas, rect)
        }

        // A very subtle warm wash keeps the metallic look coherent while remaining translucent.
        val wash = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                intArrayOf(
                    Color.argb(12, 255, 220, 150),
                    Color.argb(4, 255, 255, 255),
                    Color.argb(18, 42, 24, 9)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, rect.width() * .045f, rect.width() * .045f, wash)

        drawLiveClock(canvas, rect)
        drawLiveHeader(canvas, rect, day, locationName)
        drawLiveTimes(canvas, rect, day)
        drawLiveBottomRows(canvas, rect, day)
    }

    private fun drawFallbackPlaque(canvas: Canvas, rect: RectF) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                intArrayOf(
                    Color.argb(190, 91, 62, 32),
                    Color.argb(182, 57, 38, 20),
                    Color.argb(190, 38, 24, 12)
                ),
                floatArrayOf(0f, .55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, rect.width() * .045f, rect.width() * .045f, fill)

        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .010f
            color = Color.argb(220, 194, 149, 77)
        }
        canvas.drawRoundRect(rect, rect.width() * .045f, rect.width() * .045f, outer)

        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .0028f
            color = Color.argb(180, 234, 194, 119)
        }
        val inset = rect.width() * .018f
        canvas.drawRoundRect(
            RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset),
            rect.width() * .035f,
            rect.width() * .035f,
            inner
        )
    }

    // ----- dynamic header / clock -----

    private fun drawLiveClock(canvas: Canvas, rect: RectF) {
        val w = rect.width()
        val h = rect.height()
        val cx = rect.left + w * .275f
        val cy = rect.top + h * .205f
        val r = w * .185f

        // Patch only the inner clock face; the real bezel from the reference remains untouched.
        canvas.drawCircle(
            cx,
            cy,
            r * .88f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(210, 224, 191, 125) }
        )

        val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(155, 70, 45, 20)
            style = Paint.Style.STROKE
            strokeWidth = r * .022f
        }
        canvas.drawCircle(cx, cy, r * .86f, edge)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(210, 54, 35, 14)
            strokeWidth = r * .014f
            strokeCap = Paint.Cap.ROUND
        }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val outer = r * .76f
            val inner = if (i % 5 == 0) r * .64f else r * .70f
            canvas.drawLine(
                cx + inner * cos(a).toFloat(),
                cy + inner * sin(a).toFloat(),
                cx + outer * cos(a).toFloat(),
                cy + outer * sin(a).toFloat(),
                tick
            )
        }

        val nums = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3A2818")
            textAlign = Paint.Align.CENTER
            textSize = r * .22f
            typeface = SERIF
        }
        for (n in listOf(12, 3, 6, 9)) {
            val a = Math.toRadians((n * 30 - 90).toDouble())
            canvas.drawText(
                n.toString(),
                cx + r * .54f * cos(a).toFloat(),
                cy + r * .54f * sin(a).toFloat() + nums.textSize * .34f,
                nums
            )
        }

        val now = Calendar.getInstance()
        val second = now.get(Calendar.SECOND)
        val minute = now.get(Calendar.MINUTE) + second / 60f
        val hour = now.get(Calendar.HOUR) + minute / 60f

        drawHand(canvas, cx, cy, r * .43f, hour * 30f, r * .060f, Color.parseColor("#1E150E"))
        drawHand(canvas, cx, cy, r * .62f, minute * 6f, r * .040f, Color.parseColor("#1E150E"))
        drawHand(canvas, cx, cy, r * .69f, second * 6f, r * .013f, RED)
        canvas.drawCircle(cx, cy, r * .045f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD })
        canvas.drawCircle(cx, cy, r * .020f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = DARK })
    }

    private fun drawLiveHeader(
        canvas: Canvas,
        rect: RectF,
        day: DayZmanim,
        locationName: String
    ) {
        val w = rect.width()
        val h = rect.height()
        val x0 = rect.left + w * .49f
        val x1 = rect.right - w * .055f

        // Keep the original "זמני היום" engraving from the reference. Replace only the live
        // fields below it, using translucent bronze patches so the texture still reads as metal.
        bronzePatch(canvas, RectF(x0, rect.top + h * .145f, x1, rect.top + h * .225f))
        drawPin(canvas, rect.left + w * .84f, rect.top + h * .182f, w * .014f)
        drawRtlCentered(
            canvas,
            locationName,
            RectF(x0, rect.top + h * .154f, x1 - w * .03f, rect.top + h * .214f),
            w * .038f,
            GOLD,
            false,
            1
        )

        bronzePatch(canvas, RectF(x0, rect.top + h * .245f, x1, rect.top + h * .315f))
        drawRtlCentered(
            canvas,
            day.hebrewDate,
            RectF(x0, rect.top + h * .253f, x1, rect.top + h * .303f),
            w * .044f,
            IVORY,
            true,
            1
        )

        bronzePatch(canvas, RectF(x0, rect.top + h * .305f, x1, rect.top + h * .365f))
        val english = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = IVORY
            textAlign = Paint.Align.CENTER
            textSize = w * .030f
            typeface = SERIF
            setShadowLayer(w * .002f, 0f, w * .002f, Color.argb(170, 0, 0, 0))
        }
        canvas.drawText(day.gregorianDate, (x0 + x1) / 2f, rect.top + h * .343f, english)
    }

    // ----- dynamic grid values -----

    private fun drawLiveTimes(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val x = floatArrayOf(.17f, .39f, .61f, .83f)
        val top = listOf(
            Cell(day.sofZmanShmaGra, x[0]),
            Cell(day.netzHachama, x[1]),
            Cell(day.sofZmanTefila, x[2]),
            Cell(day.alosHashachar, x[3])
        )
        val bottom = listOf(
            Cell(day.tzais, x[0]),
            Cell(day.shkia, x[1]),
            Cell(day.minchaKetana, x[2]),
            Cell(day.minchaGedola, x[3])
        )

        drawTimeRow(canvas, rect, top, .505f)
        drawTimeRow(canvas, rect, bottom, .705f)
    }

    private fun drawTimeRow(canvas: Canvas, rect: RectF, cells: List<Cell>, yFrac: Float) {
        val w = rect.width()
        val h = rect.height()
        cells.forEach { cell ->
            val cx = rect.left + w * cell.xFrac
            val patch = RectF(
                cx - w * .092f,
                rect.top + h * (yFrac - .033f),
                cx + w * .092f,
                rect.top + h * (yFrac + .040f)
            )
            bronzePatch(canvas, patch)

            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = GOLD
                textAlign = Paint.Align.CENTER
                textSize = w * .050f
                typeface = SERIF_BOLD
                setShadowLayer(w * .0023f, 0f, w * .002f, Color.argb(190, 0, 0, 0))
            }
            canvas.drawText(
                ZmanimProvider.formatTime(cell.value),
                cx,
                rect.top + h * (yFrac + .018f),
                p
            )
        }
    }

    // ----- dynamic parsha / hilula while preserving exact book + candle illustrations -----

    private fun drawLiveBottomRows(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()

        // Keep the baked book and candles; patch only the changing text to their right.
        val parshaBox = RectF(
            rect.left + w * .30f,
            rect.top + h * .785f,
            rect.right - w * .06f,
            rect.top + h * .855f
        )
        bronzePatch(canvas, parshaBox)
        val parshaName = day.parshaLabel?.takeIf { it.isNotBlank() } ?: "—"
        drawRtlCentered(
            canvas,
            "פרשת השבוע: $parshaName",
            parshaBox,
            w * .037f,
            IVORY,
            true,
            1
        )

        val hilulaBox = RectF(
            rect.left + w * .25f,
            rect.top + h * .875f,
            rect.right - w * .055f,
            rect.top + h * .948f
        )
        bronzePatch(canvas, hilulaBox)
        val hilulaText = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            !day.roshChodeshLabel.isNullOrBlank() -> when (day.roshChodeshInDays) {
                0 -> "${day.roshChodeshLabel} · היום"
                1 -> "${day.roshChodeshLabel} · מחר"
                else -> "${day.roshChodeshLabel} · בעוד ${day.roshChodeshInDays} ימים"
            }
            else -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
        }
        drawRtlCentered(
            canvas,
            hilulaText,
            hilulaBox,
            w * .033f,
            IVORY,
            false,
            2
        )
    }

    private fun bronzePatch(canvas: Canvas, rect: RectF) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                intArrayOf(
                    Color.argb(205, 83, 58, 31),
                    Color.argb(216, 63, 43, 23),
                    Color.argb(205, 72, 49, 25)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, rect.height() * .12f, rect.height() * .12f, paint)

        // Tiny scratches/patina lines keep patches from looking like flat rectangles.
        val grain = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(28, 235, 193, 117)
            strokeWidth = 1f
        }
        val step = (rect.height() / 5f).coerceAtLeast(3f)
        var y = rect.top + step
        var i = 0
        while (y < rect.bottom) {
            val start = rect.left + if (i % 2 == 0) rect.width() * .08f else rect.width() * .20f
            val end = rect.right - if (i % 2 == 0) rect.width() * .18f else rect.width() * .06f
            canvas.drawLine(start, y, end, y + 0.8f, grain)
            y += step
            i++
        }
    }

    private fun drawPin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD
            style = Paint.Style.STROKE
            strokeWidth = r * .30f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val path = android.graphics.Path().apply {
            moveTo(cx, cy + r * 1.35f)
            cubicTo(cx - r * 1.25f, cy + r * .20f, cx - r, cy - r * 1.25f, cx, cy - r * 1.25f)
            cubicTo(cx + r, cy - r * 1.25f, cx + r * 1.25f, cy + r * .20f, cx, cy + r * 1.35f)
            close()
        }
        canvas.drawPath(path, p)
        canvas.drawCircle(cx, cy - r * .30f, r * .37f, p)
    }

    private fun drawRtlCentered(
        canvas: Canvas,
        text: String,
        box: RectF,
        size: Float,
        color: Int,
        bold: Boolean,
        maxLines: Int
    ) {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) SERIF_BOLD else SERIF
            setShadowLayer(size * .05f, 0f, size * .04f, Color.argb(170, 0, 0, 0))
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, p, box.width().toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setLineSpacing(0f, .94f)
            .build()

        canvas.save()
        canvas.translate(box.left, box.top + (box.height() - layout.height) / 2f)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawHand(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        length: Float,
        degrees: Float,
        stroke: Float,
        color: Int
    ) {
        val a = Math.toRadians((degrees - 90f).toDouble())
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            setShadowLayer(stroke * .35f, stroke * .12f, stroke * .12f, Color.argb(130, 0, 0, 0))
        }
        canvas.drawLine(
            cx,
            cy,
            cx + length * cos(a).toFloat(),
            cy + length * sin(a).toFloat(),
            p
        )
    }
}
