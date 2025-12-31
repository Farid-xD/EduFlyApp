package com.example.proj.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proj.R
import com.example.proj.models.VideoSuggestion

class VideosAdapter(
    private val videos: List<VideoSuggestion>,
    private val onVideoClick: (VideoSuggestion) -> Unit
) : RecyclerView.Adapter<VideosAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_suggestion, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.bind(video)
    }

    override fun getItemCount() = videos.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvVideoTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvVideoDescription)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvVideoDuration)
        private val tvChannel: TextView = itemView.findViewById(R.id.tvChannelName)

        fun bind(video: VideoSuggestion) {
            tvTitle.text = video.title
            tvDescription.text = video.description

            // Load thumbnail if available
            video.thumbnailUrl?.let { thumbnailUrl ->
                Glide.with(itemView.context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.ic_video)
                    .into(ivThumbnail)
            } ?: run {
                ivThumbnail.setImageResource(R.drawable.ic_video)
            }

            // Show duration if available
            video.formatDuration()?.let { formattedDuration ->
                tvDuration.text = formattedDuration
                tvDuration.visibility = View.VISIBLE
            } ?: run {
                tvDuration.visibility = View.GONE
            }

            // Show channel if available
            video.channelName?.let { channel ->
                tvChannel.text = channel
                tvChannel.visibility = View.VISIBLE
            } ?: run {
                tvChannel.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onVideoClick(video)
            }
        }
    }
}