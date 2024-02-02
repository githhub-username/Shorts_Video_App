package com.example.shortsvideoapp.model

data class UserModel(
    var id: String = "",
    var email: String = "",
    var password: String = "",
    var userName: String = "",
    var profilePic: String = "",
    var followerList: MutableList<String> = mutableListOf(),
    var followingList: MutableList<String> = mutableListOf()
)