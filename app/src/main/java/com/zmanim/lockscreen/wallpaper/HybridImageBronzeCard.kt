package com.zmanim.lockscreen.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.zmanim.lockscreen.R
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Single-layer antique bronze renderer.
 *
 * The previous version composited two complete card images and then drew live
 * content over both, which caused the duplicated labels/numbers visible in the
 * wallpaper preview. This version uses only the bronze texture image as material.
 * All structure (frame, dial, grid, ornaments), live text, hands and gears are
 * drawn exactly once in code.
 */
object HybridImageBronzeCard {
    private const val DESIGN_W = 1200f
    private const val DESIGN_H = 1620f

    private val GOLD = Color.parseColor("#D9A95A")
    private val GOLD_LIGHT = Color.parseColor("#F7D88F")
    private val GOLD_DARK = Color.parseColor("#6C421D")
    private val BRONZE_DARK = Color.parseColor("#2B180D")
    private val INK = Color.parseColor("#21150D")
    private val RED = Color.parseColor("#D72F22")
    private val IVORY = Color.parseColor("#EBCB84")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    @Volatile private var cachedBronze: Bitmap? = null
    @Volatile private var gear28: Bitmap? = null
    @Volatile private var gear20: Bitmap? = null
    @Volatile private var gear16: Bitmap? = null
    @Volatile private var gear14: Bitmap? = null
    @Volatile private var gear12: Bitmap? = null

    fun draw(
        context: Context,
        canvas: Canvas,
        width: Int,
        height: Int,
        day: DayZmanim,
        locationName: String
    ) {
        val cardW = width * .79f
        val cardH = cardW * (DESIGN_H / DESIGN_W)
        val top = height * .305f
        val card = RectF(
            (width - cardW) / 2f,
            top,
            (width + cardW) / 2f,
            top + cardH
        )
        val sx = card.width() / DESIGN_W
        val sy = card.height() / DESIGN_H

        val bronze = cachedBronze?.takeIf { !it.isRecycled }
            ?: runCatching { BitmapFactory.decodeResource(context.resources, R.drawable.plaque_bronze) }
                .getOrNull()?.also { cachedBronze = it }

        if (bronze == null) {
            ReferenceBronzeCard.draw(canvas, width, height, day, locationName)
            return
        }

        drawBronzeBase(canvas, bronze, card)
        drawAntiqueFrame(canvas, card)
        drawClockFace(canvas, card, sx, sy)
        drawMechanicalClock(canvas, card, sx, sy)
        drawHeader(canvas, card, sx, sy, day, locationName)
        drawGrid(canvas, card, sx, sy, day)
        drawFooter(canvas, card, sx, sy, day)
    }

