package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.zmanim.lockscreen.data.ZmanimSettings
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar

/** Live lock-screen wallpaper with a compact vintage zmanim card. */
class ZmanimWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ZmanimEngine()

    private inner class ZmanimEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private val grain = GrainTexture()
        private var selectedDayOffset = 0
        private var touchDownX = 0f

        init {
            setTouchEventsEnabled(true)
        }

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
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> touchDownX = event.x
                MotionEvent.ACTION_UP -> {
                    val dx = event.x - touchDownX
                    val threshold = surfaceHolder.surfaceFrame.width() * 0.10f
                    if (kotlin.math.abs(dx) > threshold) {
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
            val palette = SkyPalette.forTime(Calendar.getInstance().time, day.netzHachama, day.shkia)

            val scene = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            VintageScene.draw(Canvas(scene), width, height, palette)
            grain.apply(Canvas(scene), width, height)

            canvas.drawBitmap(scene, 0f, 0f, null)
            GlassCard.draw(
                canvas = canvas,
                background = scene,
                width = width,
                height = height,
                day = day,
                locationName = settings.location.name,
                isBrowsing = selectedDayOffset != 0
            )

            scene.recycle()
        }
    }

    companion object {
        // One-second redraw keeps the second hand genuinely live while the wallpaper is visible.
        private const val REDRAW_INTERVAL_MS = 1_000L
    }
}
