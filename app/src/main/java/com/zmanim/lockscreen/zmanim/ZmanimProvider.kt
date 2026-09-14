package com.zmanim.lockscreen.zmanim

import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.util.GeoLocation
import com.zmanim.lockscreen.data.LocationConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class DayZmanim(
    val alosHashachar: Date?,
    val netzHachama: Date?,
    val sofZmanShmaGra: Date?,
    val chatzos: Date?,
    val minchaGedola: Date?,
    val plagHamincha: Date?,
    val shkia: Date?,
    val tzais: Date?,
    val hebrewDate: String
)

/**
 * Wraps the KosherJava zmanim library (https://github.com/KosherJava/zmanim) for one location.
 * NOTE: verify the exact method/constructor names against the pinned library version's javadoc
 * once this is opened in Android Studio - this was written from memory, not compiled here.
 */
class ZmanimProvider(private val location: LocationConfig) {

    private fun buildCalendar(forDate: Calendar): ComplexZmanimCalendar {
        val timeZone = TimeZone.getTimeZone(location.timeZoneId)
        val geoLocation = GeoLocation(
            location.name,
            location.latitude,
            location.longitude,
            location.elevation,
            timeZone
        )
        return ComplexZmanimCalendar(geoLocation).apply {
            calendar = forDate
        }
    }

    fun today(): DayZmanim {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(location.timeZoneId))
        val czc = buildCalendar(cal)

        val jewishCalendar = JewishCalendar(cal)
        val formatter = HebrewDateFormatter().apply { isHebrewFormat = true }

        return DayZmanim(
            alosHashachar = czc.alosHashachar,
            netzHachama = czc.sunrise,
            sofZmanShmaGra = czc.sofZmanShmaGRA,
            chatzos = czc.chatzos,
            minchaGedola = czc.minchaGedola,
            plagHamincha = czc.plagHamincha,
            shkia = czc.sunset,
            tzais = czc.tzais,
            hebrewDate = formatter.format(jewishCalendar)
        )
    }

    companion object {
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun formatTime(date: Date?): String = date?.let { timeFormat.format(it) } ?: "--:--"
    }
}
