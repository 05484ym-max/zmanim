package com.zmanim.lockscreen.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.zmanim.lockscreen.R
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Image-first bronze renderer.
 *
 * The heavy visual work (bronze texture, frame, ornaments, clock face and grid)
 * lives in a pre-rendered image. Only live data and clock hands are drawn at
 * runtime. This keeps the card much sharper than rebuilding every decorative
 * layer on Canvas each frame.
 */
object HybridImageBronzeCard {
    private const val DESIGN_W = 1200f
    private const val DESIGN_H = 1620f

    private val GOLD = Color.parseColor("#F1D596")
    private val GOLD_BRIGHT = Color.parseColor("#FFE6AD")
    private val DARK = Color.parseColor("#2A160A")
    private val RED = Color.parseColor("#D92C20")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    @Volatile
    private var cachedBase: Bitmap? = null

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

        val bitmap = cachedBase?.takeIf { !it.isRecycled }
            ?: BitmapFactory.decodeResource(context.resources, R.drawable.zmanim_bronze_static_v4)
                .also { cachedBase = it }

        canvas.drawBitmap(
            bitmap,
            null,
            card,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        )

        // Header. No blur shadows: a narrow dark outline + bright fill stays crisp.
        drawRtl(canvas, "זמני היום", box(card, sx, sy, 610f, 105f, 1110f, 220f), 74f * sx, true)
        drawRtl(canvas, locationName, box(card, sx, sy, 610f, 235f, 995f, 330f), 43f * sx, false)
        drawRtl(canvas, day.hebrewDate, box(card, sx, sy, 560f, 342f, 1085f, 425f), 41f * sx, true)
        drawLtr(canvas, day.gregorianDate, box(card, sx, sy, 535f, 430f, 1085f, 505f), 31f * sx, false)

        drawGridText(canvas, card, sx, sy, day)
        drawFooter(canvas, card, sx, sy, day)
        drawClockHands(canvas, card, sx, sy)
    }

    private fun drawGridText(canvas: Canvas, card: RectF, sx: Float, sy: Float, day: DayZmanim) {
        val left = 90f
        val top = 600f
        val right = 1110f
        val bottom = 1115f
        val cw = (right - left) / 4f
        val ch = (bottom - top) / 2f

        val labels = listOf(
            "סוף זמן ק״ש" to day.sofZmanShmaGra,
            "זריחה" to day.netzHachama,
            "תפילה" to day.sofZmanTefila,
            "עלות השחר" to day.alosHashachar,
            "צאת הכוכבים" to day.tzais,
            "שקיעה" to day.shkia,
            "מנחה קטנה" to day.minchaKetana,
            "מנחה גדולה" to day.minchaGedola
        )

        labels.forEachIndexed { index, item ->
            val col = index % 4
            val row = index / 4
            val x1 = left + col * cw
            val y1 = top + row * ch
            val labelBox = box(card, sx, sy, x1 + 6f, y1 + 18f, x1 + cw - 6f, y1 + 108f)
            val valueBox = box(card, sx, sy, x1 + 6f, y1 + 108f, x1 + cw - 6f, y1 + ch - 14f)
            drawRtl(canvas, item.first, labelBox, 34f * sx, true)
            drawLtr(canvas, ZmanimProvider.formatTime(item.second), valueBox, 52f * sx, true)
        }
    }

    private fun drawFooter(canvas: Canvas, card: RectF, sx: Float, sy: Float, day: DayZmanim) {
        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        drawRtl(canvas, parsha, box(card, sx, sy, 300f, 1170f, 1035f, 1265f), 39f * sx, false)

        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val bottomText = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            day.roshChodeshInDays == 0 && !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            isFriday -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
            else -> null
        }
        if (!bottomText.isNullOrBlank()) {
            drawRtl(canvas, bottomText, box(card, sx, sy, 245f, 1320f, 1050f, 1415f), 35f * sx, false)
        }
    }

    private fun drawClockHands(canvas: Canvas, card: RectF, sx: Float, sy: Float) {
        val cx = card.left + 325f * sx
        val cy = card.top + 310f * sy
        val r = 230f * sx
        val now = Calendar.getInstance()
        val seconds = now.get(Calendar.SECOND) + now.get(Calendar.MILLISECOND) / 1000f
        val minutes = now.get(Calendar.MINUTE) + seconds / 60f
        val hours = now.get(Calendar.HOUR) + minutes / 60f

        hand(canvas, cx, cy, r * .45f, hours * 30f, r * .060f, Color.parseColor("#18110B"))
        hand(canvas, cx, cy, r * .66f, minutes * 6f, r * .038f, Color.parseColor("#18110B"))
        hand(canvas, cx, cy, r * .76f, seconds * 6f, r * .010f, RED)

        canvas.drawCircle(cx, cy, r * .040f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_BRIGHT })
        canvas.drawCircle(cx, cy, r * .018f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = DARK })
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, len: Float, deg: Float, stroke: Float, color: Int) {
        val angle = Math.toRadians((deg - 90f).toDouble())
        val x = cx + len * cos(angle).toFloat()
        val y = cy + len * sin(angle).toFloat()
        canvas.drawLine(cx, cy, x, y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        })
    }

    private fun drawRtl(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean) {
        drawText(canvas, text, rect, size, bold, true)
    }

    private fun drawLtr(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean) {
        drawText(canvas, text, rect, size, bold, false)
    }

    private fun drawText(canvas: Canvas, text: String, rect: RectF, size: Float, bold: Boolean, rtl: Boolean) {
        val typeface = if (bold) SERIF_BOLD else SERIF
        val flags = Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG

        // Thin outline replaces the old blurred multi-shadow treatment.
        val outline = TextPaint(flags).apply {
            textSize = size
            color = DARK
            this.typeface = typeface
            style = Paint.Style.STROKE
            strokeWidth = (size * .052f).coerceAtLeast(1f)
            strokeJoin = Paint.Join.ROUND
        }
        val fill = TextPaint(flags).apply {
            textSize = size
            color = GOLD
            this.typeface = typeface
            style = Paint.Style.FILL
        }
        val highlight = TextPaint(flags).apply {
            textSize = size
            color = Color.argb(150, 255, 235, 185)
            this.typeface = typeface
            style = Paint.Style.STROKE
            strokeWidth = (size * .012f).coerceAtLeast(.6f)
        }

        drawLayout(canvas, text, rect, outline, rtl, 0f, size * .018f)
        drawLayout(canvas, text, rect, highlight, rtl, -size * .010f, -size * .010f)
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
