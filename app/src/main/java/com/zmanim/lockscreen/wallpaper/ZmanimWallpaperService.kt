package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.zmanim.lockscreen.data.ZmanimSettings
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max

/** Live lock-screen wallpaper with a transparent glass zmanim card. */
class ZmanimWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ZmanimEngine()

    private inner class ZmanimEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var selectedDayOffset = 0
        private var touchDownX = 0f
        private var cachedBackgroundUri: String? = null
        private var cachedBackground: Bitmap? = null

        init { setTouchEventsEnabled(true) }

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

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> touchDownX = event.x
                MotionEvent.ACTION_UP -> {
                    val dx = event.x - touchDownX
                    val threshold = surfaceHolder.surfaceFrame.width() * 0.10f
                    if (abs(dx) > threshold) {
                        selectedDayOffset += if (dx < 0) 1 else -1
                        selectedDayOffset = selectedDayOffset.coerceIn(-365, 365)
                        draw()
                    }
                }
            }
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
            val provider = ZmanimProvider(settings.location)
            val selectedDate = Calendar.getInstance().apply { add(Calendar.DATE, selectedDayOffset) }
            val day = provider.forDate(selectedDate)

            // Keep the user's chosen photo crisp and untouched. No grain and no full-screen blur.
            val background = getBackground(settings.backgroundUri)
            if (background != null) {
                drawCenterCrop(canvas, background, width, height)
            } else {
                // Fallback only when no photo was chosen yet.
                val palette = SkyPalette.forTime(Calendar.getInstance().time, day.netzHachama, day.shkia)
                val scene = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                VintageScene.draw(Canvas(scene), width, height, palette)
                canvas.drawBitmap(scene, 0f, 0f, null)
                scene.recycle()
            }

            TransparentGlassCard.draw(
                canvas = canvas,
                width = width,
                height = height,
                day = day,
                locationName = settings.location.name
            )
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
            canvas.drawColor(Color.BLACK)
            canvas.drawBitmap(bitmap, src, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        }
    }

    companion object {
        private const val REDRAW_INTERVAL_MS = 1_000L
    }
}
