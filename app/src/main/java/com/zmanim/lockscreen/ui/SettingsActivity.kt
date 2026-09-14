package com.zmanim.lockscreen.ui

import android.Manifest
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zmanim.lockscreen.R
import com.zmanim.lockscreen.data.LocationConfig
import com.zmanim.lockscreen.data.PresetLocations
import com.zmanim.lockscreen.data.ZmanimSettings
import com.zmanim.lockscreen.wallpaper.ZmanimWallpaperService

class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: ZmanimSettings

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) resolveGpsLocation()
            else Toast.makeText(this, R.string.settings_permission_denied, Toast.LENGTH_SHORT).show()
        }

    private val backgroundPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@registerForActivityResult
            runCatching {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            settings.backgroundUri = uri.toString()
            Toast.makeText(this, R.string.settings_background_saved, Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings = ZmanimSettings(this)

        val citySpinner = findViewById<Spinner>(R.id.citySpinner)
        citySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            PresetLocations.ALL.map { it.name }
        )

        val currentIndex = PresetLocations.ALL.indexOfFirst { it.name == settings.location.name }
        if (currentIndex >= 0) citySpinner.setSelection(currentIndex)

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                settings.location = PresetLocations.ALL[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        findViewById<Button>(R.id.useGpsButton).setOnClickListener {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        findViewById<Button>(R.id.chooseBackgroundButton).setOnClickListener {
            backgroundPicker.launch(arrayOf("image/*"))
        }

        findViewById<Button>(R.id.setWallpaperButton).setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, ZmanimWallpaperService::class.java)
            )
            startActivity(intent)
        }
    }

    private fun resolveGpsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (last == null) {
            Toast.makeText(this, R.string.settings_location_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        settings.location = LocationConfig(
            name = getString(R.string.settings_gps_label),
            latitude = last.latitude,
            longitude = last.longitude
        )
        Toast.makeText(this, R.string.settings_location_updated, Toast.LENGTH_SHORT).show()
    }
}
