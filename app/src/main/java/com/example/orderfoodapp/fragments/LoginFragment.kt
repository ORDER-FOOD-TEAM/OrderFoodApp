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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.MainMenuActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONException
import org.json.JSONObject
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var oneTapClient: SignInClient;
    private lateinit var signInRequest: BeginSignInRequest;
    private lateinit var callbackManager: CallbackManager;

    private lateinit var dialog: Dialog;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false);

        val btnLogin = view.findViewById<Button>(R.id.login_button);
        val btnGoogleLogin = view.findViewById<FloatingActionButton>(R.id.google_login_button);
        val btnFacebookLogin = view.findViewById<FloatingActionButton>(R.id.facebook_login_button);

        //init loading dialog
        dialog = Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_loading_login);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        configLoginFacebookManager();

        btnLogin.setOnClickListener {
            loginUser();
        }

        btnGoogleLogin.setOnClickListener {
            signInGoogle();
        }

        btnFacebookLogin.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(
                    this,
                    callbackManager,
                    Arrays.asList("email", "public_profile")
                )
        }

        return view;
    }

    override fun onResume() {
        super.onResume()

        val edtEmail = requireView().findViewById<EditText>(R.id.email_editText);
        val edtPassword = requireView().findViewById<EditText>(R.id.password_editText);

        //After sign up will return to fragment login -> auto fill field text
        val emailPassed = SignupFragment.SIGNUP_EMAIL;
        val passwordPassed = SignupFragment.SIGNUP_PASSWORD;

        if (emailPassed.isNotEmpty() && passwordPassed.isNotEmpty()) {
            edtEmail.setText(emailPassed);
            edtPassword.setText(passwordPassed);
        }
    }

    //Login with email and password
    private fun loginUser() {
        dialog.show();

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
                signInWithEmailAndPasswordFirebase(email, password);
            }
        }
    }

    //Login google
    private fun signInGoogle() {
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
                Log.d("One Tap UI", e.printStackTrace().toString())
            }
    }

    private
    var loginResultHandler: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential =
                        oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken;
                    val email = credential.id;
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            val firebaseCredential =
                                GoogleAuthProvider.getCredential(idToken, null);
                            signInWithCredentialFirebase(firebaseCredential, email)
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
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(
                                "Api Exception",
                                "One-tap encountered a network error."
                            )
                        }
                        else -> {
                            Log.d(
                                "Api Exception",
                                "Couldn't get credential from result." +
                                        " (${e.localizedMessage})"
                            )
                        }
                    }
                }
            }
        };

    //Login facebook
    private fun configLoginFacebookManager() {
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val email = getDataFacebookLogin(result.accessToken);
                    handleFacebookAccessToken(result.accessToken, email);
                    Log.d("Success", "Login")
                }

                override fun onCancel() {
                    Toast.makeText(context, "Login Cancel", Toast.LENGTH_LONG).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun handleFacebookAccessToken(token: AccessToken, email: String) {
        Log.d("FB Token", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token);
        signInWithCredentialFirebase(credential, email);
    }

    private fun getDataFacebookLogin(token: AccessToken): String {
        lateinit var email: String;
        val request = GraphRequest.newMeRequest(
            token
        ) { obj, response ->
            val json = response?.jsonObject;
            try {
                if (json != null) {
                    email = json.getString("email")
                }
            } catch (e: JSONException) {
            }
        }
        return email;
    }

    //Sign in to firebase
    private fun signInWithCredentialFirebase(credential: AuthCredential, email: String) {
        dialog.show();

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(
                        "Sign in",
                        "signInWithCredential:success"
                    )
                    createUserIfNotExists(email);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        "Sign in",
                        "signInWithCredential:failure",
                        task.exception
                    )
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show();
                }
            }
    }

    private fun signInWithEmailAndPasswordFirebase(email: String, password: String) {
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
                            requireActivity(),
                            "Please check mail and verify your account!",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        notifyLoginSuccessAndStartActivity();
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

    //Utils
    private fun notifyLoginSuccessAndStartActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss();
            }
        }, 1500);

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

    private fun createUserIfNotExists(email: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer");
        val query = dbRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                    notifyLoginSuccessAndStartActivity();
                else {
                    SignupFragment.createCustomerData(email);
                    notifyLoginSuccessAndStartActivity();
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

