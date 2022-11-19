package com.sawelo.dicoding_story.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("v1/register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): Response<StoryResponse>

    @FormUrlEncoded
    @POST("v1/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Response<StoryResponse>

    @GET("v1/stories")
    suspend fun getStories(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<StoryResponse>

    @Multipart
    @POST("v1/stories")
    suspend fun postStory(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Response<StoryResponse>
}