package com.example.orderfoodapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.example.orderfoodapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backButton = findViewById<ImageView>(R.id.btnBack)
        val forgotPasswordButton = findViewById<Button>(R.id.forgotPassword_button)

        backButton.setOnClickListener() {
            finish()
        }

        forgotPasswordButton.setOnClickListener() {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email: String = email_editText.text.toString()
        if (email.isEmpty()) {
            email_editText.error = "Email can't be empty"
            email_editText.requestFocus()
        }
        else {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email).addOnCompleteListener() { task ->
                if(task.isSuccessful) {
                    Toast.makeText(this, "Please check your mail to reset password", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this, "Something went wrong! Try again!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}