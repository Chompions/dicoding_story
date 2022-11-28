package com.sawelo.dicoding_story.activity

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.fragment.DetailStoryFragment
import com.sawelo.dicoding_story.fragment.ListStoryFragment
import com.sawelo.dicoding_story.ui.StoryViewModel
import com.sawelo.dicoding_story.utils.SharedPrefsData
import com.sawelo.dicoding_story.widget.StoryWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StoryActivity : AppCompatActivity(), OnMapReadyCallback {
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var sharedPrefsData: SharedPrefsData

    private val viewModel: StoryViewModel by viewModels()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar?.title = sharedPrefsData.getName()

        menuInflater.inflate(R.menu.menu_story, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_maps -> {
                val mapFragment = SupportMapFragment.newInstance()
                mapFragment.getMapAsync(this)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace(R.id.fcv_story_fragmentContainer, mapFragment)
                }
            }
            R.id.action_logout -> {
                sharedPrefs.edit { clear() }

                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()

                updateWidget()
            }
            R.id.action_settings -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        updateWidget()

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<ListStoryFragment>(R.id.fcv_story_fragmentContainer)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setMapStyle(MapStyleOptions
            .loadRawResourceStyle(this, R.raw.map_style))

        lifecycleScope.launch {
            val boundsBuilder = LatLngBounds.builder()
            val stories = viewModel.getStoriesList()
            stories.forEach {
                val latLng = LatLng(it.lat, it.lon)
                googleMap.addMarker(
                    MarkerOptions()
                    .position(latLng)
                    .title(it.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .snippet(it.description)
                )?.apply {
                    tag = it.id
                }
                boundsBuilder.include(latLng)
            }
            googleMap.setOnMarkerClickListener { marker ->
                marker.showInfoWindow()
                true
            }
            googleMap.setOnInfoWindowClickListener { marker ->
                stories.first { it.id == marker.tag }.let { item ->
                    viewModel.setCurrentStory(item)
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        addToBackStack(null)
                        replace<DetailStoryFragment>(R.id.fcv_story_fragmentContainer)
                    }
                }
            }

            val bounds = boundsBuilder.build()
            googleMap.moveCamera(CameraUpdateFactory
                .newLatLngBounds(bounds, 0))
        }
    }

    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(application, StoryWidgetProvider::class.java)
        )
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stack_view)
        }
    }
}