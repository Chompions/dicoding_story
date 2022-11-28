package com.sawelo.dicoding_story.data.remote

import com.sawelo.dicoding_story.data.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("v1/register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): StoryResponse

    @FormUrlEncoded
    @POST("v1/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): StoryResponse

    @GET("v1/stories")
    suspend fun getStories(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: Int
    ): StoryResponse

    @Multipart
    @POST("v1/stories")
    suspend fun postStoryWithLocation(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Float,
        @Part("lon") lon: Float,
    ): StoryResponse

    @Multipart
    @POST("v1/stories")
    suspend fun postStory(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): StoryResponse
}