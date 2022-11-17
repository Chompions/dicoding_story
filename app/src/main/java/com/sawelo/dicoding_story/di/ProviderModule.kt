package com.sawelo.dicoding_story.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ProviderModule {

    private const val SHARED_PREF_KEY = "com.sawelo.dicoding_story.PREFERENCE_FILE_KEY"

    @Singleton
    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
    }

}