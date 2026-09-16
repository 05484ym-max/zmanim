package com.zmanim.lockscreen.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
 * Image-first antique bronze renderer.
 *
 * A real bronze image provides the material/texture, while the clean v4 image is
 * used as a semi-transparent layout mask. Live text, clock hands and the
 * synchronized mechanical gear train are rendered on top.
 */
object HybridImageBronzeCard {
    private const val DESIGN_W = 1200f
    private const val DESIGN_H = 1620f

    private val GOLD = Color.parseColor("#E7C078")
    private val GOLD_LIGHT = Color.parseColor("#FFE3A1")
    private val GOLD_DARK = Color.parseColor("#6B431E")
    private val BRONZE_DARK = Color.parseColor("#2A170C")
    private val INK = Color.parseColor("#21150D")
    private val RED = Color.parseColor("#D72F22")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    @Volatile private var cachedLayout: Bitmap? = null
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
        val layout = cachedLayout?.takeIf { !it.isRecycled }
            ?: runCatching { BitmapFactory.decodeResource(context.resources, R.drawable.zmanim_bronze_static_v4) }
                .getOrNull()?.also { cachedLayout = it }

        if (bronze == null || layout == null) {
            ReferenceBronzeCard.draw(canvas, width, height, day, locationName)
            return
        }

