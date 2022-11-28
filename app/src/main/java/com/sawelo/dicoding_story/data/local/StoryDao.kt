package com.sawelo.dicoding_story.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sawelo.dicoding_story.data.StoryListResponse

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(story: List<StoryListResponse>)
    @Query("SELECT * FROM story_list")
    fun queryPagingSource(): PagingSource<Int, StoryListResponse>
    @Query("DELETE FROM story_list")
    suspend fun clearAll()
}