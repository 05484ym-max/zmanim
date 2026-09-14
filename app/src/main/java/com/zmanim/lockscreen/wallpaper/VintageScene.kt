package com.zmanim.lockscreen.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.random.Random

/**
 * Draws the vintage Jerusalem-postcard background: sky gradient, hazy distant hills,
 * a city-wall/dome silhouette, two cypress trees, an olive branch in the top corner,
 * a warm duotone wash and a vignette. Grain is applied separately by [GrainTexture]
 * so it can also be layered over the glass card.
 */
object VintageScene {

    fun draw(canvas: Canvas, width: Int, height: Int, palette: SkyPalette.Palette) {
        val w = width.toFloat()
        val h = height.toFloat()

        drawSky(canvas, w, h, palette)
        drawHills(canvas, w, h, palette)
        drawSkyline(canvas, w, h, palette)
        drawCypress(canvas, w, h, palette)
        drawOliveBranch(canvas, w, h, palette)
        drawGround(canvas, w, h)
        drawDuotone(canvas, w, h, palette)
        drawVignette(canvas, w, h)
    }

    private fun drawSky(canvas: Canvas, w: Float, h: Float, palette: SkyPalette.Palette) {
        val paint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h, palette.sky, null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w, h, paint)
    }

    private fun drawHills(canvas: Canvas, w: Float, h: Float, palette: SkyPalette.Palette) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.hill
            alpha = 110
        }
        val top = h * 0.60f
        canvas.drawOval(RectF(-w * 0.2f, top, w * 1.2f, top + h * 0.22f), paint)
    }

    private fun drawSkyline(canvas: Canvas, w: Float, h: Float, palette: SkyPalette.Palette) {
        val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.silhouette }
        val wallTop = h * 0.72f
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(0f, wallTop + h * 0.05f)
            var x = 0f
            val step = w / 14f
            var toggle = true
            while (x < w) {
                val y = if (toggle) wallTop else wallTop + h * 0.03f
                lineTo(x, y)
                x += step
                toggle = !toggle
            }
            lineTo(w, h)
            close()
        }
        canvas.drawPath(path, wallPaint)

        // central dome
        val domeCx = w * 0.5f
        val domeBaseY = wallTop
        val domeRadius = w * 0.075f
        val domePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                domeCx - domeRadius * 0.3f, domeBaseY - domeRadius * 1.3f, domeRadius * 1.6f,
                palette.domeHighlight, palette.domeShadow, Shader.TileMode.CLAMP
            )
        }
        val domeRect = RectF(
            domeCx - domeRadius, domeBaseY - domeRadius * 1.8f,
            domeCx + domeRadius, domeBaseY - domeRadius * 0.6f
        )
        canvas.drawOval(domeRect, domePaint)
        canvas.drawRect(
            domeCx - domeRadius * 0.35f, domeBaseY - domeRadius * 0.9f,
            domeCx + domeRadius * 0.35f, domeBaseY, wallPaint
        )
    }

    private fun drawCypress(canvas: Canvas, w: Float, h: Float, palette: SkyPalette.Palette) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.cypress }
        drawOneCypress(canvas, w * 0.06f, h * 0.92f, w * 0.045f, h * 0.24f, paint)
        drawOneCypress(canvas, w * 0.90f, h * 0.94f, w * 0.035f, h * 0.18f, paint)
    }

    private fun drawOneCypress(canvas: Canvas, baseX: Float, baseY: Float, width: Float, height: Float, paint: Paint) {
        val rect = RectF(baseX - width / 2f, baseY - height, baseX + width / 2f, baseY)
        canvas.drawRoundRect(rect, width * 0.6f, width * 0.6f, paint)
    }

    private fun drawOliveBranch(canvas: Canvas, w: Float, h: Float, palette: SkyPalette.Palette) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.olive }
        val rnd = Random(42)
        canvas.save()
        repeat(10) {
            val lx = rnd.nextInt((w * 0.22f).toInt().coerceAtLeast(1)).toFloat()
            val ly = rnd.nextInt((h * 0.16f).toInt().coerceAtLeast(1)).toFloat()
            val angle = (rnd.nextInt(60) - 30).toFloat()
            canvas.save()
            canvas.translate(lx, ly)
            canvas.rotate(angle)
            canvas.drawRoundRect(RectF(-w * 0.017f, -w * 0.008f, w * 0.017f, w * 0.008f), w * 0.01f, w * 0.004f, paint)
            canvas.restore()
        }
        canvas.restore()
    }

    private fun drawGround(canvas: Canvas, w: Float, h: Float) {
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, h * 0.9f, 0f, h,
                intArrayOf(Color.TRANSPARENT, Color.rgb(0x15, 0x0d, 0x05)),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, h * 0.88f, w, h, paint)
    }

    private fun drawDuotone(canvas: Canvas, w: Float, h: Float, palette: SkyPalette.Palette) {
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w * 0.3f, h,
                intArrayOf(palette.duotoneTop, palette.duotoneBottom),
                null, Shader.TileMode.CLAMP
            )
            xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        }
        canvas.drawRect(0f, 0f, w, h, paint)
    }

    private fun drawVignette(canvas: Canvas, w: Float, h: Float) {
        val paint = Paint().apply {
            shader = RadialGradient(
                w / 2f, h / 2f, w * 0.85f,
                intArrayOf(Color.TRANSPARENT, Color.argb(140, 20, 12, 6)),
                floatArrayOf(0.55f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, paint)
    }
}
