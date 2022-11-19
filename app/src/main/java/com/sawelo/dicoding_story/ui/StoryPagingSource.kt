package com.sawelo.dicoding_story.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sawelo.dicoding_story.remote.StoryListResponse
import com.sawelo.dicoding_story.remote.StoryRepository


class StoryPagingSource (
    private val repository: StoryRepository,
): PagingSource<Int, StoryListResponse>() {
    override fun getRefreshKey(state: PagingState<Int, StoryListResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryListResponse> {
        return try {
            val position = params.key ?: 1
            val response = repository.getStoriesList(position)

            LoadResult.Page(
                data = response ?: emptyList(),
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (response?.isEmpty() == true) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}