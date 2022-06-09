package com.example.orderfoodapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.example.orderfoodapp.R

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener() {
            finish()
        }
    }
}