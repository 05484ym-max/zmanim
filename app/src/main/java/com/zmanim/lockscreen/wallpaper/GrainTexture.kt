package com.zmanim.lockscreen.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import kotlin.random.Random

/**
 * Cheap film-grain overlay: a small random-noise tile, generated once and repeated
 * across the canvas with an overlay blend - the same trick the mockup uses via an
 * SVG feTurbulence filter, just baked to a bitmap instead.
 */
class GrainTexture {

    private val tile: Bitmap by lazy { buildTile() }

    private fun buildTile(): Bitmap {
        val size = 96
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val rnd = Random(7)
        val pixels = IntArray(size * size)
        for (i in pixels.indices) {
            val v = rnd.nextInt(256)
            pixels[i] = Color.argb(255, v, v, v)
        }
        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        return bmp
    }

    fun apply(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint().apply {
            shader = BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
