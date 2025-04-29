package com.example.memoire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.AuthenticationRequest
import com.example.memoire.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        // Check if user is already logged in
        if (sessionManager.isLoggedIn() && sessionManager.isTokenValid()) {
            navigateToHomeAndRegisterFcmToken()
            return
        } else if (sessionManager.isLoggedIn()) {
            // Token expired, force logout
            sessionManager.logoutUser()
            showLoginError("Session expired. Please login again.")
        }

        initializeViews()
        setupGoogleSignIn()
    }

    private fun initializeViews() {
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<TextView>(R.id.registerBtn)
        val googleLogin = findViewById<CardView>(R.id.googleLoginButton)

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            val username = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        googleLogin.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("500063994752-5graisegq8sp2t5mfkai2lm9a48k0kb8.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.login(AuthenticationRequest(username, password))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()!!
                        sessionManager.saveLoginData(
                            authResponse.token,
                            authResponse.userId,
                            authResponse.username,
                            authResponse.email
                        )
                        navigateToHomeAndRegisterFcmToken()
                    } else {
                        showLoginError(response.message())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoginError(e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }


    private fun navigateToHomeAndRegisterFcmToken() {
        // Get FCM token and register with server
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Save token locally first
                sessionManager.saveFcmToken(token)
                // Then register with server if we have a valid session
                if (sessionManager.isLoggedIn()) {
                    registerFcmTokenWithServer(token)
                }
            }
        }

        // Navigate to home
        startActivity(Intent(this, MainContainerActivity::class.java))
        finish()
    }


    private fun registerFcmTokenWithServer(fcmToken: String) {
        val userId = sessionManager.getUserSession()["userId"] as? Long ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.updateFcmToken(userId, fcmToken)
                if (!response.isSuccessful) {
                    Log.e("FCM", "Failed to register FCM token: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error registering FCM token", e)
                // Retry logic could be added here considering JWT expiration
            }
        }
    }
    private fun showLoginError(message: String) {
        Toast.makeText(this, "Login failed: $message", Toast.LENGTH_SHORT).show()
        Log.e("LoginError", message)
    }

    // Google Sign-In related methods
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { sendIdTokenToBackend(it) } ?:
            showLoginError("Google Sign-In failed: No ID token")
        } catch (e: ApiException) {
            showLoginError("Google Sign-In failed: ${e.message}")
        }
    }

    private fun sendIdTokenToBackend(idToken: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.verifyGoogleToken(idToken).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val userData = response.body()!!
                        sessionManager.saveLoginData(
                            userData["token"] ?: "",
                            (userData["userId"] ?: "-1").toLong(),
                            userData["username"] ?: "",
                            userData["email"] ?: ""
                        )
                        navigateToHomeAndRegisterFcmToken()
                    } else {
                        showLoginError("Google Auth Failed!")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoginError(e.message ?: "Unknown error")
                }
            }
        }
    }
}