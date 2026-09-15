package com.zmanim.lockscreen.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.zmanim.lockscreen.zmanim.DayZmanim
import com.zmanim.lockscreen.zmanim.ZmanimProvider
import java.util.Calendar
import java.util.Date
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

object ReferenceBronzeCard {
    private val GOLD = Color.parseColor("#B77A2E")
    private val GOLD_LIGHT = Color.parseColor("#E9C77A")
    private val GOLD_HIGHLIGHT = Color.parseColor("#FFE6A8")
    private val GOLD_DARK = Color.parseColor("#573313")
    private val BRONZE_TOP = Color.parseColor("#78502A")
    private val BRONZE_MID = Color.parseColor("#51321B")
    private val BRONZE_BOTTOM = Color.parseColor("#2A190E")
    private val IVORY = Color.parseColor("#F2DFB8")
    private val INK = Color.parseColor("#1B120B")
    private val RED = Color.parseColor("#D73524")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private data class Cell(val label: String, val value: Date?)

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cardW = w * .79f
        val cardH = h * .50f
        val top = h * .305f
        val card = RectF((w - cardW) / 2f, top, (w + cardW) / 2f, top + cardH)

        drawPlate(canvas, card)
        drawCorners(canvas, card)
        drawHeader(canvas, card, day, locationName)
        drawClock(canvas, card)
        drawGrid(canvas, card, day)
        drawFooter(canvas, card, day)
    }

    private fun drawPlate(canvas: Canvas, card: RectF) {
        val radius = card.width() * .040f
        canvas.drawRoundRect(
            RectF(card.left, card.top + 3f, card.right, card.bottom + 3f), radius, radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(8, 0, 0, 0)
                setShadowLayer(card.width() * .012f, 0f, card.width() * .006f, Color.argb(72, 0, 0, 0))
            }
        )

        // More transparent plaque: the wallpaper remains clearly visible through the bronze.
        val base = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                card.left, card.top, card.right, card.bottom,
                intArrayOf(
                    Color.argb(132, Color.red(BRONZE_TOP), Color.green(BRONZE_TOP), Color.blue(BRONZE_TOP)),
                    Color.argb(118, Color.red(BRONZE_MID), Color.green(BRONZE_MID), Color.blue(BRONZE_MID)),
                    Color.argb(106, Color.red(BRONZE_BOTTOM), Color.green(BRONZE_BOTTOM), Color.blue(BRONZE_BOTTOM))
                ),
                floatArrayOf(0f, .45f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(card, radius, radius, base)

        canvas.save()
        canvas.clipPath(Path().apply { addRoundRect(card, radius, radius, Path.Direction.CW) })
        val rnd = Random(1948L)
        val texture = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(470) {
            val x = card.left + rnd.nextFloat() * card.width()
            val y = card.top + rnd.nextFloat() * card.height()
            val rr = card.width() * (.00045f + rnd.nextFloat() * .0021f)
            texture.color = if (rnd.nextBoolean()) Color.argb(10, 246, 205, 124) else Color.argb(12, 30, 16, 8)
            canvas.drawOval(RectF(x - rr * 2.1f, y - rr, x + rr * 2.1f, y + rr), texture)
        }
        repeat(90) {
            val x = card.left + rnd.nextFloat() * card.width()
            val y = card.top + rnd.nextFloat() * card.height()
            texture.style = Paint.Style.STROKE
            texture.strokeWidth = card.width() * .00065f
            texture.color = if (rnd.nextBoolean()) Color.argb(12, 239, 194, 105) else Color.argb(12, 40, 20, 9)
            canvas.drawLine(x, y, x + card.width() * (.012f + rnd.nextFloat() * .045f), y + rnd.nextFloat() * 3f - 1.5f, texture)
        }
        canvas.restore()

        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = card.width() * .0145f
            shader = LinearGradient(
                card.left, card.top, card.right, card.bottom,
                intArrayOf(Color.parseColor("#F0CF85"), GOLD, GOLD_DARK, GOLD_LIGHT),
                floatArrayOf(0f, .31f, .72f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(card, radius, radius, outer)

        val darkInset = card.width() * .0155f
        canvas.drawRoundRect(
            RectF(card.left + darkInset, card.top + darkInset, card.right - darkInset, card.bottom - darkInset),
            radius * .80f, radius * .80f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = card.width() * .0045f
                color = Color.argb(215, 70, 40, 16)
            }
        )
        val brightInset = card.width() * .023f
        canvas.drawRoundRect(
            RectF(card.left + brightInset, card.top + brightInset, card.right - brightInset, card.bottom - brightInset),
            radius * .68f, radius * .68f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = card.width() * .0022f
                color = Color.argb(210, 234, 192, 109)
            }
        )
    }

    private fun drawCorners(canvas: Canvas, card: RectF) {
        fun one(x: Float, y: Float, sx: Float, sy: Float) {
            val s = card.width() * .087f
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.argb(235, 235, 197, 122)
                strokeWidth = card.width() * .0043f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            val path = Path().apply {
                moveTo(x, y + sy * s)
                cubicTo(x + sx * s * .08f, y + sy * s * .56f, x + sx * s * .27f, y + sy * s * .20f, x + sx * s, y)
                moveTo(x + sx * s * .10f, y + sy * s * .73f)
                cubicTo(x + sx * s * .45f, y + sy * s * .63f, x + sx * s * .49f, y + sy * s * .30f, x + sx * s * .22f, y + sy * s * .16f)
                moveTo(x + sx * s * .38f, y + sy * s * .53f)
                cubicTo(x + sx * s * .79f, y + sy * s * .46f, x + sx * s * .77f, y + sy * s * .16f, x + sx * s * .55f, y + sy * s * .09f)
                moveTo(x + sx * s * .19f, y + sy * s * .37f)
                cubicTo(x + sx * s * .02f, y + sy * s * .24f, x + sx * s * .07f, y + sy * s * .04f, x + sx * s * .32f, y + sy * s * .02f)
            }
            canvas.drawPath(path, p)
            canvas.drawCircle(x + sx * s * .34f, y + sy * s * .34f, card.width() * .007f, p)
        }
        val m = card.width() * .040f
        one(card.left + m, card.top + m, 1f, 1f)
        one(card.right - m, card.top + m, -1f, 1f)
        one(card.left + m, card.bottom - m, 1f, -1f)
        one(card.right - m, card.bottom - m, -1f, -1f)
    }

    private fun drawHeader(canvas: Canvas, card: RectF, day: DayZmanim, locationName: String) {
        val w = card.width(); val h = card.height()
        val right = RectF(card.left + w * .50f, card.top + h * .035f, card.right - w * .045f, card.top + h * .355f)
        embossedRtl(canvas, "זמני היום", RectF(right.left, right.top, right.right, right.top + right.height() * .28f), w * .076f, true)
        embossedRtl(canvas, locationName, RectF(right.left, right.top + right.height() * .30f, right.right - w * .06f, right.top + right.height() * .50f), w * .049f, false)
        drawPin(canvas, right.right - w * .026f, right.top + right.height() * .405f, w * .020f)
        embossedRtl(canvas, day.hebrewDate, RectF(right.left, right.top + right.height() * .54f, right.right, right.top + right.height() * .76f), w * .044f, true)
        val datePaint = textPaint(w * .034f, IVORY, false).apply {
            textAlign = Paint.Align.CENTER
            setShadowLayer(w * .004f, 0f, w * .003f, Color.argb(160, 0, 0, 0))
        }
        canvas.drawText(day.gregorianDate, right.centerX(), right.top + right.height() * .97f, datePaint)
    }

    private fun drawClock(canvas: Canvas, card: RectF) {
        val w = card.width(); val h = card.height()
        val cx = card.left + w * .285f
        val cy = card.top + h * .205f
        val r = w * .196f

        canvas.drawCircle(cx, cy + r * .025f, r * 1.085f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(24, 0, 0, 0)
            setShadowLayer(r * .12f, 0f, r * .045f, Color.argb(120, 0, 0, 0))
        })
        val bezel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .24f, cy - r * .25f, r * 1.22f,
                intArrayOf(Color.parseColor("#F2D18A"), Color.parseColor("#BA7C2D"), Color.parseColor("#6D4419"), Color.parseColor("#25170C")),
                floatArrayOf(0f, .38f, .73f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r * 1.07f, bezel)
        canvas.drawCircle(cx, cy, r * .985f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(228, 34, 22, 12) })

        // Lighter parchment-like dial, closer to the reference, but still slightly translucent.
        canvas.drawCircle(cx, cy, r * .91f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .28f, cy - r * .30f, r,
                intArrayOf(
                    Color.argb(235, 238, 211, 151),
                    Color.argb(225, 211, 171, 104),
                    Color.argb(214, 156, 103, 52)
                ),
                floatArrayOf(0f, .58f, 1f), Shader.TileMode.CLAMP
            )
        })

        // Mechanical train stays inside the center so the numeral ring remains clean.
        canvas.save()
        canvas.clipPath(Path().apply { addCircle(cx, cy, r * .49f, Path.Direction.CW) })
        val baseAngle = (System.currentTimeMillis() % 300000L).toFloat() / 300000f * 360f
        drawMeshedGear(canvas, cx - r * .24f, cy + r * .02f, r * .235f, 18, baseAngle, 0f,
            Color.parseColor("#B7792C"), Color.parseColor("#7A4A1B"), Color.parseColor("#382111"))
        drawMeshedGear(canvas, cx + r * .08f, cy - r * .15f, r * .185f, 14, -baseAngle * (18f / 14f), 10f,
            Color.parseColor("#C28A37"), Color.parseColor("#835522"), Color.parseColor("#3B2414"))
        drawMeshedGear(canvas, cx + r * .29f, cy + r * .08f, r * .14f, 10, baseAngle * (18f / 10f), 7f,
            Color.parseColor("#A86727"), Color.parseColor("#6C4219"), Color.parseColor("#2D1A0F"))
        drawMeshedGear(canvas, cx + r * .08f, cy + r * .27f, r * .12f, 9, -baseAngle * (18f / 9f), 3f,
            Color.parseColor("#C79343"), Color.parseColor("#7A5124"), Color.parseColor("#362116"))
        drawMeshedGear(canvas, cx - r * .16f, cy - r * .24f, r * .105f, 8, -baseAngle * (18f / 8f), 18f,
            Color.parseColor("#8F5721"), Color.parseColor("#5A3517"), Color.parseColor("#28170D"))
        drawMeshedGear(canvas, cx - r * .01f, cy + r * .02f, r * .082f, 7, baseAngle * (18f / 7f), 5f,
            Color.parseColor("#D0A057"), Color.parseColor("#855D29"), Color.parseColor("#3A2416"))
        canvas.restore()

        // Warm veil integrates the mechanism and keeps it behind the numerals.
        canvas.drawCircle(cx, cy, r * .90f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .20f, cy - r * .24f, r,
                intArrayOf(Color.argb(74, 255, 235, 188), Color.argb(44, 214, 175, 110), Color.argb(42, 87, 54, 27)),
                null, Shader.TileMode.CLAMP
            )
        })

        canvas.drawCircle(cx, cy, r * .865f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .022f
            color = Color.argb(235, 69, 40, 16)
        })
        canvas.drawCircle(cx, cy, r * .838f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .007f
            color = Color.argb(155, 245, 210, 135)
        })

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#21150D")
            strokeCap = Paint.Cap.SQUARE
        }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val outer = r * .80f
            val inner = if (i % 5 == 0) r * .62f else r * .71f
            tick.strokeWidth = if (i % 5 == 0) r * .027f else r * .0085f
            canvas.drawLine(
                cx + inner * cos(a).toFloat(), cy + inner * sin(a).toFloat(),
                cx + outer * cos(a).toFloat(), cy + outer * sin(a).toFloat(), tick
            )
        }

        // Larger, darker and fully opaque numerals for stronger presence like the reference.
        val numeralFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(105, 241, 215, 157) }
        listOf(12 to 0, 3 to 90, 6 to 180, 9 to 270).forEach { (_, deg) ->
            val a = Math.toRadians((deg - 90).toDouble())
            val nx = cx + r * .53f * cos(a).toFloat()
            val ny = cy + r * .53f * sin(a).toFloat()
            canvas.drawCircle(nx, ny, r * .135f, numeralFill)
        }
        val num = textPaint(r * .30f, Color.parseColor("#1E130C"), true).apply {
            textAlign = Paint.Align.CENTER
            alpha = 255
            setShadowLayer(r * .010f, 0f, r * .006f, Color.argb(80, 255, 235, 180))
        }
        listOf(12 to 0, 3 to 90, 6 to 180, 9 to 270).forEach { (n, deg) ->
            val a = Math.toRadians((deg - 90).toDouble())
            canvas.drawText(
                n.toString(),
                cx + r * .53f * cos(a).toFloat(),
                cy + r * .53f * sin(a).toFloat() + num.textSize * .34f,
                num
            )
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .45f, hour * 30f, r * .064f, Color.parseColor("#15100B"))
        hand(canvas, cx, cy, r * .66f, min * 6f, r * .042f, Color.parseColor("#15100B"))
        hand(canvas, cx, cy, r * .73f, sec * 6f, r * .012f, RED)
        canvas.drawCircle(cx, cy, r * .060f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx - r * .02f, cy - r * .02f, r * .06f,
                intArrayOf(GOLD_HIGHLIGHT, GOLD, GOLD_DARK), null, Shader.TileMode.CLAMP)
        })
        canvas.drawCircle(cx, cy, r * .023f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK })
    }

    private fun drawMeshedGear(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        teeth: Int,
        rotation: Float,
        phaseOffset: Float,
        light: Int,
        mid: Int,
        dark: Int
    ) {
        canvas.save()
        canvas.rotate(rotation + phaseOffset, cx, cy)

        val path = Path()
        val steps = teeth * 8
        for (i in 0 until steps) {
            val a = Math.toRadians((i * 360f / steps - 90f).toDouble())
            val rr = when (i % 8) {
                0, 7 -> r * .78f
                1, 6 -> r * .88f
                2, 5 -> r
                else -> r * .96f
            }
            val x = cx + rr * cos(a).toFloat()
            val y = cy + rr * sin(a).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(96, 15, 8, 5)
            setShadowLayer(r * .08f, r * .02f, r * .04f, Color.argb(135, 0, 0, 0))
        })
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .30f, cy - r * .32f, r * 1.08f,
                intArrayOf(light, mid, dark), floatArrayOf(0f, .58f, 1f), Shader.TileMode.CLAMP
            )
        })
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .040f
            color = Color.argb(205, 43, 25, 13)
        })

        canvas.drawCircle(cx, cy, r * .68f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .028f
            color = Color.argb(96, 245, 204, 112)
        })

        val holes = 5
        for (i in 0 until holes) {
            val a = Math.toRadians((i * 360.0 / holes) - 90.0)
            val hx = cx + r * .43f * cos(a).toFloat()
            val hy = cy + r * .43f * sin(a).toFloat()
            canvas.drawCircle(hx, hy, r * .13f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    hx - r * .03f, hy - r * .03f, r * .15f,
                    intArrayOf(Color.parseColor("#170F08"), Color.parseColor("#3E2814")), null, Shader.TileMode.CLAMP
                )
            })
            canvas.drawCircle(hx, hy, r * .13f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = r * .018f
                color = Color.argb(115, 222, 166, 80)
            })
        }

        for (i in 0 until holes) {
            val a = Math.toRadians((i * 360.0 / holes) - 90.0)
            val x1 = cx + r * .17f * cos(a).toFloat()
            val y1 = cy + r * .17f * sin(a).toFloat()
            val x2 = cx + r * .57f * cos(a).toFloat()
            val y2 = cy + r * .57f * sin(a).toFloat()
            canvas.drawLine(x1, y1, x2, y2, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(135, 35, 21, 11)
                strokeWidth = r * .082f
                strokeCap = Paint.Cap.ROUND
            })
            canvas.drawLine(x1 - r * .009f, y1 - r * .009f, x2 - r * .009f, y2 - r * .009f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(92, 238, 188, 94)
                strokeWidth = r * .023f
                strokeCap = Paint.Cap.ROUND
            })
        }

        canvas.drawCircle(cx, cy, r * .22f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .06f, cy - r * .07f, r * .23f,
                intArrayOf(light, mid, dark), null, Shader.TileMode.CLAMP
            )
        })
        canvas.drawCircle(cx, cy, r * .075f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#21140B") })
        canvas.drawCircle(cx, cy, r * .040f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(215, 220, 163, 76) })

        val scratch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = r * .007f
            strokeCap = Paint.Cap.ROUND
        }
        for (i in 0 until 5) {
            val a = Math.toRadians((i * 61 + 19).toDouble())
            val sr = r * (.50f + (i % 3) * .06f)
            scratch.color = if (i % 2 == 0) Color.argb(38, 245, 207, 124) else Color.argb(44, 34, 20, 10)
            canvas.drawLine(
                cx + sr * cos(a).toFloat(), cy + sr * sin(a).toFloat(),
                cx + (sr + r * .12f) * cos(a + .05).toFloat(),
                cy + (sr + r * .12f) * sin(a + .05).toFloat(), scratch
            )
        }
        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas, card: RectF, day: DayZmanim) {
        val w = card.width(); val h = card.height()
        val grid = RectF(card.left + w * .045f, card.top + h * .382f, card.right - w * .045f, card.top + h * .738f)
        val radius = w * .027f
        canvas.drawRoundRect(grid, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(96, 35, 21, 11) })
        canvas.drawRoundRect(grid, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * .0042f
            color = Color.argb(225, 218, 161, 78)
        })
        val cw = grid.width() / 4f; val ch = grid.height() / 2f
        val sep = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(180, 147, 92, 38); strokeWidth = w * .0020f }
        for (c in 1 until 4) canvas.drawLine(grid.left + cw * c, grid.top, grid.left + cw * c, grid.bottom, sep)
        canvas.drawLine(grid.left, grid.top + ch, grid.right, grid.top + ch, sep)
        val cells = listOf(
            Cell("סוף זמן ק״ש", day.sofZmanShmaGra), Cell("זריחה", day.netzHachama), Cell("תפילה", day.sofZmanTefila), Cell("עלות השחר", day.alosHashachar),
            Cell("צאת הכוכבים", day.tzais), Cell("שקיעה", day.shkia), Cell("מנחה קטנה", day.minchaKetana), Cell("מנחה גדולה", day.minchaGedola)
        )
        cells.forEachIndexed { i, cell ->
            val col = i % 4; val row = i / 4
            val box = RectF(grid.left + col * cw, grid.top + row * ch, grid.left + (col + 1) * cw, grid.top + (row + 1) * ch)
            embossedRtl(canvas, cell.label, RectF(box.left + 3f, box.top + ch * .03f, box.right - 3f, box.top + ch * .50f), cw * .175f, true)
            val valuePaint = textPaint(cw * .250f, GOLD_LIGHT, true).apply {
                textAlign = Paint.Align.CENTER
                setShadowLayer(cw * .018f, 0f, cw * .012f, Color.argb(175, 0, 0, 0))
            }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - ch * .11f, valuePaint)
        }
    }

    private fun drawFooter(canvas: Canvas, card: RectF, day: DayZmanim) {
        val w = card.width(); val h = card.height()
        val top = card.top + h * .755f; val bottom = card.bottom - h * .045f; val mid = top + (bottom - top) * .49f
        drawBook(canvas, card.left + w * .22f, top + (mid - top) * .52f, w * .030f)
        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        embossedRtl(canvas, parsha, RectF(card.left + w * .29f, top, card.right - w * .06f, mid), w * .043f, false)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(170, 181, 123, 49); strokeWidth = w * .0014f }
        canvas.drawLine(card.left + w * .19f, mid, card.right - w * .08f, mid, linePaint)
        drawDiamond(canvas, card.centerX(), mid, w * .010f)
        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val bottomText = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            day.roshChodeshInDays == 0 && !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            isFriday -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
            else -> null
        }
        if (!bottomText.isNullOrBlank()) {
            drawCandles(canvas, card.left + w * .22f, mid + (bottom - mid) * .55f, w * .028f)
            embossedRtl(canvas, bottomText, RectF(card.left + w * .29f, mid, card.right - w * .06f, bottom), w * .037f, false)
        }
    }

    private fun hand(canvas: Canvas, cx: Float, cy: Float, len: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90).toDouble())
        canvas.drawLine(cx, cy, cx + len * cos(a).toFloat(), cy + len * sin(a).toFloat(), Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        })
    }

    private fun drawPin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT; style = Paint.Style.STROKE; strokeWidth = r * .30f }
        val path = Path().apply {
            moveTo(cx, cy + r * 1.45f)
            cubicTo(cx - r * 1.25f, cy + r * .20f, cx - r * 1.05f, cy - r * 1.15f, cx, cy - r * 1.15f)
            cubicTo(cx + r * 1.05f, cy - r * 1.15f, cx + r * 1.25f, cy + r * .20f, cx, cy + r * 1.45f)
        }
        canvas.drawPath(path, p)
        canvas.drawCircle(cx, cy - r * .28f, r * .34f, p)
    }

    private fun drawBook(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD_LIGHT; style = Paint.Style.STROKE; strokeWidth = r * .19f; strokeJoin = Paint.Join.ROUND
        }
        val path = Path().apply {
            moveTo(cx, cy - r * .48f); lineTo(cx - r, cy - r * .76f); lineTo(cx - r, cy + r * .58f); lineTo(cx, cy + r * .35f)
            moveTo(cx, cy - r * .48f); lineTo(cx + r, cy - r * .76f); lineTo(cx + r, cy + r * .58f); lineTo(cx, cy + r * .35f)
            moveTo(cx, cy - r * .48f); lineTo(cx, cy + r * .35f)
        }
        canvas.drawPath(path, p)
    }

    private fun drawCandles(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GOLD_LIGHT; style = Paint.Style.STROKE; strokeWidth = r * .17f; strokeCap = Paint.Cap.ROUND
        }
        for (dx in floatArrayOf(-r * .45f, r * .45f)) {
            canvas.drawRect(cx + dx - r * .16f, cy - r * .10f, cx + dx + r * .16f, cy + r * .72f, p)
            val flame = Path().apply {
                moveTo(cx + dx, cy - r * .93f)
                cubicTo(cx + dx - r * .28f, cy - r * .53f, cx + dx - r * .16f, cy - r * .28f, cx + dx, cy - r * .20f)
                cubicTo(cx + dx + r * .16f, cy - r * .28f, cx + dx + r * .28f, cy - r * .53f, cx + dx, cy - r * .93f)
            }
            canvas.drawPath(flame, p)
        }
    }

    private fun drawDiamond(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT; style = Paint.Style.STROKE; strokeWidth = r * .20f }
        val path = Path().apply { moveTo(cx, cy - r); lineTo(cx + r, cy); lineTo(cx, cy + r); lineTo(cx - r, cy); close() }
        canvas.drawPath(path, p)
    }

    private fun embossedRtl(canvas: Canvas, text: String, box: RectF, size: Float, bold: Boolean) {
        val shadowPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size; color = Color.argb(190, 56, 30, 10); typeface = if (bold) SERIF_BOLD else SERIF
        }
        drawRtl(canvas, text, box, shadowPaint, size * .025f, size * .035f)
        val highlightPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size; color = GOLD_HIGHLIGHT; typeface = if (bold) SERIF_BOLD else SERIF
        }
        drawRtl(canvas, text, box, highlightPaint, -size * .010f, -size * .012f)
        val mainPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size; color = GOLD_LIGHT; typeface = if (bold) SERIF_BOLD else SERIF
            setShadowLayer(size * .050f, 0f, size * .028f, Color.argb(175, 20, 10, 4))
        }
        drawRtl(canvas, text, box, mainPaint, 0f, 0f)
    }

    private fun drawRtl(canvas: Canvas, text: String, box: RectF, paint: TextPaint, dx: Float, dy: Float) {
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, box.width().toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setMaxLines(2)
            .build()
        canvas.save()
        canvas.translate(box.left + dx, box.top + (box.height() - layout.height) / 2f + dy)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) SERIF_BOLD else SERIF
    }
}
