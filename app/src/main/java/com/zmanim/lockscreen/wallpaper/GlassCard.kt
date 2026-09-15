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
import kotlin.math.cos
import kotlin.math.sin

/** Refined, highly transparent glass/brass card drawn fully in code. */
object GlassCard {
    private val GOLD = Color.parseColor("#C99A50")
    private val GOLD_LIGHT = Color.parseColor("#F0D08A")
    private val GOLD_DARK = Color.parseColor("#6E4B24")
    private val TEXT = Color.parseColor("#F6E3BC")
    private val RED = Color.parseColor("#C65C4A")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class ZCell(val label: String, val value: Date?)

    @Suppress("UNUSED_PARAMETER")
    fun draw(canvas: Canvas, plaque: Bitmap?, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()

        // Smaller and higher so Samsung's dock/navigation do not collide with the card.
        val cardW = w * .74f
        val cardH = h * .44f
        val top = h * .345f
        val rect = RectF((w - cardW) / 2f, top, (w + cardW) / 2f, top + cardH)

        drawGlass(canvas, rect, cardW * .042f)
        drawTop(canvas, rect, day, locationName)
        drawGrid(canvas, rect, day)
        drawFooter(canvas, rect, day)
    }

    private fun drawGlass(canvas: Canvas, rect: RectF, radius: Float) {
        // Only a tiny local shadow. Never darken the rest of the wallpaper.
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(8, 0, 0, 0)
            setShadowLayer(rect.width() * .007f, 0f, rect.width() * .004f, Color.argb(28, 0, 0, 0))
        }
        canvas.drawRoundRect(RectF(rect.left, rect.top + 2f, rect.right, rect.bottom + 2f), radius, radius, shadow)
        shadow.clearShadowLayer()

        // True glass feel: very low alpha, slight warm tone only inside the card.
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(
                    Color.argb(34, 255, 238, 205),
                    Color.argb(28, 92, 66, 43),
                    Color.argb(24, 25, 18, 13)
                ),
                floatArrayOf(0f, .48f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, fill)

