package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.zmanim.lockscreen.data.ZmanimSettings
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

/**
 * Live lock-screen wallpaper: the selected photo stays full-screen and untouched,
 * while the antique zmanim card is drawn on top of it.
 */
class ZmanimWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ZmanimEngine()

    private inner class ZmanimEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        private var cachedBackgroundUri: String? = null
        private var cachedBackground: Bitmap? = null

        private var cachedDayKey: String? = null
        private var cachedDay: DayZmanim? = null

        private val drawRunnable = object : Runnable {
            override fun run() {
                draw()
                if (visible) handler.postDelayed(this, REDRAW_INTERVAL_MS)
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            handler.removeCallbacks(drawRunnable)
            if (visible) handler.post(drawRunnable)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
            cachedBackground?.recycle()
            cachedBackground = null
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
            val width = canvas.width
            val height = canvas.height
            if (width <= 0 || height <= 0) return

            val settings = ZmanimSettings(this@ZmanimWallpaperService)
            val day = dayFor(settings)

            val background = getBackground(settings.backgroundUri)
            if (background != null) {
                // Repaint the whole frame from the selected image every second. This prevents
                // moving clock hands from leaving trails or duplicate red second hands.
                drawCenterCrop(canvas, background, width, height)
            } else {
                val palette = SkyPalette.forTime(Calendar.getInstance().time, day.netzHachama, day.shkia)
                val scene = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                VintageScene.draw(Canvas(scene), width, height, palette)
                canvas.drawBitmap(scene, 0f, 0f, null)
                scene.recycle()
            }

            // IMPORTANT: use the antique reference renderer. Previous builds were still calling
            // GlassOverlay, so the detailed bronze code in GlassCard never appeared on screen.
            GlassCard.draw(canvas, null, width, height, day, settings.location.name)
        }

        /** Astronomical + Jewish-calendar lookups are only recomputed once a day (or on location change). */
        private fun dayFor(settings: ZmanimSettings): DayZmanim {
            val key = DAY_KEY_FORMAT.format(Calendar.getInstance().time) + "|" + settings.location.name
            cachedDay?.let { if (cachedDayKey == key) return it }
            val fresh = ZmanimProvider(settings.location).today()
            cachedDayKey = key
            cachedDay = fresh
            return fresh
        }

        private fun getBackground(uriString: String?): Bitmap? {
            if (uriString.isNullOrBlank()) return null
            if (uriString == cachedBackgroundUri && cachedBackground?.isRecycled == false) return cachedBackground

            cachedBackground?.recycle()
            cachedBackground = null
            cachedBackgroundUri = uriString

            cachedBackground = runCatching {
                contentResolver.openInputStream(Uri.parse(uriString)).use { input ->
                    if (input == null) null else BitmapFactory.decodeStream(input)
                }
            }.getOrNull()
            return cachedBackground
        }

        private fun drawCenterCrop(canvas: Canvas, bitmap: Bitmap, width: Int, height: Int) {
            val scale = max(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
            val srcW = width / scale
            val srcH = height / scale
            val left = ((bitmap.width - srcW) / 2f).coerceAtLeast(0f)
            val top = ((bitmap.height - srcH) / 2f).coerceAtLeast(0f)
            val src = Rect(left.toInt(), top.toInt(), (left + srcW).toInt(), (top + srcH).toInt())
            val dst = RectF(0f, 0f, width.toFloat(), height.toFloat())
            canvas.drawBitmap(bitmap, src, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        }
    }

    companion object {
        private const val REDRAW_INTERVAL_MS = 1_000L
        private val DAY_KEY_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}
