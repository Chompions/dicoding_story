package com.sawelo.dicoding_story.utils

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.dicoding_story.remote.StoryListResponse

object StoryComparator : DiffUtil.ItemCallback<StoryListResponse>() {
    override fun areItemsTheSame(oldItem: StoryListResponse, newItem: StoryListResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StoryListResponse, newItem: StoryListResponse): Boolean {
        return oldItem == newItem
    }
}