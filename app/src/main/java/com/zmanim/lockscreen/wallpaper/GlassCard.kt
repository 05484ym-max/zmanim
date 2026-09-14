package com.zmanim.lockscreen.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Engraved brass/bronze plaque card - matches the approved reference image: analog clock
 * top-left, title/location/date block top-right, an 8-cell zmanim grid, a weekly-parsha
 * line and a daily-hilula line. Fully opaque (no backdrop blur needed, unlike the earlier
 * glass-card iterations), which is also why a 1-second redraw for the live second hand is
 * cheap - see ZmanimWallpaperService for the day-data caching that makes that affordable.
 */
object GlassCard {

    private val PLAQUE_LIGHT = Color.parseColor("#3C2A16")
    private val PLAQUE_BASE = Color.parseColor("#2A1D10")
    private val PLAQUE_DARK = Color.parseColor("#1C1209")
    private val GOLD = Color.parseColor("#E7C374")
    private val GOLD_DIM = Color.parseColor("#B99248")
    private val SHADOW = Color.parseColor("#140D05")
    private val HIGHLIGHT = Color.parseColor("#FFEFC4")

    private data class Cell(val label: String, val time: Date?)

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cardW = w * 0.86f
        val cardH = (cardW * 1.28f).coerceAtMost(h * 0.62f)
        val left = (w - cardW) / 2f
        val top = h * 0.16f
        val rect = RectF(left, top, left + cardW, top + cardH)
        val corner = w * 0.045f

