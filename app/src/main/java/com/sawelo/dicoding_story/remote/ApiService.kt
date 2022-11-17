package com.sawelo.dicoding_story.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("v1/register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<StoryResponse>

    @FormUrlEncoded
    @POST("v1/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<StoryResponse>

    @GET("v1/stories")
    fun getStories(
        @Header("Authorization") authorization: String,
    ): Call<StoryResponse>

    @Multipart
    @POST("v1/stories")
    fun postStory(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<StoryResponse>
}