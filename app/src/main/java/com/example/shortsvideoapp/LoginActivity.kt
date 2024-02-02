package com.example.shortsvideoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.example.shortsvideoapp.Util.util
import com.example.shortsvideoapp.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().currentUser?.let {
            ////  user is present, auto login

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.redirectToSignUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            login()
        }
    }

    private fun login() {

        val email = binding.emailInputText.text.toString()
        val password = binding.passwordInputText.text.toString()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputText.error = "Email ID is invalid"
        }

        // Check if the password is at least 6 characters long
        if (password.length < 6) {
            binding.passwordInputText.error = "Password must be at least 6 characters";
            return
        }

        // Check if the password contains at least one alphabetic character
        if (!password.any { it.isLetter() }) {
            binding.passwordInputText.error = "Password must contain at least one alphabetic character"
            return
        }

        // Check if the password contains at least one digit
        if (!password.any { it.isDigit() }) {
            binding.passwordInputText.error = "Password must contain at least one digit"
            return
        }

        loginWithFirebase(email, password)
    }

    private fun loginWithFirebase(email: String, password: String) {

        setProgressBar(true)

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                util.showToastMessage(applicationContext, "Login Successful")
                setProgressBar(false)

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                util.showToastMessage(applicationContext, "Some Error Occured")
                setProgressBar(false)
            }
    }

    private fun setProgressBar(progressStatus: Boolean) {

        if (progressStatus) {
            binding.progressBar.visibility = View.VISIBLE
            binding.loginButton.isClickable = false
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.loginButton.isClickable = true
        }
    }
}