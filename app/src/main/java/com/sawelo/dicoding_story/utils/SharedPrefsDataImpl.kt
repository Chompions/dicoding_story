package com.sawelo.dicoding_story.utils

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsDataImpl @Inject constructor(
    private val sharedPrefs: SharedPreferences
) : SharedPrefsData {

    override fun getUserId(): String? {
        return sharedPrefs.getString(USER_ID_PREF_KEY, null)
    }

    override fun getName(): String? {
        return sharedPrefs.getString(NAME_PREF_KEY, null)
    }

    override fun getToken(): String? {
        return sharedPrefs.getString(TOKEN_PREF_KEY, null)
    }

    companion object {
        const val USER_ID_PREF_KEY = "USER_ID_PREF_KEY"
        const val NAME_PREF_KEY = "NAME_PREF_KEY"
        const val TOKEN_PREF_KEY = "TOKEN_PREF_KEY"
    }
}