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

data class DateStripDay(
    val hebrewLetter: String,
    val dayOfMonth: Int,
    val isToday: Boolean
)

data class DayZmanim(
    val alosHashachar: Date?,
    val netzHachama: Date?,
    val sofZmanShmaGra: Date?,
    val chatzos: Date?,
    val minchaGedola: Date?,
    val plagHamincha: Date?,
    val shkia: Date?,
    val tzais: Date?,
    val hebrewDate: String,
    /** Candle lighting for the upcoming Friday (today's, if today is Friday) - always present. */
    val candleLighting: Date?,
    val roshChodeshLabel: String?,
    /** Days until the next Rosh Chodesh; 0 = today. */
    val roshChodeshInDays: Int?,
    /** 7 days centered on today (3 before, today, 3 after), for the date-strip UI. */
    val dateStrip: List<DateStripDay>
)

/**
 * Wraps the KosherJava zmanim library (https://github.com/KosherJava/zmanim) for one location.
 * NOTE: verify the exact method/constructor names against the pinned library version's javadoc
 * once this is opened in Android Studio - written from memory, checked only by CI compilation.
 */
class ZmanimProvider(private val location: LocationConfig) {

    private val timeZone: TimeZone get() = TimeZone.getTimeZone(location.timeZoneId)

    private fun buildCalendar(forDate: Calendar): ComplexZmanimCalendar {
        val geoLocation = GeoLocation(
            location.name,
            location.latitude,
            location.longitude,
            location.elevation,
            timeZone
        )
        return ComplexZmanimCalendar(geoLocation).apply { calendar = forDate }
    }

    fun today(): DayZmanim {
        val cal = Calendar.getInstance(timeZone)
        val czc = buildCalendar(cal)

        val jewishCalendar = JewishCalendar(cal)
        val formatter = HebrewDateFormatter().apply { isHebrewFormat = true }

        val rosh = findNextRoshChodesh(cal, formatter)

        return DayZmanim(
            alosHashachar = czc.alosHashachar,
            netzHachama = czc.sunrise,
            sofZmanShmaGra = czc.sofZmanShmaGRA,
            chatzos = czc.chatzos,
            minchaGedola = czc.minchaGedola,
            plagHamincha = czc.plagHamincha,
            shkia = czc.sunset,
            tzais = czc.tzais,
            hebrewDate = formatter.format(jewishCalendar),
            candleLighting = findCandleLighting(cal),
            roshChodeshLabel = rosh?.first,
            roshChodeshInDays = rosh?.second,
            dateStrip = buildDateStrip(cal)
        )
    }

    private fun findCandleLighting(today: Calendar): Date? {
        val friday = today.clone() as Calendar
        while (friday.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            friday.add(Calendar.DATE, 1)
        }
        val fridaySunset = buildCalendar(friday).sunset ?: return null
        return Date(fridaySunset.time - CANDLE_LIGHTING_OFFSET_MS)
    }

    private fun findNextRoshChodesh(today: Calendar, formatter: HebrewDateFormatter): Pair<String, Int>? {
        val cal = today.clone() as Calendar
        for (offset in 0..35) {
            val jc = JewishCalendar(cal)
            if (jc.isRoshChodesh) {
                val monthName = formatter.formatMonth(jc)
                return "ראש חודש $monthName" to offset
            }
            cal.add(Calendar.DATE, 1)
        }
        return null
    }

    private fun buildDateStrip(today: Calendar): List<DateStripDay> {
        val letters = listOf("א׳", "ב׳", "ג׳", "ד׳", "ה׳", "ו׳", "שבת")
        val cal = today.clone() as Calendar
        cal.add(Calendar.DATE, -3)
        return (0 until 7).map { i ->
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Calendar.SUNDAY(1)..Calendar.SATURDAY(7)
            val day = DateStripDay(
                hebrewLetter = letters[dayOfWeek - 1],
                dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                isToday = i == 3
            )
            cal.add(Calendar.DATE, 1)
            day
        }
    }

    companion object {
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private const val CANDLE_LIGHTING_OFFSET_MS = 20 * 60 * 1000L

        fun formatTime(date: Date?): String = date?.let { timeFormat.format(it) } ?: "--:--"
    }
}
