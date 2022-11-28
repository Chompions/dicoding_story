package com.sawelo.dicoding_story.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.widget.Toast
import androidx.core.content.edit
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sawelo.dicoding_story.data.StoryListResponse
import com.sawelo.dicoding_story.data.local.AppDatabase
import com.sawelo.dicoding_story.data.local.StoryRemoteMediator
import com.sawelo.dicoding_story.utils.SharedPrefsData
import com.sawelo.dicoding_story.utils.SharedPrefsDataImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val sharedPrefs: SharedPreferences,
    private val sharedPrefsData: SharedPrefsData,
    private val appDatabase: AppDatabase
) : StoryRepository {
    private val apiService = ApiConfig.getApiService()

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ) {
        try {
            apiService.register(name, email, password)
        } catch (e: Exception) {
            Toast
                .makeText(
                    context,
                    "Register failed: ${e.message}",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ) {
        try {
            val body = apiService.login(email, password)
            sharedPrefs.edit {
                putString(SharedPrefsDataImpl.USER_ID_PREF_KEY, body.loginResult?.userId)
                putString(SharedPrefsDataImpl.NAME_PREF_KEY, body.loginResult?.name)
                putString(SharedPrefsDataImpl.TOKEN_PREF_KEY, body.loginResult?.token)
            }
            Toast
                .makeText(
                    context,
                    "Register succeed",
                    Toast.LENGTH_SHORT
                )
                .show()
        } catch (e: Exception) {
            Toast
                .makeText(
                    context,
                    "Login failed: ${e.message}",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    override suspend fun postStory(
        file: MultipartBody.Part?,
        description: RequestBody?,
        location: Location?
    ) {
        val token = "Bearer ${sharedPrefsData.getToken()}"
        try {
            if (file != null && description != null) {
                if (location != null) {
                    apiService.postStoryWithLocation(
                        token, file, description,
                        location.latitude.toFloat(), location.longitude.toFloat()
                    )
                } else {
                    apiService.postStory(token, file, description)
                }
            }
            Toast
                .makeText(
                    context, "Upload story succeed",
                    Toast.LENGTH_SHORT
                )
                .show()
        } catch (e: Exception) {
            Toast
                .makeText(
                    context, "Upload story failed: ${e.message}",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    override suspend fun getStoriesList(
        page: Int,
        size: Int,
        location: Int
    ): List<StoryListResponse> {
        val token = "Bearer ${sharedPrefsData.getToken()}"
        return try {
            apiService.getStories(token, page, size, location).listStory!!
        } catch (e: Exception) {
            Toast
                .makeText(
                    context,
                    "Get stories failed: ${e.message}",
                    Toast.LENGTH_SHORT
                )
                .show()
            throw Exception("Get stories failed")
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getStoriesFlow(): Flow<PagingData<StoryListResponse>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            remoteMediator = StoryRemoteMediator(appDatabase, this),
        ) {
            appDatabase.storyDao().queryPagingSource()
        }.flow
    }
}