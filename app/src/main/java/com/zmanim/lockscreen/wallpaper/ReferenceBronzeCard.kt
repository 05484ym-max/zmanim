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
    private val GOLD = Color.parseColor("#C38B34")
    private val GOLD_LIGHT = Color.parseColor("#F2D18A")
    private val GOLD_HIGHLIGHT = Color.parseColor("#FFE6A8")
    private val GOLD_DARK = Color.parseColor("#5E3917")
    private val BRONZE_TOP = Color.parseColor("#80562C")
    private val BRONZE_MID = Color.parseColor("#5B381D")
    private val BRONZE_BOTTOM = Color.parseColor("#321E10")
    private val IVORY = Color.parseColor("#F3DEAF")
    private val INK = Color.parseColor("#1F140C")
    private val RED = Color.parseColor("#D23826")
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
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(14, 0, 0, 0)
            setShadowLayer(card.width() * .014f, 0f, card.width() * .008f, Color.argb(105, 0, 0, 0))
        }
        canvas.drawRoundRect(RectF(card.left, card.top + 3f, card.right, card.bottom + 3f), radius, radius, shadow)
        shadow.clearShadowLayer()

        val base = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                card.left, card.top, card.right, card.bottom,
                intArrayOf(
                    Color.argb(185, Color.red(BRONZE_TOP), Color.green(BRONZE_TOP), Color.blue(BRONZE_TOP)),
                    Color.argb(178, Color.red(BRONZE_MID), Color.green(BRONZE_MID), Color.blue(BRONZE_MID)),
                    Color.argb(174, Color.red(BRONZE_BOTTOM), Color.green(BRONZE_BOTTOM), Color.blue(BRONZE_BOTTOM))
                ), floatArrayOf(0f, .46f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(card, radius, radius, base)

        canvas.save()
        val clip = Path().apply { addRoundRect(card, radius, radius, Path.Direction.CW) }
        canvas.clipPath(clip)
        val rnd = Random(1948L)
        val texture = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(560) {
            val x = card.left + rnd.nextFloat() * card.width()
            val y = card.top + rnd.nextFloat() * card.height()
            val rr = card.width() * (.00045f + rnd.nextFloat() * .0023f)
            texture.color = if (rnd.nextBoolean()) Color.argb(15, 247, 207, 125) else Color.argb(18, 25, 13, 7)
            canvas.drawOval(RectF(x - rr * 2.2f, y - rr, x + rr * 2.2f, y + rr), texture)
        }
        repeat(120) {
            val x = card.left + rnd.nextFloat() * card.width()
            val y = card.top + rnd.nextFloat() * card.height()
            texture.style = Paint.Style.STROKE
            texture.strokeWidth = card.width() * .0007f
            texture.color = if (rnd.nextBoolean()) Color.argb(16, 244, 204, 121) else Color.argb(16, 37, 18, 7)
            canvas.drawLine(x, y, x + card.width() * (.012f + rnd.nextFloat() * .045f), y + rnd.nextFloat() * 3f - 1.5f, texture)
        }
        canvas.restore()

        val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = card.width() * .014f
            shader = LinearGradient(card.left, card.top, card.right, card.bottom,
                intArrayOf(GOLD_HIGHLIGHT, GOLD, GOLD_DARK, GOLD_LIGHT), floatArrayOf(0f, .34f, .70f, 1f), Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(card, radius, radius, outer)

        val darkInset = card.width() * .0155f
        canvas.drawRoundRect(RectF(card.left + darkInset, card.top + darkInset, card.right - darkInset, card.bottom - darkInset),
            radius * .80f, radius * .80f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE; strokeWidth = card.width() * .0045f; color = Color.argb(230, 72, 40, 16)
            })

        val brightInset = card.width() * .023f
        canvas.drawRoundRect(RectF(card.left + brightInset, card.top + brightInset, card.right - brightInset, card.bottom - brightInset),
            radius * .68f, radius * .68f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE; strokeWidth = card.width() * .0022f; color = Color.argb(230, 236, 193, 106)
            })
    }

    private fun drawCorners(canvas: Canvas, card: RectF) {
        fun one(x: Float, y: Float, sx: Float, sy: Float) {
            val s = card.width() * .085f
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE; color = GOLD_LIGHT; strokeWidth = card.width() * .0042f
                strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
            }
            val path = Path().apply {
                moveTo(x, y + sy * s)
                cubicTo(x + sx * s * .08f, y + sy * s * .55f, x + sx * s * .26f, y + sy * s * .22f, x + sx * s, y)
                moveTo(x + sx * s * .10f, y + sy * s * .72f)
                cubicTo(x + sx * s * .43f, y + sy * s * .62f, x + sx * s * .48f, y + sy * s * .31f, x + sx * s * .23f, y + sy * s * .18f)
                moveTo(x + sx * s * .38f, y + sy * s * .52f)
                cubicTo(x + sx * s * .77f, y + sy * s * .47f, x + sx * s * .75f, y + sy * s * .17f, x + sx * s * .55f, y + sy * s * .10f)
                moveTo(x + sx * s * .20f, y + sy * s * .36f)
                cubicTo(x + sx * s * .03f, y + sy * s * .25f, x + sx * s * .07f, y + sy * s * .05f, x + sx * s * .31f, y + sy * s * .02f)
            }
            canvas.drawPath(path, p)
            canvas.drawCircle(x + sx * s * .34f, y + sy * s * .34f, card.width() * .007f, p)
        }
        val m = card.width() * .040f
        one(card.left + m, card.top + m, 1f, 1f); one(card.right - m, card.top + m, -1f, 1f)
        one(card.left + m, card.bottom - m, 1f, -1f); one(card.right - m, card.bottom - m, -1f, -1f)
    }

    private fun drawHeader(canvas: Canvas, card: RectF, day: DayZmanim, locationName: String) {
        val w = card.width(); val h = card.height()
        val right = RectF(card.left + w * .50f, card.top + h * .035f, card.right - w * .045f, card.top + h * .355f)
        embossedRtl(canvas, "זמני היום", RectF(right.left, right.top, right.right, right.top + right.height() * .28f), w * .074f, true)
        embossedRtl(canvas, locationName, RectF(right.left, right.top + right.height() * .30f, right.right - w * .06f, right.top + right.height() * .50f), w * .048f, false)
        drawPin(canvas, right.right - w * .026f, right.top + right.height() * .405f, w * .020f)
        embossedRtl(canvas, day.hebrewDate, RectF(right.left, right.top + right.height() * .54f, right.right, right.top + right.height() * .76f), w * .043f, true)
        val datePaint = textPaint(w * .034f, IVORY, false).apply {
            textAlign = Paint.Align.CENTER; setShadowLayer(w * .004f, 0f, w * .003f, Color.argb(185, 0, 0, 0))
        }
        canvas.drawText(day.gregorianDate, right.centerX(), right.top + right.height() * .97f, datePaint)
    }

    private fun drawClock(canvas: Canvas, card: RectF) {
        val w = card.width(); val h = card.height()
        val cx = card.left + w * .285f
        val cy = card.top + h * .205f
        val r = w * .196f

        canvas.drawCircle(cx, cy + r * .025f, r * 1.08f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(30, 0, 0, 0)
            setShadowLayer(r * .13f, 0f, r * .05f, Color.argb(140, 0, 0, 0))
        })
        val bezel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .25f, cy - r * .24f, r * 1.20f,
                intArrayOf(GOLD_HIGHLIGHT, GOLD, GOLD_DARK, Color.parseColor("#2A180C")),
                floatArrayOf(0f, .42f, .74f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r * 1.07f, bezel)
        canvas.drawCircle(cx, cy, r * .98f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#21150C") })

        // Reference-like warm dial first: brighter and cleaner so numerals stay crisp.
        canvas.drawCircle(cx, cy, r * .91f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .28f, cy - r * .30f, r,
                intArrayOf(Color.parseColor("#E7C984"), Color.parseColor("#C79A54"), Color.parseColor("#85582C")),
                floatArrayOf(0f, .58f, 1f), Shader.TileMode.CLAMP
            )
        })

        // The gear train is confined to the central movement area, away from the numeral ring.
        canvas.save()
        canvas.clipPath(Path().apply { addCircle(cx, cy, r * .50f, Path.Direction.CW) })
        val baseAngle = (System.currentTimeMillis() % 240000L).toFloat() / 240000f * 360f

        // Six physically-linked wheels. Adjacent gears rotate in opposite directions and
        // scale by tooth count, so the train looks synchronized instead of arbitrary.
        drawMeshedGear(canvas, cx - r * .25f, cy + r * .02f, r * .245f, 18, baseAngle, 0f,
            Color.parseColor("#B57A33"), Color.parseColor("#734B20"), Color.parseColor("#322013"))
        drawMeshedGear(canvas, cx + r * .10f, cy - r * .14f, r * .19f, 14, -baseAngle * (18f / 14f), 11f,
            Color.parseColor("#C18B3A"), Color.parseColor("#7B5122"), Color.parseColor("#362115"))
        drawMeshedGear(canvas, cx + r * .30f, cy + r * .09f, r * .145f, 10, baseAngle * (18f / 10f), 7f,
            Color.parseColor("#9E6428"), Color.parseColor("#66411B"), Color.parseColor("#2C1B10"))
        drawMeshedGear(canvas, cx + r * .10f, cy + r * .27f, r * .125f, 9, -baseAngle * (18f / 9f), 3f,
            Color.parseColor("#C79647"), Color.parseColor("#7A5327"), Color.parseColor("#342116"))
        drawMeshedGear(canvas, cx - r * .17f, cy - r * .25f, r * .11f, 8, -baseAngle * (18f / 8f), 18f,
            Color.parseColor("#8D5A25"), Color.parseColor("#5C3A19"), Color.parseColor("#2A190F"))
        drawMeshedGear(canvas, cx - r * .02f, cy + r * .02f, r * .085f, 7, baseAngle * (18f / 7f), 5f,
            Color.parseColor("#D0A45B"), Color.parseColor("#84602C"), Color.parseColor("#382417"))
        canvas.restore()

        // Thin parchment/glass veil visually embeds the gears in the dial instead of sitting on top.
        canvas.drawCircle(cx, cy, r * .90f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .20f, cy - r * .24f, r,
                intArrayOf(Color.argb(56, 255, 232, 177), Color.argb(40, 205, 160, 92), Color.argb(48, 78, 47, 24)),
                null, Shader.TileMode.CLAMP
            )
        })

        // Strong clean dial ring similar to the supplied reference.
        canvas.drawCircle(cx, cy, r * .865f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .024f
            color = Color.argb(235, 73, 43, 18)
        })
        canvas.drawCircle(cx, cy, r * .835f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .008f
            color = Color.argb(160, 244, 207, 123)
        })

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2B1C10")
            strokeCap = Paint.Cap.SQUARE
        }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val outer = r * .80f
            val inner = if (i % 5 == 0) r * .62f else r * .71f
            tick.strokeWidth = if (i % 5 == 0) r * .026f else r * .008f
            canvas.drawLine(
                cx + inner * cos(a).toFloat(), cy + inner * sin(a).toFloat(),
                cx + outer * cos(a).toFloat(), cy + outer * sin(a).toFloat(), tick
            )
        }

        // Crisp numeral pads keep 12/3/6/9 readable even over the mechanical center.
        val numeralFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(70, 239, 209, 143) }
        listOf(12 to 0, 3 to 90, 6 to 180, 9 to 270).forEach { (_, deg) ->
            val a = Math.toRadians((deg - 90).toDouble())
            val nx = cx + r * .53f * cos(a).toFloat()
            val ny = cy + r * .53f * sin(a).toFloat()
            canvas.drawCircle(nx, ny, r * .13f, numeralFill)
        }

        val num = textPaint(r * .285f, Color.parseColor("#25170D"), false).apply {
            textAlign = Paint.Align.CENTER
            alpha = 255
            setShadowLayer(r * .012f, 0f, r * .008f, Color.argb(80, 255, 232, 170))
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
        hand(canvas, cx, cy, r * .45f, hour * 30f, r * .064f, Color.parseColor("#16100B"))
        hand(canvas, cx, cy, r * .66f, min * 6f, r * .042f, Color.parseColor("#16100B"))
        hand(canvas, cx, cy, r * .73f, sec * 6f, r * .012f, RED)
        canvas.drawCircle(cx, cy, r * .060f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .02f, cy - r * .02f, r * .06f,
                intArrayOf(GOLD_HIGHLIGHT, GOLD, GOLD_DARK), null, Shader.TileMode.CLAMP
            )
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
            color = Color.argb(115, 15, 8, 5)
            setShadowLayer(r * .09f, r * .025f, r * .045f, Color.argb(170, 0, 0, 0))
        })
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .30f, cy - r * .32f, r * 1.08f,
                intArrayOf(light, mid, dark), floatArrayOf(0f, .58f, 1f), Shader.TileMode.CLAMP
            )
        })
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .042f
            color = Color.argb(220, 46, 27, 14)
        })

        canvas.drawCircle(cx, cy, r * .68f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .030f
            color = Color.argb(105, 245, 204, 112)
        })

        // Five cut-outs and forged spokes create a watch-movement look instead of a flat icon.
        val holes = 5
        for (i in 0 until holes) {
            val a = Math.toRadians((i * 360.0 / holes) - 90.0)
            val hx = cx + r * .43f * cos(a).toFloat()
            val hy = cy + r * .43f * sin(a).toFloat()
            canvas.drawCircle(hx, hy, r * .13f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    hx - r * .03f, hy - r * .03f, r * .15f,
                    intArrayOf(Color.parseColor("#181009"), Color.parseColor("#442B16")), null, Shader.TileMode.CLAMP
                )
            })
            canvas.drawCircle(hx, hy, r * .13f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = r * .018f
                color = Color.argb(125, 222, 166, 80)
            })
        }

        for (i in 0 until holes) {
            val a = Math.toRadians((i * 360.0 / holes) - 90.0)
            val x1 = cx + r * .17f * cos(a).toFloat()
            val y1 = cy + r * .17f * sin(a).toFloat()
            val x2 = cx + r * .57f * cos(a).toFloat()
            val y2 = cy + r * .57f * sin(a).toFloat()
            canvas.drawLine(x1, y1, x2, y2, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(145, 35, 21, 11)
                strokeWidth = r * .085f
                strokeCap = Paint.Cap.ROUND
            })
            canvas.drawLine(x1 - r * .009f, y1 - r * .009f, x2 - r * .009f, y2 - r * .009f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(105, 238, 188, 94)
                strokeWidth = r * .024f
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
        canvas.drawCircle(cx, cy, r * .040f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(220, 220, 163, 76) })

        val scratch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = r * .008f
            strokeCap = Paint.Cap.ROUND
        }
        for (i in 0 until 6) {
            val a = Math.toRadians((i * 53 + 17).toDouble())
            val sr = r * (.50f + (i % 3) * .06f)
            scratch.color = if (i % 2 == 0) Color.argb(48, 245, 207, 124) else Color.argb(55, 34, 20, 10)
            canvas.drawLine(
                cx + sr * cos(a).toFloat(), cy + sr * sin(a).toFloat(),
                cx + (sr + r * .13f) * cos(a + .05).toFloat(),
                cy + (sr + r * .13f) * sin(a + .05).toFloat(), scratch
            )
        }
        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas, card: RectF, day: DayZmanim) {
        val w = card.width(); val h = card.height()
        val grid = RectF(card.left + w * .045f, card.top + h * .382f, card.right - w * .045f, card.top + h * .738f)
        val radius = w * .027f
        canvas.drawRoundRect(grid, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(132, 35, 21, 11) })
        canvas.drawRoundRect(grid, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = w * .0042f; color = Color.argb(235, 218, 161, 78)
        })
        val cw = grid.width() / 4f; val ch = grid.height() / 2f
        val sep = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(210, 147, 92, 38); strokeWidth = w * .0020f }
        for (c in 1 until 4) canvas.drawLine(grid.left + cw * c, grid.top, grid.left + cw * c, grid.bottom, sep)
        canvas.drawLine(grid.left, grid.top + ch, grid.right, grid.top + ch, sep)
        val cells = listOf(
            Cell("סוף זמן ק״ש", day.sofZmanShmaGra), Cell("זריחה", day.netzHachama), Cell("תפילה", day.sofZmanTefila), Cell("עלות השחר", day.alosHashachar),
            Cell("צאת הכוכבים", day.tzais), Cell("שקיעה", day.shkia), Cell("מנחה קטנה", day.minchaKetana), Cell("מנחה גדולה", day.minchaGedola))
        cells.forEachIndexed { i, cell ->
            val col = i % 4; val row = i / 4
            val box = RectF(grid.left + col * cw, grid.top + row * ch, grid.left + (col + 1) * cw, grid.top + (row + 1) * ch)
            embossedRtl(canvas, cell.label, RectF(box.left + 3f, box.top + ch * .03f, box.right - 3f, box.top + ch * .50f), cw * .175f, true)
            val valuePaint = textPaint(cw * .250f, GOLD_LIGHT, true).apply { textAlign = Paint.Align.CENTER; setShadowLayer(cw * .018f, 0f, cw * .012f, Color.argb(190, 0, 0, 0)) }
            canvas.drawText(ZmanimProvider.formatTime(cell.value), box.centerX(), box.bottom - ch * .11f, valuePaint)
        }
    }

    private fun drawFooter(canvas: Canvas, card: RectF, day: DayZmanim) {
        val w = card.width(); val h = card.height()
        val top = card.top + h * .755f; val bottom = card.bottom - h * .045f; val mid = top + (bottom - top) * .49f
        drawBook(canvas, card.left + w * .22f, top + (mid - top) * .52f, w * .030f)
        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        embossedRtl(canvas, parsha, RectF(card.left + w * .29f, top, card.right - w * .06f, mid), w * .043f, false)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(190, 181, 123, 49); strokeWidth = w * .0014f }
        canvas.drawLine(card.left + w * .19f, mid, card.right - w * .08f, mid, linePaint); drawDiamond(canvas, card.centerX(), mid, w * .010f)
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
            this.color = color; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND
        })
    }

    private fun drawPin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT; style = Paint.Style.STROKE; strokeWidth = r * .30f }
        val path = Path().apply {
            moveTo(cx, cy + r * 1.45f)
            cubicTo(cx - r * 1.25f, cy + r * .20f, cx - r * 1.05f, cy - r * 1.15f, cx, cy - r * 1.15f)
            cubicTo(cx + r * 1.05f, cy - r * 1.15f, cx + r * 1.25f, cy + r * .20f, cx, cy + r * 1.45f)
        }
        canvas.drawPath(path, p); canvas.drawCircle(cx, cy - r * .28f, r * .34f, p)
    }

    private fun drawBook(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT; style = Paint.Style.STROKE; strokeWidth = r * .19f; strokeJoin = Paint.Join.ROUND }
        val path = Path().apply {
            moveTo(cx, cy - r * .48f); lineTo(cx - r, cy - r * .76f); lineTo(cx - r, cy + r * .58f); lineTo(cx, cy + r * .35f)
            moveTo(cx, cy - r * .48f); lineTo(cx + r, cy - r * .76f); lineTo(cx + r, cy + r * .58f); lineTo(cx, cy + r * .35f)
            moveTo(cx, cy - r * .48f); lineTo(cx, cy + r * .35f)
        }
        canvas.drawPath(path, p)
    }

    private fun drawCandles(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD_LIGHT; style = Paint.Style.STROKE; strokeWidth = r * .17f; strokeCap = Paint.Cap.ROUND }
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
        val shadowPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; color = Color.argb(210, 56, 30, 10); typeface = if (bold) SERIF_BOLD else SERIF }
        drawRtl(canvas, text, box, shadowPaint, size * .025f, size * .035f)
        val highlightPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; color = GOLD_HIGHLIGHT; typeface = if (bold) SERIF_BOLD else SERIF }
        drawRtl(canvas, text, box, highlightPaint, -size * .010f, -size * .012f)
        val mainPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size; color = GOLD_LIGHT; typeface = if (bold) SERIF_BOLD else SERIF
            setShadowLayer(size * .055f, 0f, size * .030f, Color.argb(195, 20, 10, 4))
        }
        drawRtl(canvas, text, box, mainPaint, 0f, 0f)
    }

    private fun drawRtl(canvas: Canvas, text: String, box: RectF, paint: TextPaint, dx: Float, dy: Float) {
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, box.width().toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_CENTER).setTextDirection(TextDirectionHeuristics.RTL).setIncludePad(false).setMaxLines(2).build()
        canvas.save(); canvas.translate(box.left + dx, box.top + (box.height() - layout.height) / 2f + dy); layout.draw(canvas); canvas.restore()
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size; this.color = color; typeface = if (bold) SERIF_BOLD else SERIF
    }
}
