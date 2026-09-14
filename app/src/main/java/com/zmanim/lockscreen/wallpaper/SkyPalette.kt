package com.zmanim.lockscreen.wallpaper

import android.graphics.Color
import java.util.Date

/**
 * Picks a full vintage color palette (sky gradient, skyline silhouette, dome, cypress,
 * olive branch, duotone wash) by where "now" falls relative to today's sunrise/sunset.
 * This is what makes the wallpaper feel alive across the day.
 */
object SkyPalette {

    data class Palette(
        val sky: IntArray,
        val hill: Int,
        val silhouette: Int,
        val domeHighlight: Int,
        val domeShadow: Int,
        val cypress: Int,
        val olive: Int,
        val duotoneTop: Int,
        val duotoneBottom: Int
    )

    private val DAWN = Palette(
        sky = intArrayOf(Color.parseColor("#544B3E"), Color.parseColor("#C39F70"), Color.parseColor("#E9C891")),
        hill = Color.parseColor("#8C7857"),
        silhouette = Color.parseColor("#3A2C1A"),
        domeHighlight = Color.parseColor("#EAC674"),
        domeShadow = Color.parseColor("#A97A22"),
        cypress = Color.parseColor("#241D10"),
        olive = Color.argb(150, 52, 46, 22),
        duotoneTop = Color.argb(60, 110, 70, 30),
        duotoneBottom = Color.argb(90, 45, 22, 8)
    )

    private val DAY = Palette(
        sky = intArrayOf(Color.parseColor("#2A6FA6"), Color.parseColor("#5B9BD0"), Color.parseColor("#BFE3F2")),
        hill = Color.parseColor("#7C93A0"),
        silhouette = Color.parseColor("#2D3A40"),
        domeHighlight = Color.parseColor("#F4D98A"),
        domeShadow = Color.parseColor("#B8891F"),
        cypress = Color.parseColor("#20301E"),
        olive = Color.argb(150, 40, 60, 30),
        duotoneTop = Color.argb(30, 60, 90, 110),
        duotoneBottom = Color.argb(50, 20, 40, 55)
    )

    private val DUSK = Palette(
        sky = intArrayOf(Color.parseColor("#3B2A52"), Color.parseColor("#A85C72"), Color.parseColor("#F2A65A")),
        hill = Color.parseColor("#6A5570"),
        silhouette = Color.parseColor("#2A1E30"),
        domeHighlight = Color.parseColor("#E8B25F"),
        domeShadow = Color.parseColor("#8C4A2E"),
        cypress = Color.parseColor("#1C1420"),
        olive = Color.argb(150, 45, 30, 40),
        duotoneTop = Color.argb(70, 120, 50, 60),
        duotoneBottom = Color.argb(100, 40, 15, 25)
    )

    private val NIGHT = Palette(
        sky = intArrayOf(Color.parseColor("#05060F"), Color.parseColor("#131A3D"), Color.parseColor("#251F4A")),
        hill = Color.parseColor("#181C34"),
        silhouette = Color.parseColor("#0B0C16"),
        domeHighlight = Color.parseColor("#7C6A9A"),
        domeShadow = Color.parseColor("#3A2E52"),
        cypress = Color.parseColor("#0A0A10"),
        olive = Color.argb(150, 18, 18, 30),
        duotoneTop = Color.argb(70, 20, 20, 50),
        duotoneBottom = Color.argb(110, 8, 8, 22)
    )

    fun forTime(now: Date, sunrise: Date?, sunset: Date?): Palette {
        if (sunrise == null || sunset == null) return DAY
        val hourMs = 60 * 60 * 1000L
        return when {
            now.time < sunrise.time - hourMs -> NIGHT
            now.time < sunrise.time + hourMs -> DAWN
            now.time < sunset.time - hourMs -> DAY
            now.time < sunset.time + hourMs -> DUSK
            else -> NIGHT
        }
    }
}
