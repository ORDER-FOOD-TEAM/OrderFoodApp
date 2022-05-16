package com.example.orderfoodapp.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.MainMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth;

    private var loginType = 0;

    private lateinit var dialog: Dialog;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false);

        var btnLogin = requireView().findViewById<Button>(R.id.login_button);
        var btnGoogleLogin = requireView().findViewById<Button>(R.id.google_login_button);
        var btnFacebookLogin = requireView().findViewById<Button>(R.id.facebook_login_button);

        //init loading dialog
        dialog = Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_loading_login);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        btnLogin.setOnClickListener() {
            dialog.show();
            loginUser();
        }

        return view;
    }

    override fun onResume() {
        super.onResume()

        val edtEmail = requireView().findViewById<EditText>(R.id.email_editText);
        val edtPassword = requireView().findViewById<EditText>(R.id.password_editText);

        mAuth = FirebaseAuth.getInstance();
        val user = mAuth.currentUser;

        val emailPassed = SignupFragment.SIGNUP_EMAIL;
        val passwordPassed = SignupFragment.SIGNUP_PASSWORD;

        if (emailPassed.isNotEmpty() && passwordPassed.isNotEmpty()) {
            edtEmail.setText(emailPassed);
            edtPassword.setText(passwordPassed);
        }
    }

    private fun loginUser() {
        val edtEmail = requireView().findViewById<EditText>(R.id.email_editText);
        val edtPassword = requireView().findViewById<EditText>(R.id.password_editText);

        val email = edtEmail.text.toString();
        val password = edtPassword.text.toString();

        when {
            email.isEmpty() -> {
                edtEmail.error = "Email can't be empty";
                edtEmail.requestFocus();
            }

            password.isEmpty() -> {
                edtPassword.error = "Password can't be empty";
                edtPassword.requestFocus();
            }

            else -> {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val user = mAuth.currentUser
                            if (user?.isEmailVerified == false) {
                                user.sendEmailVerification()

                                if (dialog.isShowing) {
                                    dialog.dismiss()
                                }
                                Toast.makeText(
                                    requireActivity(), "Please check mail and verify your account!",
                                    Toast.LENGTH_LONG
                                ).show()

                            } else {
                                //set delay for smooth animation
                                Handler(Looper.getMainLooper()).postDelayed({
                                    notifyLoginSuccessAndStartActivity()
                                }, 2500);
                            }
                        } else {
                            if (dialog.isShowing) {
                                dialog.dismiss()
                            }
                            android.widget.Toast.makeText(
                                requireActivity(),
                                "Login Error: " + task.exception,
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    private fun notifyLoginSuccessAndStartActivity() {
        if (dialog.isShowing) {
            dialog.dismiss();
        }
        // Sign in success, update UI with the signed-in user's information
        Toast.makeText(
            requireActivity(),
            "User logged in successfully",
            Toast.LENGTH_SHORT
        ).show()
        val intent =
            Intent(requireActivity(), MainMenuActivity::class.java)
        startActivity(intent)
    }
}
