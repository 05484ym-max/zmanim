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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Final visual refinement drawn on top of ReferenceBronzeCard.
 * Keeps the transparent bronze plaque, but brings the clock, typography and
 * mechanical movement closer to the supplied antique reference.
 */
object ReferenceBronzeCardV3 {
    private val BRASS_LIGHT = Color.parseColor("#F1D28E")
    private val BRASS = Color.parseColor("#B87A31")
    private val BRASS_MID = Color.parseColor("#7D4E20")
    private val BRASS_DARK = Color.parseColor("#362014")
    private val PARCHMENT = Color.parseColor("#E8C985")
    private val INK = Color.parseColor("#21150D")
    private val RED = Color.parseColor("#D52C20")
    private val SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val SERIF_BOLD = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    fun draw(canvas: Canvas, width: Int, height: Int, day: DayZmanim, locationName: String) {
        ReferenceBronzeCard.draw(canvas, width, height, day, locationName)

        val w = width.toFloat()
        val h = height.toFloat()
        val cardW = w * .79f
        val cardH = h * .50f
        val top = h * .305f
        val card = RectF((w - cardW) / 2f, top, (w + cardW) / 2f, top + cardH)

        drawReferencePatina(canvas, card)
        drawRefinedClock(canvas, card)
        drawRefinedHeader(canvas, card, day, locationName)
        drawRefinedGridTypography(canvas, card, day)
        drawRefinedFooterTypography(canvas, card, day)
    }

    private fun drawReferencePatina(canvas: Canvas, card: RectF) {
        val glaze = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                card.left, card.top, card.right, card.bottom,
                intArrayOf(
                    Color.argb(12, 255, 226, 159),
                    Color.argb(4, 101, 56, 24),
                    Color.argb(16, 38, 21, 11)
                ),
                floatArrayOf(0f, .48f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(card, card.width() * .040f, card.width() * .040f, glaze)

        // A few restrained hairline marks give the plaque the worn cast-metal feel
        // of the reference without hiding the wallpaper underneath.
        val scratch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = card.width() * .00055f
        }
        for (i in 0 until 24) {
            val fx = ((i * 37) % 101) / 100f
            val fy = ((i * 61 + 13) % 97) / 100f
            val x = card.left + card.width() * (.06f + fx * .88f)
            val y = card.top + card.height() * (.06f + fy * .88f)
            scratch.color = if (i % 2 == 0) Color.argb(18, 246, 206, 122) else Color.argb(20, 45, 24, 12)
            canvas.drawLine(x, y, x + card.width() * (.018f + (i % 4) * .006f), y + (i % 3 - 1) * card.width() * .0015f, scratch)
        }
    }

    private fun drawRefinedClock(canvas: Canvas, card: RectF) {
        val w = card.width()
        val h = card.height()
        val cx = card.left + w * .285f
        val cy = card.top + h * .205f
        val r = w * .196f

        // Deep but narrow shadow, like a separate brass clock fitted into the plaque.
        canvas.drawCircle(cx, cy + r * .035f, r * 1.085f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(18, 0, 0, 0)
            setShadowLayer(r * .12f, 0f, r * .055f, Color.argb(145, 0, 0, 0))
        })

