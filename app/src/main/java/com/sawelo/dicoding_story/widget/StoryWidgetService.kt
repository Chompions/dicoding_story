package com.sawelo.dicoding_story.widget

import android.content.Intent
import android.widget.RemoteViewsService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StoryWidgetService : RemoteViewsService() {
    @Inject
    lateinit var stackRemoteViewsFactory: StackRemoteViewsFactory

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        stackRemoteViewsFactory.intent = intent
        return stackRemoteViewsFactory
    }

}