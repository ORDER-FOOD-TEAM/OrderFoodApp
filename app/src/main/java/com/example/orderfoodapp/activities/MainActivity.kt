package com.example.orderfoodapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.example.orderfoodapp.R
import com.example.orderfoodapp.adapters.AuthAdaptor
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var loginViewPage: ViewPager2;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance();

        val authAdaptor = AuthAdaptor(this)
        loginViewPage = findViewById<ViewPager2>(R.id.login_view_pager);
        loginViewPage.adapter = authAdaptor;

        val loginTabLayout = findViewById<TabLayout>(R.id.login_tab_layout);
        TabLayoutMediator(loginTabLayout, loginViewPage) { tab, position ->
            if (position == 0)
                tab.text = "Login"
            else
                tab.text = "Signup"
        }.attach()
    }

    fun backToLoginFragment() {
        loginViewPage.setCurrentItem(0, true);
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.signOut()
        val currentUser = mAuth.currentUser
        Log.d("User", currentUser.toString())
        if (currentUser != null && currentUser.isEmailVerified) {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent);
        }
    }
}