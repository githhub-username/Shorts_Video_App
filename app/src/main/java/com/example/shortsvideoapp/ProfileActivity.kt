package com.example.shortsvideoapp

import android.app.DownloadManager.Query
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.shortsvideoapp.Adapter.ProfilePostListAdapter
import com.example.shortsvideoapp.databinding.ActivityProfileBinding
import com.example.shortsvideoapp.model.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    lateinit var profileUserId: String
    lateinit var currentUserId: String

    lateinit var profileUserModel: UserModel

    lateinit var pictureLauncher: ActivityResultLauncher<Intent>

    lateinit var adapter: ProfilePostListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // upload photo
                uploadToFirestore(result.data?.data!!)
            }
        }

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileUserId = intent.getStringExtra("PROFILE_USER_ID")!!
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        if (profileUserId == currentUserId) {                            // current user

            binding.profileButton.text = "Log Out"

            binding.profileButton.setOnClickListener {
                logout()
            }

            binding.profilePicture.setOnClickListener {
                checkPermissionAndPickPhoto()
            }
        }
        else {                                                           // other user
            binding.profilePicture.isClickable = false
            binding.profileButton.text = "Follow"

            binding.profileButton.setOnClickListener {
                follow_Unfollow()
            }
        }

        getProfileDataFromFirebase()

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                .whereEqualTo("uploaderId", profileUserId)
                    .orderBy("createdTime", com.google.firebase.firestore.Query.Direction.DESCENDING), VideoModel::class.java
            ).build()

        adapter = ProfilePostListAdapter(options)
        binding.postRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.postRecyclerView.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

    private fun follow_Unfollow() {

        Firebase.firestore.collection("users")
            .document(currentUserId).get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!

                if(profileUserModel.followerList.contains(currentUserId)) {
                    // Unfollow user
                    profileUserModel.followerList.remove(currentUserId)
                    currentUserModel.followingList.remove(profileUserId)
                    binding.profileButton.text = "Follow"
                }
                else {
                    profileUserModel.followerList.add(currentUserId)
                    currentUserModel.followingList.add(profileUserId)
                    binding.profileButton.text = "Unfollow"
                }
                updateUserDataToFirebase(profileUserModel)
                updateUserDataToFirebase(currentUserModel)
            }
    }

    fun updateUserDataToFirebase(model: UserModel) {

        Firebase.firestore.collection("users")
            .document(model.id)
            .set(model)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    private fun checkPermissionAndPickPhoto() {
        var readExternalImages = ""

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readExternalImages = android.Manifest.permission.READ_MEDIA_IMAGES
        }
        else {
            readExternalImages = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if(ContextCompat.checkSelfPermission(this, readExternalImages) == PackageManager.PERMISSION_GRANTED) {
            openPicturePicker()
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(readExternalImages), 100)
        }
    }

    private fun openPicturePicker() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pictureLauncher.launch(intent)
    }

    private fun uploadToFirestore(photoUri: Uri) {
        val photoReference = FirebaseStorage.getInstance().reference.child("profilePic/" + currentUserId)

        photoReference.putFile(photoUri)
            .addOnSuccessListener {
                photoReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Store Video Model to firestore
                    postToFirestore(downloadUrl.toString())
                }
            }
    }

    private fun postToFirestore(url: String) {

        binding.progressBar.visibility = View.VISIBLE

        Firebase.firestore.collection("users")
            .document(currentUserId)
            .update("profilePic",url)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    private fun logout() {

        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getProfileDataFromFirebase() {
        Firebase.firestore.collection("users")
            .document(profileUserId).get()
            .addOnSuccessListener {
                profileUserModel = it.toObject(UserModel::class.java)!!
                setUi()
            }
    }

    private fun setUi() {

        profileUserModel.apply {
            Glide.with(binding.profilePicture).load(profilePic)
                .circleCrop()
                .apply(RequestOptions().placeholder(R.drawable.baseline_account_circle))
                .into(binding.profilePicture)

            binding.profileUsername.text = "@" + userName

            if(profileUserModel.followerList.contains(currentUserId)) {
                binding.profileButton.text = "Unfollow"
            }

            binding.progressBar.visibility = View.INVISIBLE

            binding.followerCountText.text = followerList.size.toString()
            binding.followingCountText.text = followingList.size.toString()

            // No of posts

            Firebase.firestore.collection("videos")
                .whereEqualTo("uploaderId", profileUserId).get()
                .addOnSuccessListener {
                    binding.postCountText.text = it.size().toString()
                }
        }
    }
}