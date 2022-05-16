package com.example.orderfoodapp.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.MainMenuActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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
    private lateinit var oneTapClient: SignInClient;
    private lateinit var signInRequest: BeginSignInRequest;

    private val LOGIN_EMAIL_PASSWORD = 0;
    private val LOGIN_GOOGLE = 1;
    private val LOGIN_FACEBOOK = 2;

    private val REQ_ONE_TAP_SUCCESS = 1000;
    private var showOneTapUI = true;

    private var loginType = LOGIN_EMAIL_PASSWORD;

    private lateinit var dialog: Dialog;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false);

        var btnLogin = view.findViewById<Button>(R.id.login_button);
        var btnGoogleLogin = view.findViewById<FloatingActionButton>(R.id.google_login_button);
        var btnFacebookLogin = view.findViewById<FloatingActionButton>(R.id.facebook_login_button);

        //init loading dialog
        dialog = Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_loading_login);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        btnLogin.setOnClickListener() {
            dialog.show();
            loginUser();
        }

        btnGoogleLogin.setOnClickListener() {
            loginType = LOGIN_GOOGLE;
            createGoogleRequest();
//            signInByGoogle();
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

    private fun createGoogleRequest() {
        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    loginResultHandler.launch(
                        IntentSenderRequest
                            .Builder(result.pendingIntent.intentSender).build()
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("One Tap UI", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(requireActivity()) { e ->
                Log.d("One Tap UI", e.localizedMessage)
            }
    }

    private var loginResultHandler: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            run {
                if (result.resultCode == Activity.RESULT_OK) {
                    try {
                        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                        val idToken = credential.googleIdToken
                        when {
                            idToken != null -> {
                                // Got an ID token from Google. Use it to authenticate
                                // with your backend.
                                val firebaseCredential =
                                    GoogleAuthProvider.getCredential(idToken, null);
                                var user = mAuth.currentUser;
                                mAuth.signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener(requireActivity()) { task ->
                                        if (task.isSuccessful) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Toast.makeText(
                                                requireContext(),
                                                "Sign in success",
                                                Toast.LENGTH_SHORT
                                            ).show();
                                            Log.d(
                                                "Sign in with google",
                                                "signInWithCredential:success"
                                            )
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(
                                                "Sign in with google",
                                                "signInWithCredential:failure",
                                                task.exception
                                            )
                                        }
                                    }
                                mAuth.signOut();
                            }
                            else -> {
                                // Shouldn't happen.
                                Log.d("Credential", "No ID token or password!")
                            }
                        }
                    } catch (e: ApiException) {
                        when (e.statusCode) {
                            CommonStatusCodes.CANCELED -> {
                                Log.d("Api Exception", "One-tap dialog was closed.")
                                // Don't re-prompt the user.
                                showOneTapUI = false
                            }
                            CommonStatusCodes.NETWORK_ERROR -> {
                                Log.d("Api Exception", "One-tap encountered a network error.")
                                // Try again or just ignore.
                            }
                            else -> {
                                Log.d(
                                    "Api Exception", "Couldn't get credential from result." +
                                            " (${e.localizedMessage})"
                                )
                            }
                        }
                    }
                }
            }
        };

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

