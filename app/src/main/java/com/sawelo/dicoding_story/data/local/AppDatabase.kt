package com.sawelo.dicoding_story.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sawelo.dicoding_story.data.StoryListResponse

@Database(entities = [StoryListResponse::class, RemoteKeys::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}