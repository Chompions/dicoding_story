package com.sawelo.dicoding_story.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.sawelo.dicoding_story.utils.SharedPrefsData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StoryWidgetService : RemoteViewsService() {
    @Inject
    lateinit var sharedPrefsData: SharedPrefsData

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(applicationContext, sharedPrefsData, intent)
}