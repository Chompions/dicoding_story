package com.sawelo.dicoding_story.ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.data.StoryListResponse

class ListStoryAdapter(
    diffUtil: DiffUtil.ItemCallback<StoryListResponse>,
    private val callback: ListStoryAdapterCallback? = null
) : PagingDataAdapter<StoryListResponse, ListStoryAdapter.ListStoryViewHolder>(diffUtil) {

    inner class ListStoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.iv_item_photo)
        val name: TextView = itemView.findViewById(R.id.iv_item_name)
        val progressBar: ProgressBar = itemView.findViewById(R.id.pb_item_load)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return ListStoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListStoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.name.text = item?.name
        holder.image.transitionName = "imageTransition_$position"
        Glide.with(holder.itemView)
            .load(item?.photoUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.progressBar.visibility = View.GONE
                    return false
                }

            })
            .into(holder.image)

        holder.itemView.setOnClickListener {
            if (item != null) {
                callback?.setOnClick(item, holder.image)
            }
        }
    }

    interface ListStoryAdapterCallback {
        fun setOnClick(item: StoryListResponse, imageView: ImageView)
    }
}