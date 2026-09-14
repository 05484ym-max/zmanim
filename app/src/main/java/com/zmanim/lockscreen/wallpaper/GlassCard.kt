package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the approved reference plaque image (res/drawable-nodpi/plaque_bronze.jpg) as the
 * card - its bezel, corner filigree, hammered texture, bevel and static labels ("זמני היום",
 * the 8 zmanim category names, "פרשת השבוע:"/"הילולת היום:" prefixes) are exactly what was
 * approved, pixel for pixel. Only the parts that actually change day to day are drawn here:
 * the clock hands, location, both dates, the 8 time values, and the parsha/hilula names -
 * each patched over the image's own (frozen, wrong-by-tomorrow) baked-in placeholder first.
 *
 * The patch-rect fractions below were measured directly off the reference image (see the
 * conversation) rather than guessed, but this was never rendered on-device to confirm pixel
 * alignment - nudge the *Frac constants below if something sits slightly off.
 */
object GlassCard {

    private val GOLD = Color.parseColor("#E7C374")
    private val SHADOW = Color.parseColor("#140D05")
    private val HIGHLIGHT = Color.parseColor("#FFEFC4")

    // Colors sampled from the reference image itself, used to patch over its baked-in
    // placeholder numbers before drawing the real ones - keeps the seam close to invisible.
    private val PATCH_TITLE_BLOCK = Color.rgb(108, 78, 43)
    private val PATCH_GRID = Color.rgb(80, 58, 32)
    private val PATCH_PARSHA = Color.rgb(67, 54, 35)
    private val PATCH_HILULA = Color.rgb(63, 44, 21)
    private val PATCH_CLOCK_FACE = Color.parseColor("#E9CE8E")

    private const val FALLBACK_ASPECT = 826f / 1388f // plaque_bronze.jpg width/height

    private data class Cell(val time: Date?, val xFrac: Float)

