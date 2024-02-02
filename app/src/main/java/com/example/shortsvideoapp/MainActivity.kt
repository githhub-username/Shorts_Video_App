package com.example.shortsvideoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.shortsvideoapp.Adapter.VideoList
import com.example.shortsvideoapp.Util.util
import com.example.shortsvideoapp.databinding.ActivityMainBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var adapter: VideoList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavBar.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.bottom_nav_menu_home -> {

                }
                R.id.bottom_nav_menu_postVideo -> {
                    startActivity(Intent(this, NewVideoUploadActivity::class.java))
                }
                R.id.bottom_nav_menu_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("PROFILE_USER_ID",FirebaseAuth.getInstance().currentUser?.uid)
                    startActivity(intent)
                }
            }
            false
        }

        setUpViewPager()
    }

    private fun setUpViewPager() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(Firebase.firestore.collection("videos"), VideoModel::class.java)
            .build()

        adapter = VideoList(options)
        binding.viewPager.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

//    override fun onStop() {
//        super.onStop()
//        adapter.stopListening()
//    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

}