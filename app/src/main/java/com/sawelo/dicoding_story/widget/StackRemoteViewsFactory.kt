package com.sawelo.dicoding_story.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.data.remote.StoryRepository
import com.sawelo.dicoding_story.widget.StoryWidgetProvider.Companion.notifyRemoteDataChanged
import kotlinx.coroutines.*
import javax.inject.Inject

class StackRemoteViewsFactory @Inject constructor(
    private val context: Context,
    private val repository: StoryRepository
) : RemoteViewsService.RemoteViewsFactory {
    lateinit var intent: Intent
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var observer: Observer<List<Bitmap>>
    private var prevWidgetItems: List<Bitmap>? = null
    private val nextWidgetItems = MutableLiveData<List<Bitmap>>()

    override fun onCreate() {
        coroutineScope = CoroutineScope(Dispatchers.IO)
        observer = Observer<List<Bitmap>> {
            if (prevWidgetItems != nextWidgetItems.value) {
                prevWidgetItems = nextWidgetItems.value
                notifyRemoteDataChanged(context, intent)
            }
        }
        nextWidgetItems.observeForever(observer)
    }

    override fun onDataSetChanged() {
        coroutineScope.launch {
            val stories = repository.getStoriesList(1, 10)
            val list = mutableListOf<Bitmap>()

            stories.forEach { story ->
                val bitmap =
                    withContext(Dispatchers.IO) {
                        Glide.with(context)
                            .asBitmap()
                            .load(story.photoUrl)
                            .submit(250, 250)
                            .get()
                    }

                list.add(bitmap)
            }
            nextWidgetItems.postValue(list)
        }
    }

    override fun onDestroy() {
        coroutineScope.launch(Dispatchers.Main) {
            nextWidgetItems.removeObserver(observer)
            this.cancel()
        }
    }

    override fun getCount(): Int = nextWidgetItems.value?.size ?: 0

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.item_widget)
        views.setImageViewBitmap(R.id.iv_widgetItem_image, nextWidgetItems.value?.get(position))

        val fillInIntent = Intent()
        val extras = bundleOf(
            StoryWidgetProvider.STORY_WIDGET_ID_EXTRA to position
        )
        fillInIntent.putExtras(extras)
        views.setOnClickFillInIntent(R.id.iv_widgetItem_image, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}