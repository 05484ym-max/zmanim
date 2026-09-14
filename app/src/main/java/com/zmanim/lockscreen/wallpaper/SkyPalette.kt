package com.zmanim.lockscreen.wallpaper

import java.util.Date

/**
 * Picks a background gradient by where "now" falls relative to today's sunrise/sunset -
 * this is the piece that makes the wallpaper feel alive across the day. Swap these stops
 * for the vintage-photo palette once the art direction is finalized.
 */
object SkyPalette {
    val DAWN = intArrayOf(0xFF544B3E.toInt(), 0xFFC39F70.toInt(), 0xFFE9C891.toInt())
    val DAY = intArrayOf(0xFF2A6FA6.toInt(), 0xFF5B9BD0.toInt(), 0xFFBFE3F2.toInt())
    val DUSK = intArrayOf(0xFF3B2A52.toInt(), 0xFFA85C72.toInt(), 0xFFF2A65A.toInt())
    val NIGHT = intArrayOf(0xFF05060F.toInt(), 0xFF131A3D.toInt(), 0xFF251F4A.toInt())

    fun forTime(now: Date, sunrise: Date?, sunset: Date?): IntArray {
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
