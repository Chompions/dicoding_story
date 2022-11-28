package com.sawelo.dicoding_story.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val id: String,
    val currentKey: Int,
    val prevKey: Int?,
    val nextKey: Int?,
)