package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.zmanim.lockscreen.zmanim.DateStripDay
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import java.util.Date
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Draws the full vintage zmanim card: a real backdrop blur (cheap downscale/upscale
 * trick, works back to minSdk 26) sampled from the scene behind it, an analog clock,
 * a 7-day date strip, a "next zman" box, an 8-cell zmanim grid (next one highlighted
 * gold), and a Shabbat candle-lighting / Rosh Chodesh row.
 */
object GlassCard {

    private val GOLD = Color.parseColor("#F0B95F")
    private val INK = Color.parseColor("#43301D")
    private val INK_SOFT = Color.parseColor("#5A3F24")

    private enum class IconKind { SUNRISE, SUN, MOON_STAR }
    private data class GridCell(val label: String, val time: Date?, val icon: IconKind)

    fun draw(canvas: Canvas, background: Bitmap, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val margin = w * 0.07f
        val top = h * 0.24f
        val rect = RectF(margin, top, w - margin, top + h * 0.54f)
        val corner = w * 0.07f

        drawBlurredBackdrop(canvas, background, rect, corner)
        drawTint(canvas, rect, corner)
        drawBorder(canvas, rect, corner)

        canvas.save()
        val clip = Path().apply { addRoundRect(rect, corner, corner, Path.Direction.CW) }
        canvas.clipPath(clip)
        drawContent(canvas, rect, day, locationName)
        canvas.restore()
    }

    // ---------- backdrop ----------

    private fun drawBlurredBackdrop(canvas: Canvas, background: Bitmap, rect: RectF, corner: Float) {
        val scale = 0.08f
        val smallW = max(1, (background.width * scale).toInt())
        val smallH = max(1, (background.height * scale).toInt())
        val small = Bitmap.createScaledBitmap(background, smallW, smallH, true)
        val blurred = Bitmap.createScaledBitmap(small, background.width, background.height, true)

        val path = Path().apply { addRoundRect(rect, corner, corner, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(blurred, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        canvas.restore()

        small.recycle()
        blurred.recycle()
    }

    private fun drawTint(canvas: Canvas, rect: RectF, corner: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                rect.left + rect.width() * 0.28f, rect.top,
                rect.width() * 1.3f,
                intArrayOf(Color.argb(115, 255, 250, 234), Color.argb(75, 224, 198, 148)),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, corner, corner, paint)
    }

    private fun drawBorder(canvas: Canvas, rect: RectF, corner: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(130, 255, 246, 222)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(rect, corner, corner, paint)
    }

    // ---------- layout ----------

    private fun drawContent(canvas: Canvas, rect: RectF, day: DayZmanim, locationName: String) {
        val w = rect.width()
        val h = rect.height()

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK; textSize = w * 0.072f; textAlign = Paint.Align.CENTER; isFakeBoldText = true
        }
        canvas.drawText("זמני היום", rect.centerX(), rect.top + h * 0.075f, titlePaint)

        val placePaint = Paint(titlePaint).apply {
            color = INK_SOFT; textSize = w * 0.038f; isFakeBoldText = false
        }
        canvas.drawText(locationName, rect.centerX(), rect.top + h * 0.115f, placePaint)

        val mainTop = rect.top + h * 0.16f
        val mainBottom = rect.top + h * 0.45f
        val mainHeight = mainBottom - mainTop

        val clockCx = rect.left + w * 0.22f
        val clockCy = mainTop + mainHeight * 0.5f
        val clockRadius = mainHeight * 0.46f
        drawClock(canvas, clockCx, clockCy, clockRadius)

        val rightColLeft = clockCx + clockRadius + w * 0.06f
        val rightColRight = rect.right - w * 0.06f

        val stripRect = RectF(rightColLeft, mainTop, rightColRight, mainTop + mainHeight * 0.4f)
        drawDateStrip(canvas, stripRect, day.dateStrip)

        val nextRect = RectF(rightColLeft, stripRect.bottom + mainHeight * 0.1f, rightColRight, mainBottom)
        val (nextLabel, nextTime) = nextUpcoming(day)
        drawNextBox(canvas, nextRect, nextLabel, nextTime)

        val divider1Y = mainBottom + h * 0.02f
        drawDivider(canvas, rect, divider1Y)

        val gridTop = divider1Y + h * 0.03f
        val gridBottom = gridTop + h * 0.28f
        val gridRect = RectF(rect.left + w * 0.04f, gridTop, rect.right - w * 0.04f, gridBottom)
        drawZGrid(canvas, gridRect, day)

        val divider2Y = gridBottom + h * 0.02f
        drawDivider(canvas, rect, divider2Y)

        val pairTop = divider2Y + h * 0.03f
        val pairBottom = rect.bottom - h * 0.03f
        val pairRect = RectF(rect.left + w * 0.05f, pairTop, rect.right - w * 0.05f, pairBottom)
        drawPairRow(canvas, pairRect, day)
    }

    private fun drawDivider(canvas: Canvas, rect: RectF, y: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(90, 169, 118, 31); strokeWidth = 2f }
        canvas.drawLine(rect.left + rect.width() * 0.05f, y, rect.right - rect.width() * 0.05f, y, paint)
    }

