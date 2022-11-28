package com.sawelo.dicoding_story.di

import com.sawelo.dicoding_story.data.remote.StoryRepository
import com.sawelo.dicoding_story.data.remote.StoryRepositoryImpl
import com.sawelo.dicoding_story.utils.SharedPrefsData
import com.sawelo.dicoding_story.utils.SharedPrefsDataImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class SingletonBinderModule {
    @Binds
    abstract fun bindSharedPrefsData (sharedPrefsDataImpl: SharedPrefsDataImpl): SharedPrefsData

    @Binds
    abstract fun bindStoryRepository(storyRepositoryImpl: StoryRepositoryImpl): StoryRepository
}