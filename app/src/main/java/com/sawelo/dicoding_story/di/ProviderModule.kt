package com.sawelo.dicoding_story.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.sawelo.dicoding_story.data.local.AppDatabase
import com.sawelo.dicoding_story.data.remote.StoryRepository
import com.sawelo.dicoding_story.widget.StackRemoteViewsFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ProviderModule {
    @Singleton
    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideStackRemoteViewsFactory(
        @ApplicationContext context: Context,
        storyRepository: StoryRepository
    ): StackRemoteViewsFactory {
        return StackRemoteViewsFactory(context, storyRepository)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, AppDatabase::class.java, "dicoding_story_database"
        ).build()
    }

    companion object {
        private const val SHARED_PREF_KEY = "com.sawelo.dicoding_story.PREFERENCE_FILE_KEY"
    }
}