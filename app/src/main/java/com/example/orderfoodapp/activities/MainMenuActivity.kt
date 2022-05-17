package com.example.orderfoodapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class MainMenuActivity : AppCompatActivity() {

    private val mainMenuFragment = MainMenuFragment()
    private val profileFragment = ProfileFragment()
    private val favouriteFagment = FavouriteFagment()

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