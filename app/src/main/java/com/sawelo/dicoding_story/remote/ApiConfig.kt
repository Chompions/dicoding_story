package com.sawelo.dicoding_story.remote

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private val loggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://story-api.dicoding.dev/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun getApiService(): ApiService = retrofit.create(ApiService::class.java)

    inline fun <reified T> convertErrorBody(errorBody: ResponseBody): T {
        val converter: Converter<ResponseBody, T> =
            retrofit.responseBodyConverter(
                T::class.java, arrayOfNulls(0)
            )
        return converter.convert(errorBody) as T
    }
}