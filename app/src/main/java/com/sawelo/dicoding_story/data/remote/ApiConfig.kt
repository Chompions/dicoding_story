package com.sawelo.dicoding_story.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private val loggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://story-api.dicoding.dev/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun getApiService(): ApiService = retrofit.create(ApiService::class.java)
}