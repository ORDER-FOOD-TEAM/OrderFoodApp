package com.example.orderfoodapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.R
import com.example.orderfoodapp.fragments.FavouriteFagment
import com.example.orderfoodapp.fragments.MainMenuFragment
import com.example.orderfoodapp.fragments.ProfileFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
//TO NOT WASTE TIME ON REFER ONE BY ONE ELEMENT
import kotlinx.android.synthetic.main.activity_main_menu.*


class MainMenuActivity : AppCompatActivity() {

    private val mainMenuFragment = MainMenuFragment()
    private val profileFragment = ProfileFragment()
    private val favouriteFragment = FavouriteFagment()

    //private val bottom_app_bar = findViewById<BottomNavigationView>(R.id.bottom_app_bar)
    //private val shoppingCart_button = findViewById<FloatingActionButton>(R.id.shoppingCart_button)

    //initial the main menu fragment to be the first fragment
    private var curFragment: Fragment = mainMenuFragment

    private var customerEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        customerEmail = Firebase.auth.currentUser?.email.toString()

        checkFullFillInformation()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, mainMenuFragment)
        transaction.commit()

        bottom_app_bar.setOnNavigationItemSelectedListener {
            //to hide shoppingCart button at the profile fragment
            when(it.itemId) {
                R.id.navbottombar_profile -> {
                    replaceFragment(profileFragment)
                    shoppingCart_button.visibility = View.GONE
                }
                R.id.navbottombar_home -> {
                    replaceFragment(mainMenuFragment)
                    shoppingCart_button.visibility = View.VISIBLE
                }
                R.id.navbottombar_favourite -> {
                    replaceFragment(favouriteFragment)
                    shoppingCart_button.visibility = View.VISIBLE
                }
            }
            true
        }

        //move to Cart and start cart activity
        shoppingCart_button.setOnClickListener() {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

    //to switch between tabs from the bottom navigation bar
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        if(curFragment == mainMenuFragment) {
            transaction.setCustomAnimations(
                R.anim.enter_right_to_left, R.anim.exit_right_to_left)
        }
        else if(curFragment == profileFragment) {
            transaction.setCustomAnimations(
                R.anim.enter_left_to_right, R.anim.exit_left_to_right)
        }
        else if(curFragment == favouriteFragment) {
            if(fragment == mainMenuFragment)
                transaction.setCustomAnimations(
                    R.anim.enter_left_to_right, R.anim.exit_left_to_right)
            else
                transaction.setCustomAnimations(
                    R.anim.enter_right_to_left, R.anim.exit_right_to_left)
        }

        transaction.hide(curFragment)
        if(fragment.isAdded)
            transaction.show(fragment)
        else
            transaction.add(R.id.fragment_container,fragment)

        transaction.commit()
        curFragment = fragment
    }

    private fun checkFullFillInformation() {
        //to refer to the database and query
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        val query = dbRef.orderByChild("email").equalTo(customerEmail)

        //to check information of the user
        //if their information is not met requirement, take them to FillInformation
        query.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                if(snapshot.exists())
                {
                    for (data in snapshot.children)
                    {
                        val name = data.child("fullName").value as String
                        val address = data.child("address").value as String
                        val phoneNumber = data.child("phoneNumber").value as String
                        val gender = data.child("gender").value as String
                        val dateOfBirth = data.child("dateOfBirth").value as String

                        var bool: Boolean = name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() || gender.isEmpty() || dateOfBirth.isEmpty()
                        if(bool)
                            startActivity(Intent(this@MainMenuActivity, FillInformationActivity::class.java))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //null
            }
        })
    }
}