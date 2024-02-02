package com.example.shortsvideoapp

import android.icu.text.CaseMap.Title
import com.google.firebase.Timestamp

data class VideoModel(
    var videoId: String = "",
    var title: String = "",
    var url: String = "",
    var uploaderId: String = "",
    var createdTime: Timestamp = Timestamp.now()
)