        canvas.drawCircle(cx, cy, r * 1.07f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .30f, cy - r * .30f, r * 1.25f,
                intArrayOf(Color.parseColor("#F5D995"), Color.parseColor("#BB7C30"), Color.parseColor("#704319"), Color.parseColor("#25150B")),
                floatArrayOf(0f, .34f, .72f, 1f),
                Shader.TileMode.CLAMP
            )
        })
        canvas.drawCircle(cx, cy, r * .985f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(238, 34, 20, 11) })

        // Clean parchment dial, brighter and flatter like the supplied reference.
        canvas.drawCircle(cx, cy, r * .915f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .28f, cy - r * .32f, r * 1.04f,
                intArrayOf(
                    Color.argb(248, 243, 219, 166),
                    Color.argb(244, 225, 190, 127),
                    Color.argb(238, 190, 139, 74)
                ),
                floatArrayOf(0f, .62f, 1f),
                Shader.TileMode.CLAMP
            )
        })

        // The exposed movement is deliberately smaller than the numeral ring.
        // A single movement plate ties the gears together visually so it reads as
        // one mechanism instead of unrelated wheels.
        canvas.save()
        canvas.clipPath(Path().apply { addCircle(cx, cy, r * .47f, Path.Direction.CW) })
        canvas.drawCircle(cx, cy, r * .455f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .16f, cy - r * .18f, r * .52f,
                intArrayOf(Color.argb(178, 96, 60, 28), Color.argb(190, 48, 29, 16), Color.argb(208, 26, 16, 10)),
                null,
                Shader.TileMode.CLAMP
            )
        })

        drawMovementBridge(canvas, cx, cy, r)

        val base = (System.currentTimeMillis() % 420000L).toFloat() / 420000f * 360f
        val gears = listOf(
            GearSpec(-.18f, .04f, .18f, 18,  base,                   BRASS_LIGHT, BRASS, BRASS_DARK),
            GearSpec(.10f, -.10f, .13f, 13, -base * (18f / 13f),   Color.parseColor("#E2B866"), BRASS_MID, BRASS_DARK),
            GearSpec(.292f, .045f, .10f, 10, base * (18f / 10f),   Color.parseColor("#C98D3C"), Color.parseColor("#79491C"), BRASS_DARK),
            GearSpec(.225f, .225f, .085f, 9, -base * (18f / 9f),   Color.parseColor("#D8A64F"), Color.parseColor("#80501F"), BRASS_DARK),
            GearSpec(.035f, .255f, .10f, 10, base * (18f / 10f),   Color.parseColor("#C79044"), Color.parseColor("#75471B"), BRASS_DARK),
            GearSpec(-.17f, .21f, .09f, 9, -base * (18f / 9f),     Color.parseColor("#D6A257"), Color.parseColor("#7C4D20"), BRASS_DARK),
            GearSpec(-.37f, -.07f, .08f, 8, -base * (18f / 8f),    Color.parseColor("#B9782F"), Color.parseColor("#6E421A"), BRASS_DARK)
        )
        gears.forEachIndexed { index, g ->
            drawWatchGear(
                canvas,
                cx + r * g.x,
                cy + r * g.y,
                r * g.radius,
                g.teeth,
                g.rotation + if (index % 2 == 0) 0f else 180f / g.teeth,
                g.light,
                g.mid,
                g.dark
            )
        }

        drawMovementScrew(canvas, cx - r * .34f, cy + r * .16f, r * .045f, 22f)
        drawMovementScrew(canvas, cx + r * .31f, cy - r * .20f, r * .043f, -31f)
        drawMovementScrew(canvas, cx + r * .26f, cy + r * .30f, r * .040f, 65f)
        canvas.restore()

        // A very thin warm crystal veil keeps the mechanism embedded in the dial.
        canvas.drawCircle(cx, cy, r * .905f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .22f, cy - r * .26f, r,
                intArrayOf(Color.argb(38, 255, 244, 208), Color.argb(18, 230, 196, 135), Color.argb(30, 91, 54, 26)),
                null,
                Shader.TileMode.CLAMP
            )
        })

        canvas.drawCircle(cx, cy, r * .868f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .020f
            color = Color.argb(245, 72, 42, 17)
        })
        canvas.drawCircle(cx, cy, r * .842f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .0065f
            color = Color.argb(175, 248, 218, 151)
        })

        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#20150D")
            strokeCap = Paint.Cap.SQUARE
        }
        for (i in 0 until 60) {
            val a = Math.toRadians((i * 6 - 90).toDouble())
            val outer = r * .80f
            val inner = if (i % 5 == 0) r * .62f else r * .715f
            tick.strokeWidth = if (i % 5 == 0) r * .026f else r * .0075f
            canvas.drawLine(
                cx + inner * cos(a).toFloat(),
                cy + inner * sin(a).toFloat(),
                cx + outer * cos(a).toFloat(),
                cy + outer * sin(a).toFloat(),
                tick
            )
        }

        // No pale discs behind the numbers: the reference has crisp numerals directly on the dial.
        val num = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = r * .305f
            color = Color.parseColor("#21150D")
            typeface = SERIF_BOLD
            textAlign = Paint.Align.CENTER
            textScaleX = .95f
            setShadowLayer(r * .008f, 0f, r * .006f, Color.argb(60, 255, 244, 210))
        }
        listOf(12 to 0, 3 to 90, 6 to 180, 9 to 270).forEach { (n, deg) ->
            val a = Math.toRadians((deg - 90).toDouble())
            canvas.drawText(
                n.toString(),
                cx + r * .55f * cos(a).toFloat(),
                cy + r * .55f * sin(a).toFloat() + num.textSize * .34f,
                num
            )
        }

        val now = Calendar.getInstance()
        val sec = now.get(Calendar.SECOND)
        val min = now.get(Calendar.MINUTE) + sec / 60f
        val hour = now.get(Calendar.HOUR) + min / 60f
        hand(canvas, cx, cy, r * .46f, hour * 30f, r * .058f, Color.parseColor("#15100C"))
        hand(canvas, cx, cy, r * .67f, min * 6f, r * .037f, Color.parseColor("#15100C"))
        hand(canvas, cx, cy, r * .74f, sec * 6f, r * .011f, RED)

        canvas.drawCircle(cx, cy, r * .061f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .02f, cy - r * .025f, r * .065f,
                intArrayOf(Color.parseColor("#F6D78B"), BRASS, BRASS_DARK),
                null,
                Shader.TileMode.CLAMP
            )
        })
        canvas.drawCircle(cx, cy, r * .024f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK })
    }

    private fun drawMovementBridge(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val bridge = Path().apply {
            moveTo(cx - r * .40f, cy - r * .02f)
            cubicTo(cx - r * .22f, cy - r * .33f, cx + r * .06f, cy - r * .34f, cx + r * .34f, cy - r * .12f)
            cubicTo(cx + r * .18f, cy - r * .02f, cx + r * .18f, cy + r * .14f, cx + r * .34f, cy + r * .31f)
            cubicTo(cx + r * .06f, cy + r * .20f, cx - r * .14f, cy + r * .30f, cx - r * .38f, cy + r * .18f)
            close()
        }
        canvas.drawPath(bridge, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx - r * .35f, cy - r * .25f, cx + r * .35f, cy + r * .28f,
                intArrayOf(Color.argb(115, 188, 127, 53), Color.argb(88, 102, 61, 24), Color.argb(116, 55, 31, 15)),
                null,
                Shader.TileMode.CLAMP
            )
        })
        canvas.drawPath(bridge, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .018f
            color = Color.argb(125, 34, 20, 11)
        })
    }

    private fun drawWatchGear(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        teeth: Int,
        rotation: Float,
        light: Int,
        mid: Int,
        dark: Int
    ) {
        canvas.save()
        canvas.rotate(rotation, cx, cy)

        val teethPath = Path()
        val steps = teeth * 12
        for (i in 0 until steps) {
            val a = Math.toRadians((i * 360f / steps - 90f).toDouble())
            val rr = when (i % 12) {
                0, 11 -> r * .79f
                1, 10 -> r * .83f
                2, 9 -> r * .90f
                3, 4, 7, 8 -> r
                else -> r * .965f
            }
            val x = cx + rr * cos(a).toFloat()
            val y = cy + rr * sin(a).toFloat()
            if (i == 0) teethPath.moveTo(x, y) else teethPath.lineTo(x, y)
        }
        teethPath.close()

        canvas.drawPath(teethPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 0, 0, 0)
            setShadowLayer(r * .085f, r * .020f, r * .038f, Color.argb(175, 0, 0, 0))
        })
        canvas.drawPath(teethPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .32f, cy - r * .34f, r * 1.12f,
                intArrayOf(light, mid, dark),
                floatArrayOf(0f, .56f, 1f),
                Shader.TileMode.CLAMP
            )
        })
        canvas.drawPath(teethPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .035f
            color = Color.argb(210, 49, 28, 13)
        })

        // Skeletonized center: dark aperture plus a real wheel rim and spokes.
        canvas.drawCircle(cx, cy, r * .60f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .08f, cy - r * .09f, r * .62f,
                intArrayOf(Color.parseColor("#2A1A0E"), Color.parseColor("#120C08")),
                null,
                Shader.TileMode.CLAMP
            )
        })
        canvas.drawCircle(cx, cy, r * .67f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * .115f
            shader = RadialGradient(
                cx - r * .18f, cy - r * .20f, r * .80f,
                intArrayOf(light, mid, dark),
                null,
                Shader.TileMode.CLAMP
            )
        })

        val spokeCount = if (teeth >= 13) 5 else 4
        for (i in 0 until spokeCount) {
            val a = Math.toRadians((i * 360.0 / spokeCount) - 90.0)
            val x1 = cx + r * .20f * cos(a).toFloat()
            val y1 = cy + r * .20f * sin(a).toFloat()
            val x2 = cx + r * .60f * cos(a).toFloat()
            val y2 = cy + r * .60f * sin(a).toFloat()
            canvas.drawLine(x1, y1, x2, y2, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                strokeWidth = r * .105f
                strokeCap = Paint.Cap.ROUND
                color = mid
            })
            canvas.drawLine(x1 - r * .008f, y1 - r * .010f, x2 - r * .008f, y2 - r * .010f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                strokeWidth = r * .028f
                strokeCap = Paint.Cap.ROUND
                color = Color.argb(130, 244, 205, 122)
            })
        }

        canvas.drawCircle(cx, cy, r * .225f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .06f, cy - r * .07f, r * .24f,
                intArrayOf(light, mid, dark),
                null,
                Shader.TileMode.CLAMP
            )
        })
        canvas.drawCircle(cx, cy, r * .085f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#26170D") })
        canvas.drawCircle(cx, cy, r * .048f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D4A455") })

        // Screw slot makes the hub read as a mechanical part rather than an icon.
        val slot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(210, 58, 33, 16)
            strokeWidth = r * .025f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx - r * .035f, cy, cx + r * .035f, cy, slot)

        canvas.restore()
    }

    private fun drawMovementScrew(canvas: Canvas, cx: Float, cy: Float, r: Float, angle: Float) {
        canvas.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * .30f, cy - r * .30f, r,
                intArrayOf(Color.parseColor("#E3C17C"), Color.parseColor("#7C5A31"), Color.parseColor("#2D2118")),
                null,
                Shader.TileMode.CLAMP
            )
        })
        val a = Math.toRadians(angle.toDouble())
        val dx = cos(a).toFloat() * r * .60f
        val dy = sin(a).toFloat() * r * .60f
        canvas.drawLine(cx - dx, cy - dy, cx + dx, cy + dy, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#332114")
            strokeWidth = r * .22f
            strokeCap = Paint.Cap.ROUND
        })
    }

    private fun drawRefinedHeader(canvas: Canvas, card: RectF, day: DayZmanim, locationName: String) {
        val w = card.width()
        val h = card.height()
        val right = RectF(card.left + w * .50f, card.top + h * .035f, card.right - w * .045f, card.top + h * .355f)
        engravedRtl(canvas, "זמני היום", RectF(right.left, right.top, right.right, right.top + right.height() * .28f), w * .078f, true)
        engravedRtl(canvas, locationName, RectF(right.left, right.top + right.height() * .30f, right.right - w * .060f, right.top + right.height() * .50f), w * .050f, false)
        engravedRtl(canvas, day.hebrewDate, RectF(right.left, right.top + right.height() * .54f, right.right, right.top + right.height() * .76f), w * .045f, true)

        val date = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = w * .035f
            typeface = SERIF
            color = Color.parseColor("#F2E0B8")
            textAlign = Paint.Align.CENTER
            textScaleX = .96f
            setShadowLayer(w * .0032f, 0f, w * .0022f, Color.argb(175, 0, 0, 0))
        }
        canvas.drawText(day.gregorianDate, right.centerX(), right.top + right.height() * .97f, date)
    }

    private fun drawRefinedGridTypography(canvas: Canvas, card: RectF, day: DayZmanim) {
        val w = card.width()
        val h = card.height()
        val grid = RectF(card.left + w * .045f, card.top + h * .382f, card.right - w * .045f, card.top + h * .738f)
        val cw = grid.width() / 4f
        val ch = grid.height() / 2f
        val cells = listOf(
            Pair("סוף זמן ק״ש", day.sofZmanShmaGra),
            Pair("זריחה", day.netzHachama),
            Pair("תפילה", day.sofZmanTefila),
            Pair("עלות השחר", day.alosHashachar),
            Pair("צאת הכוכבים", day.tzais),
            Pair("שקיעה", day.shkia),
            Pair("מנחה קטנה", day.minchaKetana),
            Pair("מנחה גדולה", day.minchaGedola)
        )
        cells.forEachIndexed { i, cell ->
            val col = i % 4
            val row = i / 4
            val box = RectF(grid.left + col * cw, grid.top + row * ch, grid.left + (col + 1) * cw, grid.top + (row + 1) * ch)
            engravedRtl(canvas, cell.first, RectF(box.left + 3f, box.top + ch * .03f, box.right - 3f, box.top + ch * .50f), cw * .170f, true)
            val value = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = cw * .245f
                typeface = SERIF_BOLD
                color = Color.parseColor("#F0D18D")
                textAlign = Paint.Align.CENTER
                textScaleX = .96f
                setShadowLayer(cw * .014f, 0f, cw * .010f, Color.argb(185, 0, 0, 0))
            }
            canvas.drawText(ZmanimProvider.formatTime(cell.second), box.centerX(), box.bottom - ch * .11f, value)
        }
    }

    private fun drawRefinedFooterTypography(canvas: Canvas, card: RectF, day: DayZmanim) {
        val w = card.width()
        val h = card.height()
        val top = card.top + h * .755f
        val bottom = card.bottom - h * .045f
        val mid = top + (bottom - top) * .49f
        val parsha = day.parshaLabel?.let { "פרשת השבוע: $it" } ?: "פרשת השבוע"
        engravedRtl(canvas, parsha, RectF(card.left + w * .29f, top, card.right - w * .06f, mid), w * .043f, false)

        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val bottomText = when {
            !day.hilulaLabel.isNullOrBlank() -> "הילולת היום: ${day.hilulaLabel}"
            day.roshChodeshInDays == 0 && !day.roshChodeshLabel.isNullOrBlank() -> day.roshChodeshLabel
            isFriday -> "כניסת שבת: ${ZmanimProvider.formatTime(day.candleLighting)}"
            else -> null
        }
        if (!bottomText.isNullOrBlank()) {
            engravedRtl(canvas, bottomText, RectF(card.left + w * .29f, mid, card.right - w * .06f, bottom), w * .037f, false)
        }
    }

    private fun engravedRtl(canvas: Canvas, text: String, box: RectF, size: Float, bold: Boolean) {
        val typeface = if (bold) SERIF_BOLD else SERIF

        val deep = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.typeface = typeface
            color = Color.argb(230, 48, 27, 12)
            style = Paint.Style.STROKE
            strokeWidth = size * .090f
            strokeJoin = Paint.Join.ROUND
            textScaleX = .97f
        }
        drawRtl(canvas, text, box, deep, size * .018f, size * .026f)

        val highlight = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.typeface = typeface
            color = Color.argb(225, 255, 232, 174)
            textScaleX = .97f
        }
        drawRtl(canvas, text, box, highlight, -size * .014f, -size * .014f)

        val main = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.typeface = typeface
            color = Color.parseColor("#E8C77D")
            textScaleX = .97f
            setShadowLayer(size * .028f, 0f, size * .025f, Color.argb(155, 0, 0, 0))
        }
        drawRtl(canvas, text, box, main, 0f, 0f)
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

    private fun hand(canvas: Canvas, cx: Float, cy: Float, len: Float, deg: Float, stroke: Float, color: Int) {
        val a = Math.toRadians((deg - 90).toDouble())
        canvas.drawLine(
            cx,
            cy,
            cx + len * cos(a).toFloat(),
            cy + len * sin(a).toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                strokeWidth = stroke
                strokeCap = Paint.Cap.ROUND
            }
        )
    }

    private data class GearSpec(
        val x: Float,
        val y: Float,
        val radius: Float,
        val teeth: Int,
        val rotation: Float,
        val light: Int,
        val mid: Int,
        val dark: Int
    )
}
