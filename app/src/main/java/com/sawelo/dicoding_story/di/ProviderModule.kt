package com.sawelo.dicoding_story.di

import android.content.Context
import android.content.SharedPreferences
import com.sawelo.dicoding_story.remote.StoryRepository
import com.sawelo.dicoding_story.ui.StoryPagingSource
import com.sawelo.dicoding_story.utils.SharedPrefsData
import com.sawelo.dicoding_story.widget.StackRemoteViewsFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ProviderModule {
    @Inject
    lateinit var storyRepository: StoryRepository
    @Inject
    lateinit var sharedPrefsData: SharedPrefsData

    @Singleton
    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideStackRemoteViewsFactory(@ApplicationContext context: Context)
    : StackRemoteViewsFactory {
        return StackRemoteViewsFactory(context, storyRepository)
    }

    @Singleton
    @Provides
    fun provideStoryPageSource(): StoryPagingSource {
        return StoryPagingSource(storyRepository)
    }

    companion object {
        private const val SHARED_PREF_KEY = "com.sawelo.dicoding_story.PREFERENCE_FILE_KEY"
    }
}