        // Subtle top glass highlight.
        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, rect.top, 0f, rect.top + rect.height() * .28f,
                Color.argb(34, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, highlight)

        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .0055f
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(Color.argb(210, 240, 204, 130), Color.argb(190, 151, 104, 50), Color.argb(210, 240, 204, 130)),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, outer)

        val inset = rect.width() * .014f
        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * .0017f
            color = Color.argb(120, 240, 208, 145)
        }
        canvas.drawRoundRect(
            RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset),
            radius * .72f, radius * .72f, inner
        )

        drawCornerAccent(canvas, rect.left + inset * 1.45f, rect.top + inset * 1.45f, 1f, 1f, rect.width())
        drawCornerAccent(canvas, rect.right - inset * 1.45f, rect.top + inset * 1.45f, -1f, 1f, rect.width())
        drawCornerAccent(canvas, rect.left + inset * 1.45f, rect.bottom - inset * 1.45f, 1f, -1f, rect.width())
        drawCornerAccent(canvas, rect.right - inset * 1.45f, rect.bottom - inset * 1.45f, -1f, -1f, rect.width())
    }

    private fun drawCornerAccent(canvas: Canvas, x: Float, y: Float, sx: Float, sy: Float, width: Float) {
        val s = width * .045f
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(150, 222, 177, 96)
            style = Paint.Style.STROKE
            strokeWidth = width * .0023f
            strokeCap = Paint.Cap.ROUND
        }
        val path = Path().apply {
            moveTo(x, y + sy * s)
            cubicTo(x + sx * s * .18f, y + sy * s * .42f, x + sx * s * .50f, y + sy * s * .20f, x + sx * s, y)
        }
        canvas.drawPath(path, p)
    }

    private fun drawTop(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String) {
        val w = rect.width()
        val h = rect.height()
        val clockCx = rect.left + w * .27f
        val clockCy = rect.top + h * .205f
        val clockR = w * .165f
        drawClock(canvas, clockCx, clockCy, clockR)

        val right = RectF(rect.left + w * .50f, rect.top + h * .055f, rect.right - w * .055f, rect.top + h * .365f)
        rtl(canvas, "זמני היום", RectF(right.left, right.top, right.right, right.top + right.height() * .28f), w * .056f, true)
        rtl(canvas, locationName, RectF(right.left, right.top + right.height() * .30f, right.right, right.top + right.height() * .49f), w * .035f, false)
        rtl(canvas, day.hebrewDate, RectF(right.left, right.top + right.height() * .53f, right.right, right.top + right.height() * .73f), w * .032f, true)

        val p = textPaint(w * .024f, TEXT, false).apply {
            textAlign = Paint.Align.CENTER
            alpha = 225
        }
        canvas.drawText(day.gregorianDate, right.centerX(), right.top + right.height() * .92f, p)
    }

    private fun drawGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()
        val grid = RectF(rect.left + w * .050f, rect.top + h * .385f, rect.right - w * .050f, rect.top + h * .710f)
        val radius = w * .025f

        canvas.drawRoundRect(grid, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(16, 20, 15, 12) })
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * .0018f
            color = Color.argb(130, 218, 167, 88)
        }
        canvas.drawRoundRect(grid, radius, radius, border)

        val cw = grid.width() / 4f
        val ch = grid.height() / 2f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(72, 198, 151, 78)
            strokeWidth = w * .0009f
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
            rtl(canvas, cell.label, RectF(box.left + 3f, box.top + ch * .05f, box.right - 3f, box.top + ch * .43f), cw * .135f, true)
            val tp = textPaint(cw * .185f, GOLD_LIGHT, true).apply { textAlign = Paint.Align.CENTER }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - ch * .13f, tp)
        }
    }

    private fun drawFooter(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val w = rect.width()
        val h = rect.height()
        val top = rect.top + h * .735f
        val bottom = rect.bottom - h * .055f
        val mid = top + (bottom - top) * .50f

        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        rtl(canvas, parsha, RectF(rect.left + w * .10f, top, rect.right - w * .10f, mid), w * .031f, false)

        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val bottomText = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            day.roshChodeshInDays == 0 && !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            isFriday -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
            else -> null
        }
        if (!bottomText.isNullOrBlank()) {
            val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(70, 200, 154, 82)
                strokeWidth = w * .0009f
            }
            canvas.drawLine(rect.left + w * .16f, mid, rect.right - w * .16f, mid, divider)
            rtl(canvas, bottomText, RectF(rect.left + w * .10f, mid, rect.right - w * .10f, bottom), w * .028f, false)
        }
    }

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val rim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .07f
            color = Color.argb(190, 209, 159, 78)
        }
        canvas.drawCircle(cx, cy, r, rim)

        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .2f, cy - r * .25f, r,
                intArrayOf(Color.argb(54, 255, 246, 220), Color.argb(30, 162, 128, 86), Color.argb(18, 50, 38, 28)),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r * .91f, face)

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, 245, 225, 185)
            strokeCap = Paint.Cap.ROUND
        }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val out = r * .78f
            val inn = if (i % 5 == 0) r * .65f else r * .72f
            tick.strokeWidth = if (i % 5 == 0) r * .017f else r * .008f
            canvas.drawLine(cx + inn * cos(a).toFloat(), cy + inn * sin(a).toFloat(), cx + out * cos(a).toFloat(), cy + out * sin(a).toFloat(), tick)
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .42f, hour * 30f, r * .052f, Color.argb(235, 248, 234, 205))
        hand(canvas, cx, cy, r * .62f, min * 6f, r * .034f, Color.argb(235, 248, 234, 205))
        hand(canvas, cx, cy, r * .69f, sec * 6f, r * .010f, RED)
        canvas.drawCircle(cx, cy, r * .038f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT })
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

    private fun rtl(canvas: Canvas, text: String, box: RectF, size: Float, bold: Boolean) {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            color = GOLD_LIGHT
            typeface = if (bold) SERIF_BOLD else SERIF
            setShadowLayer(size * .035f, 0f, size * .020f, Color.argb(88, 0, 0, 0))
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
