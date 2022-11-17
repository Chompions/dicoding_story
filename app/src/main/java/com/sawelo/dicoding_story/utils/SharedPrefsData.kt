package com.sawelo.dicoding_story.utils

interface SharedPrefsData {
    fun getUserId(): String?
    fun getName(): String?
    fun getToken(): String?
}