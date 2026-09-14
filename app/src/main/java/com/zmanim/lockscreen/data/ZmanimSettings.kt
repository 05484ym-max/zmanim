package com.zmanim.lockscreen.data

import android.content.Context
import android.content.SharedPreferences

data class LocationConfig(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0,
    val timeZoneId: String = "Asia/Jerusalem"
)

object PresetLocations {
    val JERUSALEM = LocationConfig("ירושלים", 31.7683, 35.2137, 754.0)
    val TEL_AVIV = LocationConfig("תל אביב", 32.0853, 34.7818, 5.0)
    val HAIFA = LocationConfig("חיפה", 32.7940, 34.9896, 20.0)
    val BEER_SHEVA = LocationConfig("באר שבע", 31.2530, 34.7915, 280.0)

    val ALL = listOf(JERUSALEM, TEL_AVIV, HAIFA, BEER_SHEVA)
}

/** Thin SharedPreferences wrapper - swap for DataStore later if the settings screen grows. */
class ZmanimSettings(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var location: LocationConfig
        get() {
            val name = prefs.getString(KEY_NAME, null) ?: return PresetLocations.JERUSALEM
            return LocationConfig(
                name = name,
                latitude = prefs.getFloat(KEY_LAT, PresetLocations.JERUSALEM.latitude.toFloat()).toDouble(),
                longitude = prefs.getFloat(KEY_LON, PresetLocations.JERUSALEM.longitude.toFloat()).toDouble(),
                elevation = prefs.getFloat(KEY_ELEV, 0f).toDouble(),
                timeZoneId = prefs.getString(KEY_TZ, "Asia/Jerusalem") ?: "Asia/Jerusalem"
            )
        }
        set(value) {
            prefs.edit()
                .putString(KEY_NAME, value.name)
                .putFloat(KEY_LAT, value.latitude.toFloat())
                .putFloat(KEY_LON, value.longitude.toFloat())
                .putFloat(KEY_ELEV, value.elevation.toFloat())
                .putString(KEY_TZ, value.timeZoneId)
                .apply()
        }

    companion object {
        private const val PREFS_NAME = "zmanim_settings"
        private const val KEY_NAME = "location_name"
        private const val KEY_LAT = "location_lat"
        private const val KEY_LON = "location_lon"
        private const val KEY_ELEV = "location_elev"
        private const val KEY_TZ = "location_tz"
    }
}
