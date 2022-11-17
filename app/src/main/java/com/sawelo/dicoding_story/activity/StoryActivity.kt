package com.sawelo.dicoding_story.activity

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.fragment.ListStoryFragment
import com.sawelo.dicoding_story.utils.SharedPrefsData
import com.sawelo.dicoding_story.widget.StoryWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StoryActivity : AppCompatActivity() {
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var sharedPrefsData: SharedPrefsData

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar?.title = sharedPrefsData.getName()

        menuInflater.inflate(R.menu.menu_story, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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