package com.zmanim.lockscreen.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin

/**
 * Compact transparent glass card for the lock screen.
 * The background photo stays clearly visible through the card.
 */
object TransparentGlassCard {
    private val WHITE = Color.WHITE
    private val GOLD = Color.parseColor("#E7C27A")

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val card = RectF(w * 0.10f, h * 0.37f, w * 0.90f, h * 0.72f)
        val radius = w * 0.055f

        // Subtle shadow, then translucent glass. No opaque parchment and no full-screen blur.
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(70, 0, 0, 0)
            setShadowLayer(w * 0.025f, 0f, h * 0.006f, Color.argb(120, 0, 0, 0))
        }
        canvas.drawRoundRect(RectF(card.left, card.top + h * 0.005f, card.right, card.bottom + h * 0.005f), radius, radius, shadow)

        val glass = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(62, 255, 248, 232) }
        canvas.drawRoundRect(card, radius, radius, glass)

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(125, 255, 242, 207)
            style = Paint.Style.STROKE
            strokeWidth = w * 0.003f
        }
        canvas.drawRoundRect(card, radius, radius, border)

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE; textAlign = Paint.Align.CENTER; textSize = w * 0.060f; isFakeBoldText = true
            setShadowLayer(5f, 0f, 2f, Color.argb(180,0,0,0))
        }
        canvas.drawText("זמני היום", card.centerX(), card.top + h * 0.038f, title)

        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(235,255,255,255); textAlign = Paint.Align.CENTER; textSize = w * 0.031f
            setShadowLayer(4f,0f,2f,Color.argb(160,0,0,0))
        }
        canvas.drawText(locationName, card.centerX(), card.top + h * 0.062f, small)
        canvas.drawText(day.hebrewDate, card.centerX(), card.top + h * 0.088f, small)
        canvas.drawText(day.gregorianDate, card.centerX(), card.top + h * 0.111f, Paint(small).apply { textSize = w * 0.025f })

        val midY = card.top + h * 0.205f
        drawClock(canvas, card.left + card.width() * 0.25f, midY, card.width() * 0.17f)

        val next = nextUpcoming(day)
        val nextCx = card.left + card.width() * 0.69f
        val nextTitle = Paint(small).apply { textSize = w * 0.028f }
        canvas.drawText("הזמן הקרוב", nextCx, midY - h * 0.060f, nextTitle)
        val nextLabel = Paint(title).apply { textSize = w * 0.040f }
        canvas.drawText(next.first, nextCx, midY - h * 0.020f, nextLabel)
        val nextTime = Paint(title).apply { textSize = w * 0.066f; color = GOLD }
        canvas.drawText(ZmanimProvider.formatTime(next.second), nextCx, midY + h * 0.030f, nextTime)
        val mins = next.second?.let { ((it.time - System.currentTimeMillis()) / 60000L).coerceAtLeast(0) }
        if (mins != null) canvas.drawText("בעוד $mins דקות", nextCx, midY + h * 0.060f, nextTitle)

        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(95,255,255,255); strokeWidth = 1.5f }
        val dividerY = card.top + h * 0.270f
        canvas.drawLine(card.left + w*0.035f, dividerY, card.right - w*0.035f, dividerY, divider)

        val cells = listOf(
            "עלות השחר" to day.alosHashachar,
            "הנץ" to day.netzHachama,
            "סוף ק״ש" to day.sofZmanShmaGra,
            "חצות" to day.chatzos,
            "מנחה" to day.minchaGedola,
            "פלג" to day.plagHamincha,
            "שקיעה" to day.shkia,
            "צאת" to day.tzais
        )
        val cellW = (card.width() - w*0.05f) / cells.size
        val labelPaint = Paint(small).apply { textSize = w * 0.021f }
        val timePaint = Paint(title).apply { textSize = w * 0.028f }
        cells.forEachIndexed { i, item ->
            val cx = card.left + w*0.025f + cellW*(i+0.5f)
            canvas.drawText(item.first, cx, dividerY + h*0.037f, labelPaint)
            canvas.drawText(ZmanimProvider.formatTime(item.second), cx, dividerY + h*0.066f, timePaint)
        }

        val bottomY = card.bottom - h*0.040f
        val eventPaint = Paint(small).apply { textSize = w * 0.027f }
        val candle = "כניסת שבת ${ZmanimProvider.formatTime(day.candleLighting)}"
        canvas.drawText(candle, card.left + card.width()*0.27f, bottomY, eventPaint)
        val special = day.roshChodeshLabel ?: day.parshaLabel ?: ""
        canvas.drawText(special, card.left + card.width()*0.72f, bottomY, eventPaint)
    }

    private fun nextUpcoming(day: DayZmanim): Pair<String, Date?> {
        val now = Date()
        return listOf(
            "עלות השחר" to day.alosHashachar,
            "הנץ החמה" to day.netzHachama,
            "סוף זמן ק״ש" to day.sofZmanShmaGra,
            "חצות" to day.chatzos,
            "מנחה גדולה" to day.minchaGedola,
            "פלג המנחה" to day.plagHamincha,
            "שקיעה" to day.shkia,
            "צאת הכוכבים" to day.tzais
        ).firstOrNull { it.second?.after(now) == true } ?: ("מחר" to null)
    }

    private fun drawClock(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(38,255,255,255) }
        canvas.drawCircle(cx, cy, r, face)
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(175,255,239,199); style = Paint.Style.STROKE; strokeWidth = r*0.035f }
        canvas.drawCircle(cx, cy, r, ring)

        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE; textAlign = Paint.Align.CENTER; textSize = r*0.22f
            setShadowLayer(3f,0f,1f,Color.argb(180,0,0,0))
        }
        for (n in 1..12) {
            val a = Math.toRadians((n*30-90).toDouble())
            canvas.drawText(n.toString(), cx + r*0.76f*cos(a).toFloat(), cy + r*0.76f*sin(a).toFloat()+numberPaint.textSize*0.32f, numberPaint)
        }

        val cal = Calendar.getInstance()
        val sec = cal.get(Calendar.SECOND)
        val min = cal.get(Calendar.MINUTE) + sec/60f
        val hour = cal.get(Calendar.HOUR) + min/60f
        fun hand(angleDeg: Double, length: Float, width: Float, color: Int) {
            val a = Math.toRadians(angleDeg-90.0)
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color=color; strokeWidth=width; strokeCap=Paint.Cap.ROUND }
            canvas.drawLine(cx, cy, cx+length*cos(a).toFloat(), cy+length*sin(a).toFloat(), p)
        }
        hand(hour*30.0, r*0.48f, r*0.060f, WHITE)
        hand(min*6.0, r*0.68f, r*0.040f, WHITE)
        hand(sec*6.0, r*0.78f, r*0.015f, Color.parseColor("#C85645"))
        canvas.drawCircle(cx, cy, r*0.045f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD })
    }
}
