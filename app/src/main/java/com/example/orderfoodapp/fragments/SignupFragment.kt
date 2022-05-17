package com.example.orderfoodapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.MainActivity
import com.example.orderfoodapp.models.Customer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignupFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth;

    //Signup email and password
    companion object {
        var SIGNUP_EMAIL = ""
        var SIGNUP_PASSWORD = ""

        fun createCustomerData(email: String) {
            val dbRef = FirebaseDatabase.getInstance().getReference("Customer");
            val newUser = Customer(
                "",
                10000,
                "",
                email,
                "",
                "",
                ""
            )
            dbRef.push().setValue(newUser);
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_signup, container, false);

        mAuth = FirebaseAuth.getInstance();

        var btnSignup = view.findViewById<Button>(R.id.btnSignup);
        btnSignup.setOnClickListener() {
            createUser()
        }
        return view;
    }

    private fun createUser() {
        val edtEmail = requireView().findViewById<EditText>(R.id.edtEmailAddress);
        val edtPassword = requireView().findViewById<EditText>(R.id.edtPassword);
        val edtConfirmPassword = requireView().findViewById<EditText>(R.id.edtConfirmPassword);


        val email: String = edtEmail.text.toString();
        val password: String = edtPassword.text.toString();
        val confirmPassword: String = edtConfirmPassword.text.toString();

        when {
            email.isEmpty() -> {
                edtEmail.error = "Email can't be empty";
                edtEmail.requestFocus();
            }

            password.isEmpty() -> {
                edtPassword.error = "Password can't be empty";
                edtPassword.requestFocus();
            }

            confirmPassword.isEmpty() -> {
                edtConfirmPassword.error = "You must confirm password"
                edtConfirmPassword.requestFocus();
            }

            password != confirmPassword -> {
                edtConfirmPassword.error = "Your confirm is not correct";
                edtConfirmPassword.requestFocus();
            }

            else -> {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Signup user", "createUserWithEmail:success")

                            FirebaseAuth.getInstance().currentUser?.sendEmailVerification();

                            createCustomerData(email);

                            SIGNUP_EMAIL = email;
                            SIGNUP_PASSWORD = password;

                            Toast.makeText(
                                requireContext(),
                                "User sign up successfully, please check mail to verify account!",
                                Toast.LENGTH_SHORT
                            ).show()

                            (requireActivity() as MainActivity).backToLoginFragment();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Signup user", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(

                                requireContext(), "Signup failed. ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}