    private fun allEntries(day: DayZmanim): List<Pair<String, Date?>> = listOf(
        "עלות השחר" to day.alosHashachar,
        "הנץ החמה" to day.netzHachama,
        "סוף זמן ק\"ש" to day.sofZmanShmaGra,
        "חצות היום" to day.chatzos,
        "מנחה גדולה" to day.minchaGedola,
        "פלג המנחה" to day.plagHamincha,
        "שקיעה" to day.shkia,
        "צאת הכוכבים" to day.tzais
    )

    private fun nextUpcoming(day: DayZmanim): Pair<String, Date?> {
        val now = Calendar.getInstance().time
        val entries = allEntries(day)
        return entries.firstOrNull { it.second != null && it.second!!.after(now) } ?: entries.last()
    }

    // ---------- analog clock ----------

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(140, 255, 251, 240) }
        canvas.drawCircle(cx, cy, radius, facePaint)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD; style = Paint.Style.STROKE; strokeWidth = radius * 0.05f; alpha = 190
        }
        canvas.drawCircle(cx, cy, radius, ringPaint)

        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK_SOFT; textAlign = Paint.Align.CENTER; textSize = radius * 0.24f
        }
        for (n in 1..12) {
            val angle = Math.toRadians((n * 30 - 90).toDouble())
            val nx = cx + (radius * 0.74f) * cos(angle).toFloat()
            val ny = cy + (radius * 0.74f) * sin(angle).toFloat() + numPaint.textSize * 0.32f
            canvas.drawText(n.toString(), nx, ny, numPaint)
        }

        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR)
        val minute = now.get(Calendar.MINUTE)
        val minuteAngle = Math.toRadians((minute * 6 - 90).toDouble())
        val hourAngle = Math.toRadians((hour * 30 + minute * 0.5 - 90))

        val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK; strokeWidth = radius * 0.065f; strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(
            cx, cy,
            cx + radius * 0.44f * cos(hourAngle).toFloat(),
            cy + radius * 0.44f * sin(hourAngle).toFloat(),
            hourPaint
        )
        val minutePaint = Paint(hourPaint).apply { strokeWidth = radius * 0.045f; color = INK_SOFT }
        canvas.drawLine(
            cx, cy,
            cx + radius * 0.68f * cos(minuteAngle).toFloat(),
            cy + radius * 0.68f * sin(minuteAngle).toFloat(),
            minutePaint
        )
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD }
        canvas.drawCircle(cx, cy, radius * 0.055f, centerPaint)
    }

    // ---------- date strip ----------

    private fun drawDateStrip(canvas: Canvas, rect: RectF, days: List<DateStripDay>) {
        if (days.isEmpty()) return
        val cellW = rect.width() / days.size

        days.forEachIndexed { i, day ->
            val cx = rect.left + cellW * i + cellW / 2f
            if (day.isToday) {
                val badge = RectF(cx - cellW * 0.42f, rect.top, cx + cellW * 0.42f, rect.bottom)
                val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(70, 169, 118, 31) }
                canvas.drawRoundRect(badge, rect.height() * 0.3f, rect.height() * 0.3f, badgePaint)
                val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(160, 169, 118, 31); style = Paint.Style.STROKE; strokeWidth = 2f
                }
                canvas.drawRoundRect(badge, rect.height() * 0.3f, rect.height() * 0.3f, badgeBorder)
            }
            val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (day.isToday) INK else INK_SOFT
                textAlign = Paint.Align.CENTER
                textSize = rect.height() * 0.3f
                isFakeBoldText = day.isToday
            }
            val numPaint = Paint(letterPaint).apply {
                isFakeBoldText = true
                textSize = rect.height() * 0.4f
            }
            canvas.drawText(day.hebrewLetter, cx, rect.top + rect.height() * 0.4f, letterPaint)
            canvas.drawText(day.dayOfMonth.toString(), cx, rect.bottom - rect.height() * 0.1f, numPaint)
        }
    }

    // ---------- next box ----------

    private fun drawNextBox(canvas: Canvas, rect: RectF, label: String, time: Date?) {
        val corner = rect.height() * 0.28f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(60, 255, 251, 240) }
        canvas.drawRoundRect(rect, corner, corner, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 169, 118, 31); style = Paint.Style.STROKE; strokeWidth = 2f
        }
        canvas.drawRoundRect(rect, corner, corner, borderPaint)

        val iconR = rect.height() * 0.3f
        val iconCx = rect.right - rect.height() * 0.5f
        drawSunIcon(canvas, iconCx, rect.centerY(), iconR, GOLD)

        val textRight = iconCx - iconR - rect.height() * 0.2f
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK_SOFT; textSize = rect.height() * 0.22f; textAlign = Paint.Align.RIGHT
        }
        val bigPaint = Paint(smallPaint).apply { color = INK; isFakeBoldText = true; textSize = rect.height() * 0.3f }
        val timePaint = Paint(smallPaint).apply { color = INK; textSize = rect.height() * 0.26f }

        canvas.drawText("הזמן הבא", textRight, rect.top + rect.height() * 0.32f, smallPaint)
        canvas.drawText(label, textRight, rect.top + rect.height() * 0.62f, bigPaint)
        canvas.drawText(ZmanimProvider.formatTime(time), textRight, rect.bottom - rect.height() * 0.12f, timePaint)
    }

    // ---------- zmanim grid ----------

    private fun drawZGrid(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val cells = listOf(
            GridCell("עלות השחר", day.alosHashachar, IconKind.SUNRISE),
            GridCell("הנץ החמה", day.netzHachama, IconKind.SUNRISE),
            GridCell("סוף זמן ק\"ש", day.sofZmanShmaGra, IconKind.SUN),
            GridCell("חצות היום", day.chatzos, IconKind.SUN),
            GridCell("מנחה גדולה", day.minchaGedola, IconKind.SUN),
            GridCell("פלג המנחה", day.plagHamincha, IconKind.SUN),
            GridCell("שקיעה", day.shkia, IconKind.SUNRISE),
            GridCell("צאת הכוכבים", day.tzais, IconKind.MOON_STAR)
        )
        val now = Calendar.getInstance().time
        val nextIndex = cells.indexOfFirst { it.time != null && it.time.after(now) }

        val cols = 4
        val rows = 2
        val colW = rect.width() / cols
        val rowH = rect.height() / rows

        cells.forEachIndexed { index, cell ->
            val col = index % cols
            val row = index / cols
            val cx = rect.left + colW * col + colW / 2f
            val cellTop = rect.top + rowH * row
            val isNext = index == nextIndex
            val isPast = cell.time != null && cell.time.before(now)
            val alphaVal = if (isPast && !isNext) 115 else 255

            val iconR = rowH * 0.17f
            val iconCy = cellTop + iconR * 1.3f
            drawIcon(canvas, cell.icon, cx, iconCy, iconR, colorWithAlpha(if (isNext) GOLD else INK_SOFT, alphaVal))

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorWithAlpha(if (isNext) GOLD else INK_SOFT, alphaVal)
                textAlign = Paint.Align.CENTER
                textSize = colW * 0.135f
                isFakeBoldText = isNext
            }
            canvas.drawText(cell.label, cx, cellTop + rowH * 0.62f, labelPaint)

            val timePaint = Paint(labelPaint).apply {
                color = colorWithAlpha(if (isNext) GOLD else INK, alphaVal)
                textSize = colW * 0.16f
            }
            canvas.drawText(ZmanimProvider.formatTime(cell.time), cx, cellTop + rowH * 0.86f, timePaint)
        }
    }

    // ---------- Shabbat / Rosh Chodesh row ----------

    private fun drawPairRow(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val midX = rect.centerX()
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(90, 169, 118, 31); strokeWidth = 2f }
        canvas.drawLine(midX, rect.top, midX, rect.bottom, dividerPaint)

        // right half: Shabbat candle lighting
        val rightIconCx = rect.right - rect.width() * 0.13f
        drawCandleIcon(canvas, rightIconCx, rect.centerY(), rect.height() * 0.34f, GOLD)
        val rightTextRight = rightIconCx - rect.height() * 0.44f
        val labelPaintR = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK_SOFT; textSize = rect.height() * 0.24f; textAlign = Paint.Align.RIGHT
        }
        val timePaintR = Paint(labelPaintR).apply { color = INK; isFakeBoldText = true; textSize = rect.height() * 0.28f }
        canvas.drawText("כניסת שבת", rightTextRight, rect.top + rect.height() * 0.4f, labelPaintR)
        canvas.drawText(ZmanimProvider.formatTime(day.candleLighting), rightTextRight, rect.bottom - rect.height() * 0.1f, timePaintR)

        // left half: Rosh Chodesh
        val leftIconCx = rect.left + rect.width() * 0.13f
        drawCrescent(canvas, leftIconCx, rect.centerY(), rect.height() * 0.34f, GOLD)
        val leftTextLeft = leftIconCx + rect.height() * 0.44f
        val labelPaintL = Paint(labelPaintR).apply { textAlign = Paint.Align.LEFT }
        val timePaintL = Paint(timePaintR).apply { textAlign = Paint.Align.LEFT }
        val roshDaysText = when (day.roshChodeshInDays) {
            0 -> "היום"
            1 -> "מחר"
            null -> ""
            else -> "בעוד ${day.roshChodeshInDays} ימים"
        }
        canvas.drawText(day.roshChodeshLabel ?: "", leftTextLeft, rect.top + rect.height() * 0.4f, labelPaintL)
        canvas.drawText(roshDaysText, leftTextLeft, rect.bottom - rect.height() * 0.1f, timePaintL)
    }

    // ---------- icons ----------

    private fun drawIcon(canvas: Canvas, kind: IconKind, cx: Float, cy: Float, r: Float, color: Int) {
        when (kind) {
            IconKind.SUN -> drawSunIcon(canvas, cx, cy, r, color)
            IconKind.SUNRISE -> drawSunriseIcon(canvas, cx, cy, r, color)
            IconKind.MOON_STAR -> drawMoonStarIcon(canvas, cx, cy, r, color)
        }
    }

    private fun ringPaint(color: Int, r: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; style = Paint.Style.STROKE; strokeWidth = r * 0.17f; strokeCap = Paint.Cap.ROUND
    }

    private fun drawSunIcon(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = ringPaint(color, r)
        canvas.drawCircle(cx, cy, r * 0.48f, paint)
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45).toDouble())
            val x1 = cx + (r * 0.7f) * cos(angle).toFloat()
            val y1 = cy + (r * 0.7f) * sin(angle).toFloat()
            val x2 = cx + r * cos(angle).toFloat()
            val y2 = cy + r * sin(angle).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }

    private fun drawSunriseIcon(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = ringPaint(color, r)
        canvas.save()
        canvas.clipRect(cx - r, cy - r, cx + r, cy + r * 0.12f)
        canvas.drawCircle(cx, cy, r * 0.48f, paint)
        canvas.restore()
        canvas.drawLine(cx - r, cy + r * 0.12f, cx + r, cy + r * 0.12f, paint)
        for (deg in listOf(-60, -30, 0, 30, 60)) {
            val angle = Math.toRadians((deg - 90).toDouble())
            val x1 = cx + (r * 0.75f) * cos(angle).toFloat()
            val y1 = cy + (r * 0.75f) * sin(angle).toFloat()
            val x2 = cx + r * cos(angle).toFloat()
            val y2 = cy + r * sin(angle).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }

    private fun drawCrescent(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        val path = Path().apply { addCircle(cx, cy, r * 0.55f, Path.Direction.CW) }
        val cutout = Path().apply { addCircle(cx + r * 0.28f, cy - r * 0.15f, r * 0.5f, Path.Direction.CW) }
        path.op(cutout, Path.Op.DIFFERENCE)
        canvas.drawPath(path, paint)
    }

    private fun drawMoonStarIcon(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        drawCrescent(canvas, cx, cy, r, color)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawCircle(cx + r * 0.55f, cy + r * 0.35f, r * 0.12f, paint)
    }

    private fun drawCandleIcon(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        val bodyW = r * 0.5f
        canvas.drawRoundRect(
            RectF(cx - bodyW / 2f, cy - r * 0.1f, cx + bodyW / 2f, cy + r),
            bodyW * 0.25f, bodyW * 0.25f, paint
        )
        val flame = Path().apply {
            moveTo(cx, cy - r * 0.95f)
            quadTo(cx + r * 0.3f, cy - r * 0.4f, cx, cy - r * 0.05f)
            quadTo(cx - r * 0.3f, cy - r * 0.4f, cx, cy - r * 0.95f)
            close()
        }
        canvas.drawPath(flame, paint)
    }

    private fun colorWithAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
}
