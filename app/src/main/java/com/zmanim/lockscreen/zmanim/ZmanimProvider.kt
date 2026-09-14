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
    val gregorianDate: String,
    val candleLighting: Date?,
    val roshChodeshLabel: String?,
    val roshChodeshInDays: Int?,
    val parshaLabel: String?,
    val hilulaLabel: String?,
    val dateStrip: List<DateStripDay>
)

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

    fun today(): DayZmanim = forDate(Calendar.getInstance(timeZone))

    fun forDate(date: Calendar): DayZmanim {
        val cal = date.clone() as Calendar
        cal.timeZone = timeZone
        val czc = buildCalendar(cal)
        val jewishCalendar = JewishCalendar(cal).apply { inIsrael = true }
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
            gregorianDate = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).apply {
                timeZone = this@ZmanimProvider.timeZone
            }.format(cal.time),
            candleLighting = findCandleLighting(cal),
            roshChodeshLabel = rosh?.first,
            roshChodeshInDays = rosh?.second,
            parshaLabel = findUpcomingParsha(cal, formatter),
            hilulaLabel = findHilula(jewishCalendar),
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

    /**
     * Returns the next actual weekly parasha in Israel.
     * If the nearest Shabbat is a festival and KosherJava returns no parasha,
     * continue to following Shabbatot until a regular weekly portion is found.
     */
    private fun findUpcomingParsha(today: Calendar, formatter: HebrewDateFormatter): String? {
        val shabbat = today.clone() as Calendar
        while (shabbat.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            shabbat.add(Calendar.DATE, 1)
        }
        repeat(8) {
            val jc = JewishCalendar(shabbat).apply { inIsrael = true }
            val label = runCatching { formatter.formatParsha(jc).trim() }.getOrNull()
            if (!label.isNullOrBlank()) return label
            shabbat.add(Calendar.DATE, 7)
        }
        return null
    }

    private fun findHilula(jc: JewishCalendar): String? {
        val month = jc.jewishMonth
        val day = jc.jewishDayOfMonth
        return HILULOT[month to day]?.joinToString(" • ")
    }

    private fun findNextRoshChodesh(today: Calendar, formatter: HebrewDateFormatter): Pair<String, Int>? {
        val cal = today.clone() as Calendar
        for (offset in 0..35) {
            val jc = JewishCalendar(cal).apply { inIsrael = true }
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
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
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

        private val HILULOT: Map<Pair<Int, Int>, List<String>> = mapOf(
            (7 to 18) to listOf("רבי נחמן מברסלב"),
            (7 to 25) to listOf("רבי לוי יצחק מברדיטשוב"),
            (8 to 15) to listOf("החזון איש"),
            (8 to 16) to listOf("רבי שלמה קרליבך"),
            (10 to 20) to listOf("הרמב״ם"),
            (10 to 24) to listOf("בעל התניא"),
            (11 to 4) to listOf("הבבא סאלי"),
            (12 to 7) to listOf("משה רבנו"),
            (12 to 21) to listOf("רבי אלימלך מליז׳נסק"),
            (2 to 14) to listOf("רבי מאיר בעל הנס"),
            (2 to 18) to listOf("רבי שמעון בר יוחאי"),
            (2 to 26) to listOf("הרמח״ל"),
            (4 to 3) to listOf("הרבי מליובאוויטש"),
            (5 to 5) to listOf("האר״י הקדוש")
        )

        fun formatTime(date: Date?): String = date?.let { timeFormat.format(it) } ?: "--:--"
    }
}