        drawShadowDrop(canvas, rect, corner)
        drawPlaqueBase(canvas, rect, corner)
        drawHammeredTexture(canvas, rect)
        drawBevelBorder(canvas, rect, corner)
        drawCorners(canvas, rect)
        drawContent(canvas, rect, day, locationName)
    }

    // ---------- plaque base ----------

    private fun drawShadowDrop(canvas: Canvas, rect: RectF, corner: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(110, 10, 6, 2) }
        canvas.drawRoundRect(RectF(rect.left + 4f, rect.top + 14f, rect.right + 4f, rect.bottom + 14f), corner, corner, paint)
    }

    private fun drawPlaqueBase(canvas: Canvas, rect: RectF, corner: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(PLAQUE_LIGHT, PLAQUE_BASE, PLAQUE_DARK),
                floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, corner, corner, paint)
    }

    private fun drawHammeredTexture(canvas: Canvas, rect: RectF) {
        val rnd = Random(11)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.save()
        val clip = Path().apply { addRoundRect(rect, rect.width() * 0.045f, rect.width() * 0.045f, Path.Direction.CW) }
        canvas.clipPath(clip)
        repeat(160) {
            val x = rect.left + rnd.nextFloat() * rect.width()
            val y = rect.top + rnd.nextFloat() * rect.height()
            val r = rect.width() * (0.008f + rnd.nextFloat() * 0.014f)
            paint.color = if (rnd.nextBoolean()) Color.argb(16, 255, 220, 160) else Color.argb(24, 10, 6, 2)
            canvas.drawCircle(x, y, r, paint)
        }
        canvas.restore()
    }

    private fun drawBevelBorder(canvas: Canvas, rect: RectF, corner: Float) {
        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * 0.013f
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(Color.parseColor("#F3D48A"), Color.parseColor("#8A6425"), Color.parseColor("#F3D48A")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, corner, corner, outer)

        val inset = rect.width() * 0.02f
        val innerRect = RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * 0.004f
            color = Color.argb(160, 60, 40, 14)
        }
        canvas.drawRoundRect(innerRect, corner * 0.85f, corner * 0.85f, inner)
    }

    private fun drawCorners(canvas: Canvas, rect: RectF) {
        val size = rect.width() * 0.11f
        val inset = rect.width() * 0.035f
        drawCornerFlourish(canvas, rect.left + inset, rect.top + inset, size, mirrorX = false, mirrorY = false)
        drawCornerFlourish(canvas, rect.right - inset, rect.top + inset, size, mirrorX = true, mirrorY = false)
        drawCornerFlourish(canvas, rect.left + inset, rect.bottom - inset, size, mirrorX = false, mirrorY = true)
        drawCornerFlourish(canvas, rect.right - inset, rect.bottom - inset, size, mirrorX = true, mirrorY = true)
    }

    private fun drawCornerFlourish(canvas: Canvas, cx: Float, cy: Float, size: Float, mirrorX: Boolean, mirrorY: Boolean) {
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(if (mirrorX) -1f else 1f, if (mirrorY) -1f else 1f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD_DIM; style = Paint.Style.STROKE
            strokeWidth = size * 0.07f; strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(Path().apply { moveTo(0f, size); quadTo(0f, 0f, size, 0f) }, paint)
        canvas.drawPath(
            Path().apply { moveTo(size * 0.18f, size * 0.82f); quadTo(size * 0.18f, size * 0.18f, size * 0.82f, size * 0.18f) },
            paint
        )
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_DIM }
        canvas.drawCircle(size * 0.18f, size * 0.82f, size * 0.05f, dot)
        canvas.drawCircle(size * 0.82f, size * 0.18f, size * 0.05f, dot)
        canvas.restore()
    }

    // ---------- content ----------

    private fun drawContent(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String) {
        val w = rect.width()
        val h = rect.height()

        val clockCx = rect.left + w * 0.255f
        val clockCy = rect.top + h * 0.165f
        val clockR = w * 0.195f
        drawClock(canvas, clockCx, clockCy, clockR)

        val textCx = rect.left + w * 0.725f
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER; textSize = w * 0.078f; isFakeBoldText = true
        }
        embossText(canvas, "זמני היום", textCx, rect.top + h * 0.075f, titlePaint)

        drawPinIcon(canvas, textCx - w * 0.10f, rect.top + h * 0.125f, w * 0.018f, GOLD)
        val placePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = w * 0.042f }
        embossText(canvas, locationName, textCx + w * 0.02f, rect.top + h * 0.135f, placePaint)

        val hebPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER; textSize = w * 0.05f; isFakeBoldText = true
        }
        embossText(canvas, day.hebrewDate, textCx, rect.top + h * 0.205f, hebPaint)

        val gregPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = w * 0.032f }
        embossText(canvas, day.gregorianDate, textCx, rect.top + h * 0.245f, gregPaint)

        val divider1Y = rect.top + h * 0.315f
        drawDivider(canvas, rect, divider1Y)

        val gridRect = RectF(rect.left + w * 0.05f, divider1Y + h * 0.02f, rect.right - w * 0.05f, rect.top + h * 0.615f)
        drawGrid(canvas, gridRect, day)

        val divider2Y = rect.top + h * 0.635f
        drawDivider(canvas, rect, divider2Y)

        val parshaText = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        val parshaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = w * 0.037f }
        drawBookIcon(canvas, rect.centerX() - parshaPaint.measureText(parshaText) / 2f - w * 0.05f, rect.top + h * 0.685f, w * 0.022f, GOLD)
        embossText(canvas, parshaText, rect.centerX(), rect.top + h * 0.695f, parshaPaint)

        val divider3Y = rect.top + h * 0.735f
        drawDivider(canvas, rect, divider3Y, thin = true)

        val hilulaText = day.hilulaLabel?.let { "הילולת היום: $it" }
            ?: day.roshChodeshLabel?.let { label ->
                when (day.roshChodeshInDays) {
                    0 -> "$label · היום"
                    1 -> "$label · מחר"
                    else -> "$label · בעוד ${day.roshChodeshInDays} ימים"
                }
            }
        if (hilulaText != null) {
            val hilulaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = w * 0.034f }
            drawCandlesIcon(
                canvas, rect.centerX() - hilulaPaint.measureText(hilulaText) / 2f - w * 0.055f,
                rect.top + h * 0.785f, w * 0.02f, GOLD
            )
            embossText(canvas, hilulaText, rect.centerX(), rect.top + h * 0.795f, hilulaPaint)
        }
    }

    private fun drawDivider(canvas: Canvas, rect: RectF, y: Float, thin: Boolean = false) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (thin) Color.argb(90, 185, 146, 72) else Color.argb(140, 185, 146, 72)
            strokeWidth = if (thin) 1f else 1.6f
        }
        canvas.drawLine(rect.left + rect.width() * 0.06f, y, rect.right - rect.width() * 0.06f, y, paint)
    }

    // ---------- grid ----------

    private fun drawGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        // Exact left-to-right cell order from the approved reference image.
        val topRow = listOf(
            Cell("סוף זמן ש\"מ", day.sofZmanShmaGra),
            Cell("זריחה", day.netzHachama),
            Cell("תפילין", day.sofZmanTefila),
            Cell("עלות השחר", day.alosHashachar)
        )
        val bottomRow = listOf(
            Cell("צאת הכוכבים", day.tzais),
            Cell("שקיעה", day.shkia),
            Cell("מנחה קטנה", day.minchaKetana),
            Cell("מנחה גדולה", day.minchaGedola)
        )
        val cols = 4
        val colW = rect.width() / cols
        val rowH = rect.height() / 2f

        val cellBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(120, 20, 13, 6) }
        canvas.drawRect(rect, cellBg)

        val gridLine = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(70, 185, 146, 72); strokeWidth = 1f }
        for (c in 1 until cols) {
            val x = rect.left + colW * c
            canvas.drawLine(x, rect.top, x, rect.bottom, gridLine)
        }
        canvas.drawLine(rect.left, rect.top + rowH, rect.right, rect.top + rowH, gridLine)
        canvas.drawRect(
            rect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.argb(90, 185, 146, 72); strokeWidth = 1.4f }
        )

        fun drawRow(cells: List<Cell>, rowTop: Float) {
            cells.forEachIndexed { col, cell ->
                val cx = rect.left + colW * col + colW / 2f
                val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; textSize = colW * 0.155f }
                embossText(canvas, cell.label, cx, rowTop + rowH * 0.36f, labelPaint)
                val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textAlign = Paint.Align.CENTER; textSize = colW * 0.20f; isFakeBoldText = true
                }
                embossText(canvas, ZmanimProvider.formatTime(cell.time), cx, rowTop + rowH * 0.72f, timePaint)
            }
        }
        drawRow(topRow, rect.top)
        drawRow(bottomRow, rect.top + rowH)
    }

    // ---------- clock ----------

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = r * 0.16f
            shader = LinearGradient(
                cx - r, cy - r, cx + r, cy + r,
                intArrayOf(Color.parseColor("#F2D98A"), Color.parseColor("#9C7326"), Color.parseColor("#F2D98A")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r * 0.9f, ring)

        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E9CE8E") }
        canvas.drawCircle(cx, cy, r * 0.78f, face)
        val faceShade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * 0.2f, cy - r * 0.2f, r * 1.1f,
                Color.argb(70, 255, 245, 210), Color.argb(50, 120, 85, 30), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r * 0.78f, faceShade)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(170, 60, 40, 12); strokeWidth = r * 0.02f }
        for (i in 0 until 12) {
            val a = Math.toRadians((i * 30 - 90).toDouble())
            val outerP = r * 0.72f
            val innerP = if (i % 3 == 0) r * 0.58f else r * 0.66f
            canvas.drawLine(
                cx + innerP * cos(a).toFloat(), cy + innerP * sin(a).toFloat(),
                cx + outerP * cos(a).toFloat(), cy + outerP * sin(a).toFloat(), tick
            )
        }
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3B2818"); textAlign = Paint.Align.CENTER
            textSize = r * 0.26f; isFakeBoldText = true
        }
        listOf(12 to 0, 3 to 90, 6 to 180, 9 to 270).forEach { (num, deg) ->
            val a = Math.toRadians((deg - 90).toDouble())
            val nx = cx + r * 0.5f * cos(a).toFloat()
            val ny = cy + r * 0.5f * sin(a).toFloat() + numPaint.textSize * 0.32f
            canvas.drawText(num.toString(), nx, ny, numPaint)
        }

        val now = Calendar.getInstance()
        val second = now.get(Calendar.SECOND)
        val minute = now.get(Calendar.MINUTE) + second / 60f
        val hour = now.get(Calendar.HOUR) + minute / 60f

        drawHand(canvas, cx, cy, r * 0.42f, hour * 30f, r * 0.05f, Color.parseColor("#241708"))
        drawHand(canvas, cx, cy, r * 0.60f, minute * 6f, r * 0.035f, Color.parseColor("#241708"))
        drawHand(canvas, cx, cy, r * 0.65f, second * 6f, r * 0.012f, Color.parseColor("#B23A2B"))

        canvas.drawCircle(cx, cy, r * 0.045f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#241708") })
        canvas.drawCircle(cx, cy, r * 0.02f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B23A2B") })
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, length: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90).toDouble())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(cx, cy, cx + length * cos(a).toFloat(), cy + length * sin(a).toFloat(), paint)
    }

    // ---------- small icons ----------

    private fun drawPinIcon(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; style = Paint.Style.STROKE
            strokeWidth = r * 0.32f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        }
        val path = Path().apply {
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
            Path().apply {
                moveTo(cx, cy - r * 0.5f); lineTo(cx - r, cy - r * 0.75f); lineTo(cx - r, cy + r * 0.55f); lineTo(cx, cy + r * 0.35f)
            },
            paint
        )
        canvas.drawPath(
            Path().apply {
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
        val flame = Path().apply {
            moveTo(cx, cy - r * 0.8f)
            quadTo(cx + r * 0.24f, cy - r * 0.32f, cx, cy - r * 0.05f)
            quadTo(cx - r * 0.24f, cy - r * 0.32f, cx, cy - r * 0.8f)
            close()
        }
        canvas.drawPath(flame, paint)
    }

    // ---------- engraved-text helper ----------

    private fun embossText(canvas: Canvas, text: String, x: Float, y: Float, base: Paint) {
        val shadow = Paint(base).apply { color = SHADOW; alpha = 160 }
        val highlight = Paint(base).apply { color = HIGHLIGHT; alpha = 130 }
        val main = Paint(base).apply { color = GOLD }
        canvas.drawText(text, x + 1.1f, y + 1.3f, shadow)
        canvas.drawText(text, x - 0.6f, y - 0.6f, highlight)
        canvas.drawText(text, x, y, main)
    }
}
