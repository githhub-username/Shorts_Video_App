package com.example.shortsvideoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.shortsvideoapp.Adapter.VideoList
import com.example.shortsvideoapp.databinding.ActivityPostsVideoPlayerBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class PostsVideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostsVideoPlayerBinding
    lateinit var videoId: String
    lateinit var adapter: VideoList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostsVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoId = intent.getStringExtra("videoId")!!

        setUpViewPager()
    }

    private fun setUpViewPager() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereEqualTo("videoId", videoId), VideoModel::class.java
            ).build()

        adapter = VideoList(options)
        binding.viewPager.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }
}