        // 1) Real bronze image as the physical material of the card.
        canvas.drawBitmap(
            bronze,
            null,
            card,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
                alpha = 244
            }
        )

        // 2) Dark aged glaze. It gives the bronze depth without hiding the wallpaper completely.
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
                        Color.argb(26, 255, 221, 146),
                        Color.argb(78, 73, 39, 17),
                        Color.argb(108, 34, 18, 9)
                    ),
                    floatArrayOf(0f, .48f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        )

        // 3) The image-first structural layer: frame, clock dial, grid and ornaments.
        // Keeping it partially transparent lets the richer real-bronze texture show through.
        canvas.drawBitmap(
            layout,
            null,
            card,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
                alpha = 174
            }
        )

        drawAntiqueFrame(canvas, card)
        drawMechanicalClock(context, canvas, card, sx, sy)
        drawHeader(canvas, card, sx, sy, day, locationName)
        drawGrid(canvas, card, sx, sy, day)
        drawFooter(canvas, card, sx, sy, day)
    }

    private fun drawAntiqueFrame(canvas: Canvas, card: RectF) {
        val radius = card.width() * .035f
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = card.width() * .0085f
            shader = LinearGradient(
                card.left,
                card.top,
                card.right,
                card.bottom,
                intArrayOf(
                    Color.parseColor("#F5D995"),
                    Color.parseColor("#A66B2B"),
                    Color.parseColor("#5C3518"),
                    Color.parseColor("#E0B567")
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(card, radius, radius, frame)

        val inner = RectF(card).apply { inset(card.width() * .018f, card.width() * .018f) }
        canvas.drawRoundRect(
            inner,
            radius * .76f,
            radius * .76f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = card.width() * .0023f
                color = Color.argb(220, 243, 202, 122)
            }
        )
    }

    private fun drawHeader(
        canvas: Canvas,
        card: RectF,
        sx: Float,
        sy: Float,
        day: DayZmanim,
        locationName: String
    ) {
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
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.2f * sx
            color = Color.argb(230, 220, 166, 83)
        }
        canvas.drawRoundRect(grid, 28f * sx, 28f * sx, border)
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
            drawRtl(
                canvas,
                item.first,
                box(card, sx, sy, x1 + 8f, y1 + 18f, x1 + cw - 8f, y1 + 112f),
                33f * sx,
                true
            )
            drawLtr(
                canvas,
                ZmanimProvider.formatTime(item.second),
                box(card, sx, sy, x1 + 8f, y1 + 112f, x1 + cw - 8f, y1 + ch - 14f),
                51f * sx,
                true
            )
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

    private fun drawMechanicalClock(
        context: Context,
        canvas: Canvas,
        card: RectF,
        sx: Float,
        sy: Float
    ) {
        val cx = card.left + 325f * sx
        val cy = card.top + 310f * sy
        val r = 230f * sx

        // The movement is deliberately restricted to the center of the dial so
        // 12/3/6/9 remain crisp and visually dominant, like the reference image.
        val clip = Path().apply { addCircle(cx, cy, r * .53f, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(clip)

        canvas.drawCircle(
            cx,
            cy,
            r * .50f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx - r * .12f,
                    cy - r * .15f,
                    r * .55f,
                    intArrayOf(
                        Color.argb(52, 244, 208, 131),
                        Color.argb(105, 94, 54, 24),
                        Color.argb(145, 31, 19, 12)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
        )

        val elapsedSeconds = System.currentTimeMillis() / 1000f
        val base = (elapsedSeconds * 4f) % 360f // one revolution every 90 seconds

        val g28 = gear28 ?: buildGearBitmap(28, 6).also { gear28 = it }
        val g20 = gear20 ?: buildGearBitmap(20, 5).also { gear20 = it }
        val g16 = gear16 ?: buildGearBitmap(16, 5).also { gear16 = it }
        val g14 = gear14 ?: buildGearBitmap(14, 5).also { gear14 = it }
        val g12 = gear12 ?: buildGearBitmap(12, 4).also { gear12 = it }

        // One linked train. Adjacent gears reverse direction and their angular
        // speed follows the tooth-count ratio, so they read as a single mechanism.
        drawGear(canvas, g28, cx - r * .05f, cy + r * .03f, r * .205f, base)
        drawGear(canvas, g20, cx + r * .285f, cy + r * .015f, r * .148f, -base * 28f / 20f + 7f)
        drawGear(canvas, g16, cx + r * .15f, cy + r * .285f, r * .120f, base * 28f / 16f + 13f)
        drawGear(canvas, g14, cx - r * .12f, cy + r * .315f, r * .108f, -base * 28f / 14f + 5f)
        drawGear(canvas, g20, cx - r * .315f, cy + r * .04f, r * .145f, -base * 28f / 20f + 19f)
        drawGear(canvas, g12, cx - r * .10f, cy - r * .285f, r * .092f, -base * 28f / 12f + 11f)
        canvas.restore()

        // Thin parchment veil integrates the movement into the antique dial.
        canvas.drawCircle(
            cx,
            cy,
            r * .47f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(22, 236, 199, 122)
            }
        )

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

        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(170, 0, 0, 0)
            setShadowLayer(10f, 3f, 6f, Color.argb(190, 0, 0, 0))
        }
        canvas.drawPath(path, shadow)

        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                c - size * .16f,
                c - size * .18f,
                size * .56f,
                intArrayOf(
                    Color.parseColor("#F2D086"),
                    Color.parseColor("#B77931"),
                    Color.parseColor("#74441D"),
                    Color.parseColor("#332014")
                ),
                floatArrayOf(0f, .42f, .77f, 1f),
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, body)
        canvas.drawPath(
            path,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4.5f
                color = Color.parseColor("#4B2D16")
            }
        )

        // Dark movement plate under the spokes.
        canvas.drawCircle(c, c, size * .285f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#55331A") })

        val spokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = size * .055f
            strokeCap = Paint.Cap.ROUND
            shader = LinearGradient(
                c - size * .25f,
                c - size * .25f,
                c + size * .25f,
                c + size * .25f,
                intArrayOf(Color.parseColor("#E7BC69"), Color.parseColor("#8A5425")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        repeat(spokes) { index ->
            val a = (2.0 * PI * index / spokes - PI / 2.0).toFloat()
            canvas.drawLine(
                c + size * .085f * cos(a),
                c + size * .085f * sin(a),
                c + size * .255f * cos(a),
                c + size * .255f * sin(a),
                spokePaint
            )
        }

        canvas.drawCircle(c, c, size * .090f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B97731") })
        canvas.drawCircle(c, c, size * .050f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2A1A10") })
        canvas.drawCircle(
            c,
            c,
            size * .090f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = Color.parseColor("#F1CF82")
            }
        )

        // Deterministic patina marks keep the gears from looking like flat icons.
        val patina = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(58, 34, 21, 13) }
        repeat(12) { i ->
            val a = (2.0 * PI * i / 12.0 + teeth * .07).toFloat()
            val rr = size * (.20f + (i % 3) * .045f)
            canvas.drawCircle(c + rr * cos(a), c + rr * sin(a), 3.2f + (i % 2), patina)
        }
        return bitmap
    }

    private fun drawGear(
        canvas: Canvas,
        bitmap: Bitmap,
        cx: Float,
        cy: Float,
        radius: Float,
        angle: Float
    ) {
        canvas.save()
        canvas.rotate(angle, cx, cy)
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(cx - radius, cy - radius, cx + radius, cy + radius),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
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

        canvas.drawLine(
            cx,
            cy,
            x,
            y + stroke * .16f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.argb(115, 0, 0, 0)
                strokeWidth = stroke * 1.22f
                strokeCap = Paint.Cap.ROUND
            }
        )
        canvas.drawLine(
            cx,
            cy,
            x,
            y,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                strokeWidth = stroke
                strokeCap = Paint.Cap.ROUND
            }
        )
    }

    private fun drawRtl(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean) {
        drawText(canvas, text, rect, size, bold, true)
    }

    private fun drawLtr(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean) {
        drawText(canvas, text, rect, size, bold, false)
    }

    private fun drawText(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean, rtl: Boolean) {
        val typeface = if (bold) SERIF_BOLD else SERIF
        val flags = Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG

        val outline = TextPaint(flags).apply {
            textSize = size
            color = Color.parseColor("#3A2412")
            this.typeface = typeface
            style = Paint.Style.STROKE
            strokeWidth = (size * .045f).coerceAtLeast(.9f)
            strokeJoin = Paint.Join.ROUND
        }
        val fill = TextPaint(flags).apply {
            textSize = size
            color = GOLD_LIGHT
            this.typeface = typeface
            style = Paint.Style.FILL
        }
        val warm = TextPaint(flags).apply {
            textSize = size
            color = Color.argb(100, 142, 86, 34)
            this.typeface = typeface
            style = Paint.Style.STROKE
            strokeWidth = (size * .018f).coerceAtLeast(.6f)
        }

        drawLayout(canvas, text, rect, outline, rtl, 0f, size * .012f)
        drawLayout(canvas, text, rect, warm, rtl, 0f, size * .006f)
        drawLayout(canvas, text, rect, fill, rtl, 0f, 0f)
    }

    private fun drawLayout(
        canvas: Canvas,
        text: String,
        rect: RectF,
        paint: TextPaint,
        rtl: Boolean,
        dx: Float,
        dy: Float
    ) {
        val width = rect.width().toInt().coerceAtLeast(1)
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .setMaxLines(2)
        if (rtl) builder.setTextDirection(TextDirectionHeuristics.RTL)
        val layout = builder.build()
        canvas.save()
        canvas.translate(rect.left + dx, rect.top + (rect.height() - layout.height) / 2f + dy)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun box(
        card: RectF,
        sx: Float,
        sy: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ) = RectF(
        card.left + x1 * sx,
        card.top + y1 * sy,
        card.left + x2 * sx,
        card.top + y2 * sy
    )
}
