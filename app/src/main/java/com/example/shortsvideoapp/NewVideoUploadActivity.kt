package com.example.shortsvideoapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.shortsvideoapp.Util.util
import com.example.shortsvideoapp.databinding.ActivityNewVideoUploadBinding
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class NewVideoUploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewVideoUploadBinding
    private var SelectedVideoUri: Uri? = null
    lateinit var videoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewVideoUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                SelectedVideoUri = result.data?.data

                showPostView()

            }
        }

        binding.uploadView.setOnClickListener {
            checkPermissionAndPickVideo()
        }

        binding.PostButton.setOnClickListener {
            postVideo()
        }

        binding.cancelPostButton.setOnClickListener {
            finish()
        }
    }

    private fun postVideo() {

        if(binding.postCaptionInputText.text.toString().isEmpty()) {
            binding.postCaptionInputText.error = "Write Some Caption"
            return
        }

        setProgressBar(true)

        SelectedVideoUri?.apply {
            // Store to firebase

            val videoReference = FirebaseStorage.getInstance().reference.child("videos/" + this.lastPathSegment)

            videoReference.putFile(this)
                .addOnSuccessListener {
                    videoReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Store Video Model to firestore
                        postToFirestore(downloadUrl.toString())
                    }
                }
        }
    }

    private fun postToFirestore(url: String) {

        val videoModel = VideoModel(
            FirebaseAuth.getInstance().currentUser?.uid!! + "_" + Timestamp.now().toString(),
            binding.postCaptionInputText.text.toString(),
            url,
            FirebaseAuth.getInstance().currentUser?.uid!!,
            Timestamp.now(),
        )

        Firebase.firestore.collection("videos")
            .document(videoModel.videoId)
            .set(videoModel)
            .addOnSuccessListener {
                setProgressBar(false)
                util.showToastMessage(this, "Video Uploaded")
                finish()
            }.addOnFailureListener {
                setProgressBar(false)
                util.showToastMessage(this, "Some error occured, Video not Uploaded")
            }
    }

    private fun setProgressBar(progressStatus: Boolean) {
        if(progressStatus) {
            binding.progressBar.visibility = View.VISIBLE
            binding.PostButton.visibility = View.GONE
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.PostButton.visibility = View.VISIBLE
        }
    }

    private fun showPostView() {

        SelectedVideoUri?.let {
            binding.postView.visibility = View.VISIBLE
            binding.uploadView.visibility = View.GONE

            Glide.with(binding.postThumbnailView).load(it).into(binding.postThumbnailView)
        }
    }

    private fun checkPermissionAndPickVideo() {
        var readExternalVideo: String = ""

        if(Build.VERSION.SDK_INT >= TIRAMISU) {
            readExternalVideo = android.Manifest.permission.READ_MEDIA_VIDEO
        }
        else {
            readExternalVideo = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if(ContextCompat.checkSelfPermission(this, readExternalVideo) == PackageManager.PERMISSION_GRANTED) {
            openVideoPicker()
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(readExternalVideo), 100)
        }
    }

    private fun openVideoPicker() {

        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        videoLauncher.launch(intent)
    }
}