package com.example.orderfoodapp.activities

import android.os.Bundle
import android.transition.Fade
import androidx.appcompat.app.AppCompatActivity
import com.example.orderfoodapp.R
import com.r0adkll.slidr.Slidr

class FoodDetailActivity : AppCompatActivity() {
    //private lateinit var customerEmail: String
    //private lateinit var listDish: ArrayList<Dish>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        val fade = Fade()
        window.enterTransition = fade
        window.exitTransition = fade

        Slidr.attach(this)
    }
}