package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.zmanim.lockscreen.data.ZmanimSettings
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar

/**
 * Redraws the vintage zmanim glass card once a minute - Canvas/Paint only, since live
 * wallpapers don't host Compose. The heavy lifting lives in [VintageScene] (background
 * art), [GrainTexture] (film grain) and [GlassCard] (the blurred card + zmanim text);
 * this class is just the WallpaperService/Engine plumbing and redraw scheduling.
 */
class ZmanimWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ZmanimEngine()

    private inner class ZmanimEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private val grain = GrainTexture()

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
            val width = canvas.width
            val height = canvas.height
            if (width <= 0 || height <= 0) return

            val settings = ZmanimSettings(this@ZmanimWallpaperService)
            val day = ZmanimProvider(settings.location).today()
            val palette = SkyPalette.forTime(Calendar.getInstance().time, day.netzHachama, day.shkia)

            // Rendered to a bitmap first (not straight to the surface) so the card can
            // sample a blurred copy of the scene behind it - the "frosted glass" look.
            val scene = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            VintageScene.draw(Canvas(scene), width, height, palette)
            grain.apply(Canvas(scene), width, height)

            canvas.drawBitmap(scene, 0f, 0f, null)
            GlassCard.draw(canvas, scene, width, height, day, settings.location.name)

            scene.recycle()
        }
    }

    companion object {
        private const val REDRAW_INTERVAL_MS = 60_000L
    }
}
