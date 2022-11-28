package com.sawelo.dicoding_story.data

import androidx.room.Entity
import androidx.room.PrimaryKey
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

@Entity(tableName = "story_list")
data class StoryListResponse(
    @PrimaryKey
    @field:SerializedName("id")
    val id: String = "id",
    @field:SerializedName("name")
    val name: String = "name",
    @field:SerializedName("description")
    val description: String = "description",
    @field:SerializedName("photoUrl")
    val photoUrl: String = "photoUrl",
    @field:SerializedName("createdAt")
    val createdAt: String = "createdAt",
    @field:SerializedName("lat")
    val lat: Double = 0.0,
    @field:SerializedName("lon")
    val lon: Double = 0.0,
)