    private fun drawBronzeBase(canvas: Canvas, bronze: Bitmap, card: RectF) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
            alpha = 246
        }
        canvas.drawBitmap(bronze, null, card, p)

        canvas.drawRoundRect(
            card,
            card.width() * .035f,
            card.width() * .035f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    card.left,
                    card.top,
                    card.right,
                    card.bottom,
                    intArrayOf(
                        Color.argb(18, 255, 227, 158),
                        Color.argb(52, 83, 48, 23),
                        Color.argb(88, 31, 18, 10)
                    ),
                    floatArrayOf(0f, .52f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        )
    }

    private fun drawAntiqueFrame(canvas: Canvas, card: RectF) {
        val radius = card.width() * .038f
        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = card.width() * .012f
            shader = LinearGradient(
                card.left,
                card.top,
                card.right,
                card.bottom,
                intArrayOf(
                    Color.parseColor("#F3D487"),
                    Color.parseColor("#A56A2B"),
                    Color.parseColor("#593117"),
                    Color.parseColor("#D9A95A")
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(card, radius, radius, outer)

        val inner = RectF(card).apply { inset(card.width() * .021f, card.width() * .021f) }
        canvas.drawRoundRect(inner, radius * .72f, radius * .72f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = card.width() * .0028f
            color = Color.argb(230, 238, 193, 105)
        })

        drawCornerOrnament(canvas, inner.left + 30f, inner.top + 30f, 1f, 1f, card.width() * .12f)
        drawCornerOrnament(canvas, inner.right - 30f, inner.top + 30f, -1f, 1f, card.width() * .12f)
        drawCornerOrnament(canvas, inner.left + 30f, inner.bottom - 30f, 1f, -1f, card.width() * .12f)
        drawCornerOrnament(canvas, inner.right - 30f, inner.bottom - 30f, -1f, -1f, card.width() * .12f)
    }

    private fun drawCornerOrnament(canvas: Canvas, x: Float, y: Float, sx: Float, sy: Float, size: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * .045f
            strokeCap = Paint.Cap.ROUND
            color = Color.argb(225, 231, 190, 103)
        }
        val p = Path()
        p.moveTo(x, y + sy * size * .65f)
        p.cubicTo(x + sx * size * .08f, y + sy * size * .28f, x + sx * size * .28f, y + sy * size * .12f, x + sx * size * .58f, y)
        p.cubicTo(x + sx * size * .38f, y + sy * size * .16f, x + sx * size * .25f, y + sy * size * .34f, x + sx * size * .16f, y + sy * size * .62f)
        canvas.drawPath(p, paint)
        canvas.drawCircle(x + sx * size * .26f, y + sy * size * .27f, size * .07f, paint)
    }

    private fun drawClockFace(canvas: Canvas, card: RectF, sx: Float, sy: Float) {
        val cx = card.left + 325f * sx
        val cy = card.top + 310f * sy
        val r = 230f * sx

        canvas.drawCircle(cx + r * .02f, cy + r * .035f, r * 1.02f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 0, 0, 0)
        })
        canvas.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .28f,
                cy - r * .32f,
                r * 1.05f,
                intArrayOf(Color.parseColor("#F1DDA4"), Color.parseColor("#D2AD61"), Color.parseColor("#8C5B25")),
                floatArrayOf(0f, .72f, 1f),
                Shader.TileMode.CLAMP
            )
        })
        canvas.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .055f
            color = Color.parseColor("#4E2D15")
        })
        canvas.drawCircle(cx, cy, r * .91f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .012f
            color = Color.parseColor("#8A5A27")
        })

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK; strokeCap = Paint.Cap.ROUND }
        repeat(60) { i ->
            val a = Math.toRadians((i * 6.0 - 90.0))
            val major = i % 5 == 0
            val inner = if (major) r * .76f else r * .82f
            val outer = r * .88f
            tick.strokeWidth = if (major) r * .018f else r * .008f
            canvas.drawLine(
                cx + inner * cos(a).toFloat(), cy + inner * sin(a).toFloat(),
                cx + outer * cos(a).toFloat(), cy + outer * sin(a).toFloat(), tick
            )
        }

        val numeral = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = INK
            textAlign = Paint.Align.CENTER
            typeface = SERIF_BOLD
            textSize = r * .24f
        }
        canvas.drawText("12", cx, cy - r * .62f, numeral)
        canvas.drawText("3", cx + r * .64f, cy + r * .08f, numeral)
        canvas.drawText("6", cx, cy + r * .76f, numeral)
        canvas.drawText("9", cx - r * .64f, cy + r * .08f, numeral)
    }

    private fun drawHeader(canvas: Canvas, card: RectF, sx: Float, sy: Float, day: DayZmanim, locationName: String) {
        drawRtl(canvas, "זמני היום", box(card, sx, sy, 610f, 105f, 1110f, 220f), 72f * sx, true)
        drawRtl(canvas, locationName, box(card, sx, sy, 610f, 235f, 985f, 325f), 42f * sx, false)
        drawRtl(canvas, day.hebrewDate, box(card, sx, sy, 555f, 340f, 1085f, 425f), 40f * sx, true)
        drawLtr(canvas, day.gregorianDate, box(card, sx, sy, 535f, 430f, 1085f, 505f), 30f * sx, false)
    }

    private fun drawGrid(canvas: Canvas, card: RectF, sx: Float, sy: Float, day: DayZmanim) {
        val left = 90f
        val top = 600f
        val right = 1110f
        val bottom = 1115f
        val cw = (right - left) / 4f
        val ch = (bottom - top) / 2f
        val grid = box(card, sx, sy, left, top, right, bottom)

        canvas.drawRoundRect(grid, 30f * sx, 30f * sx, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(94, 35, 19, 10)
        })
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.1f * sx
            color = Color.argb(235, 218, 164, 82)
        }
        canvas.drawRoundRect(grid, 30f * sx, 30f * sx, border)
        for (i in 1..3) {
            val x = card.left + (left + cw * i) * sx
            canvas.drawLine(x, grid.top, x, grid.bottom, border)
        }
        val middleY = card.top + (top + ch) * sy
        canvas.drawLine(grid.left, middleY, grid.right, middleY, border)

        val values = listOf(
            "סוף זמן ק״ש" to day.sofZmanShmaGra,
            "זריחה" to day.netzHachama,
            "תפילה" to day.sofZmanTefila,
            "עלות השחר" to day.alosHashachar,
            "צאת הכוכבים" to day.tzais,
            "שקיעה" to day.shkia,
            "מנחה קטנה" to day.minchaKetana,
            "מנחה גדולה" to day.minchaGedola
        )
        values.forEachIndexed { index, item ->
            val col = index % 4
            val row = index / 4
            val x1 = left + col * cw
            val y1 = top + row * ch
            drawRtl(canvas, item.first, box(card, sx, sy, x1 + 8f, y1 + 18f, x1 + cw - 8f, y1 + 112f), 33f * sx, true)
            drawLtr(canvas, ZmanimProvider.formatTime(item.second), box(card, sx, sy, x1 + 8f, y1 + 112f, x1 + cw - 8f, y1 + ch - 14f), 51f * sx, true)
        }
    }

    private fun drawFooter(canvas: Canvas, card: RectF, sx: Float, sy: Float, day: DayZmanim) {
        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        drawRtl(canvas, parsha, box(card, sx, sy, 300f, 1170f, 1038f, 1265f), 38f * sx, false)

        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val bottomText = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            day.roshChodeshInDays == 0 && !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            isFriday -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
            else -> null
        }
        if (!bottomText.isNullOrBlank()) {
            drawRtl(canvas, bottomText, box(card, sx, sy, 245f, 1320f, 1060f, 1418f), 34f * sx, false)
        }
    }

    private fun drawMechanicalClock(canvas: Canvas, card: RectF, sx: Float, sy: Float) {
        val cx = card.left + 325f * sx
        val cy = card.top + 310f * sy
        val r = 230f * sx

        val clip = Path().apply { addCircle(cx, cy, r * .48f, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(clip)

        val elapsedSeconds = System.currentTimeMillis() / 1000f
        val base = (elapsedSeconds * 2.4f) % 360f

        val g28 = gear28 ?: buildGearBitmap(28, 6).also { gear28 = it }
        val g20 = gear20 ?: buildGearBitmap(20, 5).also { gear20 = it }
        val g16 = gear16 ?: buildGearBitmap(16, 5).also { gear16 = it }
        val g14 = gear14 ?: buildGearBitmap(14, 5).also { gear14 = it }
        val g12 = gear12 ?: buildGearBitmap(12, 4).also { gear12 = it }

        drawGear(canvas, g28, cx - r * .03f, cy + r * .01f, r * .19f, base)
        drawGear(canvas, g20, cx + r * .25f, cy + r * .02f, r * .14f, -base * 28f / 20f + 8f)
        drawGear(canvas, g16, cx + r * .12f, cy + r * .25f, r * .115f, base * 28f / 16f + 14f)
        drawGear(canvas, g14, cx - r * .11f, cy + r * .27f, r * .102f, -base * 28f / 14f + 6f)
        drawGear(canvas, g20, cx - r * .27f, cy + r * .02f, r * .14f, -base * 28f / 20f + 20f)
        drawGear(canvas, g12, cx - r * .08f, cy - r * .25f, r * .09f, -base * 28f / 12f + 12f)
        canvas.restore()

        canvas.drawCircle(cx, cy, r * .46f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(18, 237, 202, 127)
        })

        drawClockHands(canvas, cx, cy, r)
    }

    private fun buildGearBitmap(teeth: Int, spokes: Int): Bitmap {
        val size = 320
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val c = size / 2f
        val tip = size * .455f
        val root = size * .385f
        val path = Path()

        for (i in 0 until teeth * 4) {
            val a = (2.0 * PI * i / (teeth * 4.0) - PI / 2.0).toFloat()
            val radius = if (i % 4 == 1 || i % 4 == 2) tip else root
            val x = c + radius * cos(a)
            val y = c + radius * sin(a)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                c - size * .16f, c - size * .18f, size * .56f,
                intArrayOf(Color.parseColor("#E8C16C"), Color.parseColor("#A76A2A"), Color.parseColor("#6A3E19"), Color.parseColor("#2B1A0F")),
                floatArrayOf(0f, .42f, .77f, 1f), Shader.TileMode.CLAMP
            )
        })
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4.5f
            color = Color.parseColor("#4B2D16")
        })
        canvas.drawCircle(c, c, size * .285f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4C2D18") })

        val spokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = size * .055f
            strokeCap = Paint.Cap.ROUND
            shader = LinearGradient(c - size * .25f, c - size * .25f, c + size * .25f, c + size * .25f,
                intArrayOf(Color.parseColor("#E0B45F"), Color.parseColor("#7B4920")), null, Shader.TileMode.CLAMP)
        }
        repeat(spokes) { index ->
            val a = (2.0 * PI * index / spokes - PI / 2.0).toFloat()
            canvas.drawLine(
                c + size * .085f * cos(a), c + size * .085f * sin(a),
                c + size * .255f * cos(a), c + size * .255f * sin(a), spokePaint
            )
        }
        canvas.drawCircle(c, c, size * .090f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B06D2B") })
        canvas.drawCircle(c, c, size * .050f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#24160D") })
        return bitmap
    }

    private fun drawGear(canvas: Canvas, bitmap: Bitmap, cx: Float, cy: Float, radius: Float, angle: Float) {
        canvas.save()
        canvas.rotate(angle, cx, cy)
        canvas.drawBitmap(bitmap, null, RectF(cx - radius, cy - radius, cx + radius, cy + radius), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        canvas.restore()
    }

    private fun drawClockHands(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val now = Calendar.getInstance()
        val seconds = now.get(Calendar.SECOND) + now.get(Calendar.MILLISECOND) / 1000f
        val minutes = now.get(Calendar.MINUTE) + seconds / 60f
        val hours = now.get(Calendar.HOUR) + minutes / 60f

        hand(canvas, cx, cy, r * .45f, hours * 30f, r * .058f, INK)
        hand(canvas, cx, cy, r * .67f, minutes * 6f, r * .036f, INK)
        hand(canvas, cx, cy, r * .78f, seconds * 6f, r * .010f, RED)
        canvas.drawCircle(cx, cy, r * .046f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT })
        canvas.drawCircle(cx, cy, r * .021f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BRONZE_DARK })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, len: Float, deg: Float, stroke: Float, color: Int) {
        val angle = Math.toRadians((deg - 90f).toDouble())
        val x = cx + len * cos(angle).toFloat()
        val y = cy + len * sin(angle).toFloat()
        canvas.drawLine(cx, cy, x, y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.argb(100, 0, 0, 0)
            strokeWidth = stroke * 1.22f
            strokeCap = Paint.Cap.ROUND
        })
        canvas.drawLine(cx, cy, x, y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        })
    }

    private fun drawRtl(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean) = drawText(canvas, text, rect, size, bold, true)
    private fun drawLtr(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean) = drawText(canvas, text, rect, size, bold, false)

    private fun drawText(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean, rtl: Boolean) {
        val typeface = if (bold) SERIF_BOLD else SERIF
        val flags = Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG
        val outline = TextPaint(flags).apply {
            textSize = size
            color = Color.parseColor("#3A2412")
            this.typeface = typeface
            style = Paint.Style.STROKE
            strokeWidth = (size * .036f).coerceAtLeast(.8f)
            strokeJoin = Paint.Join.ROUND
        }
        val fill = TextPaint(flags).apply {
            textSize = size
            color = GOLD_LIGHT
            this.typeface = typeface
            style = Paint.Style.FILL
        }
        drawLayout(canvas, text, rect, outline, rtl)
        drawLayout(canvas, text, rect, fill, rtl)
    }

    private fun drawLayout(canvas: Canvas, text: String, rect: RectF, paint: TextPaint, rtl: Boolean) {
        val width = rect.width().toInt().coerceAtLeast(1)
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .setMaxLines(2)
        if (rtl) builder.setTextDirection(TextDirectionHeuristics.RTL)
        val layout = builder.build()
        canvas.save()
        canvas.translate(rect.left, rect.top + (rect.height() - layout.height) / 2f)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun box(card: RectF, sx: Float, sy: Float, x1: Float, y1: Float, x2: Float, y2: Float) = RectF(
        card.left + x1 * sx,
        card.top + y1 * sy,
        card.left + x2 * sx,
        card.top + y2 * sy
    )
}
