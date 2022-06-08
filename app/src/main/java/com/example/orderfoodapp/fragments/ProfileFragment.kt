package com.example.orderfoodapp.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.*
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var key: String? = ""
    private lateinit var profile_mail : TextView;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val cvEditProfile=view.findViewById<CardView>(R.id.cvEditProfile)
        val cvPaymentMethod=view.findViewById<CardView>(R.id.cvPaymentMethod)
        val cvOrderHistory=view.findViewById<CardView>(R.id.cvOrderHistory)
        val cvAboutUs=view.findViewById<CardView>(R.id.cvAboutUs)
        val cvLogout=view.findViewById<CardView>(R.id.cvLogout)

        profile_mail = view.findViewById(R.id.profile_mail)

        displayCustomer()

        cvEditProfile.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            intent.putExtra("key",key)
            startActivity(intent)
        }
        cvPaymentMethod.setOnClickListener {
            startActivity(Intent(context, PaymentMethodActivity::class.java))
        }
        cvOrderHistory.setOnClickListener {
            startActivity(Intent(context, OrderHistory::class.java))
        }
        cvAboutUs.setOnClickListener {
            startActivity(Intent(context, AboutUsActivity::class.java))
        }
        cvLogout.setOnClickListener {
            Logout()
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        displayCustomer()
    }

    private fun displayCustomer() {
        try {
            val customerEmail = Firebase.auth.currentUser!!.email.toString()
            profile_mail.text = customerEmail
            val ref = FirebaseDatabase.getInstance().getReference("Customer")

            //Get data first time
            ref.get().addOnSuccessListener {
                for (data in it.children) {
                    if(data.child("email").value.toString() == customerEmail) {
                        key = data.key.toString()
                        profile_name.text = data.child("fullName").value.toString()
                        break
                    }
                }
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }

            //Run when data change
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        if(data.child("email").value.toString() == customerEmail) {
                            key = data.key.toString()
                            profile_name.text = data.child("fullName").value.toString()
                            break
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        "Cannot load customer's data, please try again later!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.e("Error load data", error.message)
                }
            })

            val imgName = customerEmail.replace(".", "_")
            val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imgName.jpg")
            val localFile = File.createTempFile("tempfile", ".jpg")
            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                profile_picture.setImageBitmap(bitmap)
            }
        }
        //Check if user is not login -> go to login screen
        catch (ex: KotlinNullPointerException) {
            startActivity(Intent(context, MainActivity::class.java))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun Logout() {
        //Logout facebook
        LoginManager.getInstance().logOut()

        //Logout google
        val oneTapClient = Identity.getSignInClient(requireActivity())
        oneTapClient.signOut()

        //Logout firebase
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}