package com.example.shortsvideoapp.Util

import android.content.Context
import android.os.Message
import android.widget.Toast

object util {

    fun showToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

}