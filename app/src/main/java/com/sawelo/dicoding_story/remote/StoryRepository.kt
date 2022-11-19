package com.sawelo.dicoding_story.remote

import androidx.paging.PagingData
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
        file: MultipartBody.Part,
        description: RequestBody
    )

    suspend fun getStoriesList(
        page: Int = 1,
        size: Int = 10,
    ): List<StoryListResponse>?

    fun getStoriesFlow(
        page: Int = 1,
        size: Int = 10,
    ): Flow<PagingData<StoryListResponse>>

}