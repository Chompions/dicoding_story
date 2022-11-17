package com.sawelo.dicoding_story.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.sawelo.dicoding_story.R

class StoryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action != null) {
            if (intent.action == STORY_WIDGET_UPDATE_ACTION) {
                notifyRemoteDataChanged(context, intent)
            }
        }
    }

    companion object {
        const val STORY_WIDGET_ID_EXTRA = "STORY_WIDGET_ID_EXTRA"
        const val STORY_WIDGET_UPDATE_ACTION = "STORY_WIDGET_UPDATE_ACTION"

        fun notifyRemoteDataChanged(context: Context, intent: Intent) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetId = intent.getIntExtra(STORY_WIDGET_ID_EXTRA, 0)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stack_view)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, StoryWidgetService::class.java)
            intent.putExtra(STORY_WIDGET_ID_EXTRA, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val views = RemoteViews(context.packageName, R.layout.widget_story_preview)
            views.setRemoteAdapter(R.id.stack_view, intent)
            views.setEmptyView(R.id.stack_view, R.id.empty_view)

            val updateIntent = Intent(context, StoryWidgetProvider::class.java)
            updateIntent.action = STORY_WIDGET_UPDATE_ACTION
            updateIntent.putExtra(STORY_WIDGET_ID_EXTRA, appWidgetId)
            val updatePendingIntent = PendingIntent.getBroadcast(
                context, 0, updateIntent,
                if (Build.VERSION.SDK_INT >= 31) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            views.setPendingIntentTemplate(R.id.stack_view, updatePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

    }
}

