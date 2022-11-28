package com.sawelo.dicoding_story.data.local

import androidx.paging.*
import androidx.room.withTransaction
import com.sawelo.dicoding_story.data.StoryListResponse
import com.sawelo.dicoding_story.data.remote.StoryRepository

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val appDatabase: AppDatabase,
    private val storyRepository: StoryRepository,
) : RemoteMediator<Int, StoryListResponse>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryListResponse>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKey = state.anchorPosition?.let { position ->
                        state.closestItemToPosition(position)?.id?.let { id ->
                            appDatabase.remoteKeysDao().getRemoteKeysId(id)
                        }
                    }
                    remoteKey?.currentKey ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKey = state.pages.firstOrNull()?.data?.firstOrNull()?.let {
                        appDatabase.remoteKeysDao().getRemoteKeysId(it.id)
                    }
                    remoteKey?.prevKey
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    val remoteKey = state.pages.lastOrNull()?.data?.lastOrNull()?.let {
                        appDatabase.remoteKeysDao().getRemoteKeysId(it.id)
                    }
                    remoteKey?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
            val responseData = storyRepository.getStoriesList(page, state.config.pageSize)
            val isEndOfPaginationReached = responseData.isEmpty()

            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    appDatabase.remoteKeysDao().deleteRemoteKeys()
                    appDatabase.storyDao().clearAll()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (isEndOfPaginationReached) null else page + 1
                val keys = responseData.map {
                    RemoteKeys(
                        id = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey,
                        currentKey = page
                    )
                }
                appDatabase.remoteKeysDao().insertAll(keys)
                appDatabase.storyDao().insertAll(responseData)
            }

            MediatorResult.Success(endOfPaginationReached = isEndOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}