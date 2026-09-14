package com.zmanim.lockscreen.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.zmanim.lockscreen.data.ZmanimSettings
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar

/**
 * Draws the zmanim glass card straight onto the wallpaper Surface with Canvas/Paint - live
 * wallpapers don't host Compose, so this is intentionally plain 2D drawing. Redraws once a
 * minute (and only while actually visible) since the zmanim/date only change at that grain.
 *
 * This is the *skeleton* renderer: flat rounded rect + text rows, no blur/grain/vintage
 * treatment yet. Swap render()/drawCard() for the real art direction once it's settled -
 * everything else (scheduling, zmanim data, palette-by-time-of-day) stays as-is.
 */
class ZmanimWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ZmanimEngine()

    private inner class ZmanimEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        private val drawRunnable = object : Runnable {
            override fun run() {
                draw()
                if (visible) {
                    handler.postDelayed(this, REDRAW_INTERVAL_MS)
                }
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            handler.removeCallbacks(drawRunnable)
            if (visible) {
                handler.post(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.let { render(it) }
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
        }

        private fun render(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()

            val settings = ZmanimSettings(this@ZmanimWallpaperService)
            val day = ZmanimProvider(settings.location).today()

            drawBackground(canvas, width, height, day)
            drawCard(canvas, width, height, day)
        }

        private fun drawBackground(canvas: Canvas, width: Float, height: Float, day: DayZmanim) {
            val colors = SkyPalette.forTime(Calendar.getInstance().time, day.netzHachama, day.shkia)
            val paint = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, height, colors, null, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, width, height, paint)
        }

        private fun drawCard(canvas: Canvas, width: Float, height: Float, day: DayZmanim) {
            val margin = width * 0.06f
            val top = height * 0.40f
            val cardRect = RectF(margin, top, width - margin, top + height * 0.36f)
            val corner = 48f

            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(90, 250, 240, 219)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(cardRect, corner, corner, cardPaint)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(140, 255, 246, 222)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(cardRect, corner, corner, borderPaint)

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0x43, 0x30, 0x1D)
                textSize = 40f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText(day.hebrewDate, cardRect.centerX(), cardRect.top + 56f, titlePaint)

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0x5A, 0x3F, 0x24)
                textSize = 32f
                textAlign = Paint.Align.RIGHT
            }
            val timePaint = Paint(labelPaint).apply {
                color = Color.rgb(0x43, 0x30, 0x1D)
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
            }

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

            var y = cardRect.top + 110f
            val rowHeight = (cardRect.height() - 130f) / rows.size
            for ((label, time) in rows) {
                canvas.drawText(label, cardRect.right - 32f, y, labelPaint)
                canvas.drawText(ZmanimProvider.formatTime(time), cardRect.left + 32f, y, timePaint)
                y += rowHeight
            }
        }
    }

    companion object {
        private const val REDRAW_INTERVAL_MS = 60_000L
    }
}
