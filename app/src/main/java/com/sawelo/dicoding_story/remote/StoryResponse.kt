package com.sawelo.dicoding_story.remote

import com.google.gson.annotations.SerializedName

data class StoryResponse(
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String,
    @field:SerializedName("loginResult")
    val loginResult: LoginResultResponse? = null,
    @field:SerializedName("listStory")
    val listStory: List<StoryListResponse>? = null
)

data class LoginResultResponse(
    @field:SerializedName("userId")
    val userId: String,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("token")
    val token: String,
)

data class StoryListResponse(
    @field:SerializedName("id")
    val id: String,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("description")
    val description: String,
    @field:SerializedName("photoUrl")
    val photoUrl: String,
    @field:SerializedName("createdAt")
    val createdAt: String,
    @field:SerializedName("lat")
    val lat: Float,
    @field:SerializedName("lon")
    val lon: Float,
)