package com.sawelo.dicoding_story.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.remote.ApiConfig
import com.sawelo.dicoding_story.remote.StoryResponse
import com.sawelo.dicoding_story.utils.SharedPrefsData
import com.sawelo.dicoding_story.widget.StoryWidgetProvider.Companion.notifyRemoteDataChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StackRemoteViewsFactory(
    private val context: Context,
    private val sharedPrefsData: SharedPrefsData,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {
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
        getRemoteData()
    }

    override fun onDestroy() {
        coroutineScope.launch(Dispatchers.Main) {
            nextWidgetItems.removeObserver(observer)
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

    private fun getRemoteData() {
        val token = sharedPrefsData.getToken()
        val client = ApiConfig.getApiService().getStories("Bearer $token")
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                coroutineScope.launch {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.listStory != null) {
                            val list = mutableListOf<Bitmap>()

                            body.listStory.forEach { story ->
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
                    } else {
                        if (response.errorBody() != null) {
                            withContext(Dispatchers.Main) {
                                val errorResponse = ApiConfig.convertErrorBody<StoryResponse>(
                                    response.errorBody()!!
                                )

                                Toast
                                    .makeText(
                                        context,
                                        "Get stories failed: ${errorResponse.message}",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()

                                nextWidgetItems.value = emptyList()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                Toast
                    .makeText(context, "Get stories failed: ${t.message}", Toast.LENGTH_SHORT)
                    .show()

                nextWidgetItems.value = emptyList()
            }
        })
    }
}