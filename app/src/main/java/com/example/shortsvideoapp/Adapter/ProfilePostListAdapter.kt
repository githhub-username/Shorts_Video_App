package com.example.shortsvideoapp.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.shortsvideoapp.PostsVideoPlayerActivity
import com.example.shortsvideoapp.VideoModel
import com.example.shortsvideoapp.databinding.ItemPostRecyclerViewBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ProfilePostListAdapter(options: FirestoreRecyclerOptions<VideoModel>):
    FirestoreRecyclerAdapter<VideoModel, ProfilePostListAdapter.VideoViewHolder>(options) {

    inner class VideoViewHolder(private val binding: ItemPostRecyclerViewBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(video: VideoModel) {
            Glide.with(binding.postThumbnailImageView).load(video.url)
                .into(binding.postThumbnailImageView)

            binding.postThumbnailImageView.setOnClickListener {
                val intent = Intent(binding.postThumbnailImageView.context, PostsVideoPlayerActivity::class.java)
                intent.putExtra("videoId", video.videoId)
                binding.postThumbnailImageView.context.startActivity(intent)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemPostRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bind(model)
    }
}