package com.sawelo.dicoding_story.data.remote

import android.location.Location
import androidx.paging.PagingData
import com.sawelo.dicoding_story.data.StoryListResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface StoryRepository {
    suspend fun register(
        name: String,
        email: String,
        password: String
    )

    suspend fun login(
        email: String,
        password: String,
    )

    suspend fun postStory(
        file: MultipartBody.Part?,
        description: RequestBody?,
        location: Location? = null,
    )

    suspend fun getStoriesList(
        page: Int,
        size: Int,
        location: Int = 0
    ): List<StoryListResponse>

    fun getStoriesFlow(): Flow<PagingData<StoryListResponse>>

}