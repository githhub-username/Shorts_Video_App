package com.example.shortsvideoapp.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.shortsvideoapp.ProfileActivity
import com.example.shortsvideoapp.R
import com.example.shortsvideoapp.VideoModel
import com.example.shortsvideoapp.databinding.ItemVideoBinding
import com.example.shortsvideoapp.model.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class VideoList(options: FirestoreRecyclerOptions<VideoModel>): FirestoreRecyclerAdapter<VideoModel, VideoList.VideoViewHolder>(options) {

    inner class VideoViewHolder(private  val binding: ItemVideoBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindVideo(videoModel: VideoModel) {

            //binding user data

            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId).get()
                .addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)

                    userModel?.apply {
                        binding.uploaderUsernameText.text = userName

                        // profile picture
                        Glide.with(binding.uploaderProfileIcon).load(profilePic)
                            .circleCrop()
                            .apply(RequestOptions().placeholder(R.drawable.baseline_account_circle))
                            .into(binding.uploaderProfileIcon)

                        binding.layoutUuploaderDetail.setOnClickListener {
                            val intent = Intent(binding.layoutUuploaderDetail.context, ProfileActivity::class.java)
                            intent.putExtra("PROFILE_USER_ID", id)
                            binding.layoutUuploaderDetail.context.startActivity(intent)
                        }
                    }
                }

            binding.captionText.text = videoModel.title
            binding.progressBar.visibility = View.VISIBLE

            //Video binding

            binding.videoView.apply {
                setVideoPath(videoModel.url)
                setOnPreparedListener {
                    binding.progressBar.visibility = View.GONE
                    it.start()
                    it.isLooping = true
                }

                // play and pause

                setOnClickListener {
                    if(isPlaying) {
                        pause()
                        binding.pauseIcon.visibility = View.VISIBLE
                    }
                    else {
                        start()
                        binding.pauseIcon.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bindVideo(model)
    }

}