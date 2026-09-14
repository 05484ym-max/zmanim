package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import java.util.Date
import kotlin.math.max

/**
 * Draws the translucent "glass" zmanim card: a real backdrop blur (cheap downscale/
 * upscale trick, not a system blur API - keeps this working back to minSdk 26) sampled
 * from the scene bitmap behind it, a warm tint, and the zmanim rows with the next
 * upcoming zman picked out in gold.
 */
object GlassCard {

    private val GOLD = Color.parseColor("#F0B95F")
    private val INK = Color.parseColor("#43301D")
    private val INK_SOFT = Color.parseColor("#5A3F24")

    fun draw(canvas: Canvas, background: Bitmap, width: Int, height: Int, day: DayZmanim) {
        val w = width.toFloat()
        val h = height.toFloat()
        val margin = w * 0.07f
        val top = h * 0.30f
        val rect = RectF(margin, top, w - margin, top + h * 0.42f)
        val corner = w * 0.07f

        drawBlurredBackdrop(canvas, background, rect, corner)
        drawTint(canvas, rect, corner)
        drawBorder(canvas, rect, corner)

        canvas.save()
        val clip = Path().apply { addRoundRect(rect, corner, corner, Path.Direction.CW) }
        canvas.clipPath(clip)
        drawContent(canvas, rect, day)
        canvas.restore()
    }

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

    private fun drawContent(canvas: Canvas, rect: RectF, day: DayZmanim) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = INK
            textSize = rect.width() * 0.075f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val titleY = rect.top + rect.height() * 0.12f
        canvas.drawText("זמני היום", rect.centerX(), titleY, titlePaint)

        val datePaint = Paint(titlePaint).apply {
            textSize = rect.width() * 0.04f
            isFakeBoldText = false
            color = INK_SOFT
        }
        canvas.drawText(day.hebrewDate, rect.centerX(), titleY + rect.height() * 0.06f, datePaint)

        val rows = listOf(
            "עלות השחר" to day.alosHashachar,
            "הנץ החמה" to day.netzHachama,
            "סוף זמן ק\"ש (גר\"א)" to day.sofZmanShmaGra,
            "חצות היום" to day.chatzos,
            "מנחה גדולה" to day.minchaGedola,
            "פלג המנחה" to day.plagHamincha,
            "שקיעה" to day.shkia,
            "צאת הכוכבים" to day.tzais
        )

        val now = Calendar.getInstance().time
        val nextIndex = rows.indexOfFirst { (_, time) -> time != null && time.after(now) }

        val listTop = rect.top + rect.height() * 0.28f
        val listBottom = rect.bottom - rect.height() * 0.05f
        val rowHeight = (listBottom - listTop) / rows.size

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = rect.width() * 0.045f
            textAlign = Paint.Align.RIGHT
        }
        val timePaint = Paint(labelPaint).apply { textAlign = Paint.Align.LEFT }

        rows.forEachIndexed { index, (label, time) ->
            val y = listTop + rowHeight * index + rowHeight * 0.65f
            val isNext = index == nextIndex
            val isPast = time != null && time.before(now)

            labelPaint.color = if (isNext) GOLD else INK_SOFT
            labelPaint.isFakeBoldText = isNext
            labelPaint.alpha = if (isPast && !isNext) 110 else 255

            timePaint.color = if (isNext) GOLD else INK
            timePaint.isFakeBoldText = isNext
            timePaint.alpha = if (isPast && !isNext) 110 else 255

            canvas.drawText(label, rect.right - rect.width() * 0.07f, y, labelPaint)
            canvas.drawText(ZmanimProvider.formatTime(time), rect.left + rect.width() * 0.07f, y, timePaint)

            if (isNext) {
                val dotR = rect.width() * 0.009f
                val dotX = rect.right - rect.width() * 0.07f - labelPaint.measureText(label) - dotR * 3
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD }
                canvas.drawCircle(dotX, y - rowHeight * 0.12f, dotR, dotPaint)
            }
        }
    }
}