    fun draw(canvas: Canvas, plaque: Bitmap?, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cardW = w * 0.86f
        val aspect = if (plaque != null) plaque.height.toFloat() / plaque.width.toFloat() else 1f / FALLBACK_ASPECT
        val cardH = (cardW * aspect).coerceAtMost(h * 0.74f)
        val left = (w - cardW) / 2f
        val top = h * 0.13f
        val rect = RectF(left, top, left + cardW, top + cardH)

        if (plaque != null) {
            canvas.drawBitmap(plaque, null, rect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        } else {
            drawFallbackPlaque(canvas, rect)
        }

        drawClock(canvas, rect, day)
        drawTitleBlockLines(canvas, rect, day, locationName)
        drawGridNumbers(canvas, rect, day)
        drawParshaLine(canvas, rect, day)
        drawHilulaLine(canvas, rect, day)
    }

    /** Only used if the drawable asset ever fails to decode, so the wallpaper still shows something. */
    private fun drawFallbackPlaque(canvas: Canvas, rect: RectF) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(Color.parseColor("#3C2A16"), Color.parseColor("#2A1D10"), Color.parseColor("#1C1209")),
                floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, rect.width() * 0.045f, rect.width() * 0.045f, paint)
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = rect.width() * 0.013f; color = Color.parseColor("#B99248")
        }
        canvas.drawRoundRect(rect, rect.width() * 0.045f, rect.width() * 0.045f, border)
    }

    // ---------- clock ----------

    private fun drawClock(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()
        val cx = rect.left + w * 0.28f
        val cy = rect.top + h * 0.16f
        val faceR = w * 0.205f

        canvas.drawCircle(cx, cy, faceR * 0.86f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PATCH_CLOCK_FACE })

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(170, 60, 40, 12); strokeWidth = faceR * 0.02f }
        for (i in 0 until 12) {
            val a = Math.toRadians((i * 30 - 90).toDouble())
            val outerP = faceR * 0.72f
            val innerP = if (i % 3 == 0) faceR * 0.58f else faceR * 0.66f
            canvas.drawLine(
                cx + innerP * cos(a).toFloat(), cy + innerP * sin(a).toFloat(),
                cx + outerP * cos(a).toFloat(), cy + outerP * sin(a).toFloat(), tick
            )
        }
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3B2818"); textAlign = Paint.Align.CENTER
            textSize = faceR * 0.26f; isFakeBoldText = true
        }
        listOf(12 to 0, 3 to 90, 6 to 180, 9 to 270).forEach { (num, deg) ->
            val a = Math.toRadians((deg - 90).toDouble())
            val nx = cx + faceR * 0.5f * cos(a).toFloat()
            val ny = cy + faceR * 0.5f * sin(a).toFloat() + numPaint.textSize * 0.32f
            canvas.drawText(num.toString(), nx, ny, numPaint)
        }

        val now = Calendar.getInstance()
        val second = now.get(Calendar.SECOND)
        val minute = now.get(Calendar.MINUTE) + second / 60f
        val hour = now.get(Calendar.HOUR) + minute / 60f

        drawHand(canvas, cx, cy, faceR * 0.42f, hour * 30f, faceR * 0.05f, Color.parseColor("#241708"))
        drawHand(canvas, cx, cy, faceR * 0.60f, minute * 6f, faceR * 0.035f, Color.parseColor("#241708"))
        drawHand(canvas, cx, cy, faceR * 0.65f, second * 6f, faceR * 0.012f, Color.parseColor("#B23A2B"))

        canvas.drawCircle(cx, cy, faceR * 0.045f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#241708") })
        canvas.drawCircle(cx, cy, faceR * 0.02f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B23A2B") })
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, length: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90).toDouble())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(cx, cy, cx + length * cos(a).toFloat(), cy + length * sin(a).toFloat(), paint)
    }

    // ---------- title block: location + two date lines ----------

    private fun drawTitleBlockLines(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String) {
        val w = rect.width()
        val h = rect.height()
        val textCx = rect.left + w * 0.725f

        patchRect(canvas, rect, 0.50f, 0.97f, 0.145f, 0.055f, PATCH_TITLE_BLOCK)
        drawPinIcon(canvas, textCx - w * 0.11f, rect.top + h * 0.148f, w * 0.016f, GOLD)
        embossText(canvas, locationName, textCx + w * 0.02f, rect.top + h * 0.158f, w * 0.038f, bold = false)

        patchRect(canvas, rect, 0.50f, 0.97f, 0.19f, 0.06f, PATCH_TITLE_BLOCK)
        embossText(canvas, day.hebrewDate, textCx, rect.top + h * 0.205f, w * 0.046f, bold = true)

        patchRect(canvas, rect, 0.50f, 0.97f, 0.235f, 0.05f, PATCH_TITLE_BLOCK)
        embossText(canvas, day.gregorianDate, textCx, rect.top + h * 0.245f, w * 0.030f, bold = false)
    }

    // ---------- zmanim grid: numbers only, labels are baked into the image ----------

    private fun drawGridNumbers(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val xFracs = floatArrayOf(0.17f, 0.39f, 0.61f, 0.83f)
        val topRow = listOf(
            Cell(day.sofZmanShmaGra, xFracs[0]),
            Cell(day.netzHachama, xFracs[1]),
            Cell(day.sofZmanTefila, xFracs[2]),
            Cell(day.alosHashachar, xFracs[3])
        )
        val bottomRow = listOf(
            Cell(day.tzais, xFracs[0]),
            Cell(day.shkia, xFracs[1]),
            Cell(day.minchaKetana, xFracs[2]),
            Cell(day.minchaGedola, xFracs[3])
        )
        drawRow(canvas, rect, topRow, yFrac = 0.42f)
        drawRow(canvas, rect, bottomRow, yFrac = 0.575f)
    }

    private fun drawRow(canvas: Canvas, rect: RectF, cells: List<Cell>, yFrac: Float) {
        cells.forEach { cell ->
            patchRect(canvas, rect, cell.xFrac - 0.10f, cell.xFrac + 0.10f, yFrac, 0.055f, PATCH_GRID)
            embossText(
                canvas, ZmanimProvider.formatTime(cell.time),
                rect.left + rect.width() * cell.xFrac, rect.top + rect.height() * (yFrac + 0.014f),
                rect.width() * 0.042f, bold = true
            )
        }
    }

    // ---------- parsha / hilula lines (redrawn whole, including their label prefix) ----------

    private fun drawParshaLine(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()
        patchRect(canvas, rect, 0.08f, 0.92f, 0.695f, 0.06f, PATCH_PARSHA)
        val text = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        val size = w * 0.037f
        drawBookIcon(canvas, rect.centerX() - textHalfWidth(text, size) - w * 0.05f, rect.top + h * 0.685f, w * 0.022f, GOLD)
        embossText(canvas, text, rect.centerX(), rect.top + h * 0.70f, size, bold = false)
    }

    private fun drawHilulaLine(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val text = day.hilulaLabel?.let { "הילולת היום: $it" }
            ?: day.roshChodeshLabel?.let { label ->
                when (day.roshChodeshInDays) {
                    0 -> "$label · היום"
                    1 -> "$label · מחר"
                    else -> "$label · בעוד ${day.roshChodeshInDays} ימים"
                }
            } ?: return

        val w = rect.width()
        val h = rect.height()
        patchRect(canvas, rect, 0.08f, 0.92f, 0.83f, 0.06f, PATCH_HILULA)
        val size = w * 0.034f
        drawCandlesIcon(canvas, rect.centerX() - textHalfWidth(text, size) - w * 0.055f, rect.top + h * 0.82f, w * 0.02f, GOLD)
        embossText(canvas, text, rect.centerX(), rect.top + h * 0.835f, size, bold = false)
    }

    private fun textHalfWidth(text: String, size: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size }
        return paint.measureText(text) / 2f
    }

    // ---------- patch + icons + emboss-text helpers ----------

    private fun patchRect(canvas: Canvas, rect: RectF, xFrac0: Float, xFrac1: Float, yFrac: Float, heightFrac: Float, color: Int) {
        val w = rect.width()
        val h = rect.height()
        val top = rect.top + h * yFrac - h * heightFrac / 2f
        val bottom = rect.top + h * yFrac + h * heightFrac / 2f
        val patch = RectF(rect.left + w * xFrac0, top, rect.left + w * xFrac1, bottom)
        canvas.drawRoundRect(patch, h * 0.01f, h * 0.01f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    }

    private fun drawPinIcon(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; style = Paint.Style.STROKE
            strokeWidth = r * 0.32f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        }
        val path = android.graphics.Path().apply {
            moveTo(cx, cy + r * 1.3f)
            cubicTo(cx - r * 1.3f, cy + r * 0.15f, cx - r * 0.95f, cy - r * 1.3f, cx, cy - r * 1.3f)
            cubicTo(cx + r * 0.95f, cy - r * 1.3f, cx + r * 1.3f, cy + r * 0.15f, cx, cy + r * 1.3f)
            close()
        }
        canvas.drawPath(path, paint)
        canvas.drawCircle(cx, cy - r * 0.35f, r * 0.4f, paint)
    }

    private fun drawBookIcon(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; style = Paint.Style.STROKE
            strokeWidth = r * 0.22f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(
            android.graphics.Path().apply {
                moveTo(cx, cy - r * 0.5f); lineTo(cx - r, cy - r * 0.75f); lineTo(cx - r, cy + r * 0.55f); lineTo(cx, cy + r * 0.35f)
            },
            paint
        )
        canvas.drawPath(
            android.graphics.Path().apply {
                moveTo(cx, cy - r * 0.5f); lineTo(cx + r, cy - r * 0.75f); lineTo(cx + r, cy + r * 0.55f); lineTo(cx, cy + r * 0.35f)
            },
            paint
        )
    }

    private fun drawCandlesIcon(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        drawOneCandle(canvas, cx - r * 0.55f, cy, r, color)
        drawOneCandle(canvas, cx + r * 0.55f, cy, r, color)
    }

    private fun drawOneCandle(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawRoundRect(RectF(cx - r * 0.16f, cy - r * 0.1f, cx + r * 0.16f, cy + r), r * 0.08f, r * 0.08f, paint)
        val flame = android.graphics.Path().apply {
            moveTo(cx, cy - r * 0.8f)
            quadTo(cx + r * 0.24f, cy - r * 0.32f, cx, cy - r * 0.05f)
            quadTo(cx - r * 0.24f, cy - r * 0.32f, cx, cy - r * 0.8f)
            close()
        }
        canvas.drawPath(flame, paint)
    }

    private fun embossText(canvas: Canvas, text: String, x: Float, y: Float, size: Float, bold: Boolean) {
        val base = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = size; isFakeBoldText = bold }
        val shadow = Paint(base).apply { color = SHADOW; alpha = 160 }
        val highlight = Paint(base).apply { color = HIGHLIGHT; alpha = 130 }
        val main = Paint(base).apply { color = GOLD }
        canvas.drawText(text, x + 1.1f, y + 1.3f, shadow)
        canvas.drawText(text, x - 0.6f, y - 0.6f, highlight)
        canvas.drawText(text, x, y, main)
    }
}
