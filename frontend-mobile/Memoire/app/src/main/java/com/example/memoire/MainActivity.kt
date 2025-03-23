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
import android.content.Context
import android.content.SharedPreferences
import com.example.memoire.com.example.memoire.HomeActivity

import com.example.memoire.models.AuthenticationRequest
import com.example.memoire.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sessionManager = SessionManager(this)
//        if (sessionManager.isLoggedIn()) {
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<TextView>(R.id.registerBtn)
        val googleLogin = findViewById<CardView>(R.id.googleLoginButton)

        // 🔹 Google Sign-In Configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("500063994752-5graisegq8sp2t5mfkai2lm9a48k0kb8.apps.googleusercontent.com")  // Replace with your actual Google Client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        registerButton.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
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

        // 🔹 Google Login Button Click Listener
        googleLogin.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.login(AuthenticationRequest(username, password))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()!!
                       // Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this@MainActivity,""+response+" ",Toast.LENGTH_LONG).show()

                        // Save login data
                        saveLoginData(authResponse.token, authResponse.userId, authResponse.username, authResponse.email)

                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, "Login Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error calling API: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            Log.d("GoogleSignIn", "ID Token: $idToken")

            if (idToken != null) {
                sendIdTokenToBackend(idToken)
            } else {
                Toast.makeText(this, "Google Sign-In failed!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Sign-in failed", e)
            Toast.makeText(this, "Google Sign-In failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendIdTokenToBackend(idToken: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.verifyGoogleToken(idToken).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val userData = response.body()!!
//                        Toast.makeText(this@MainActivity, "Google Login Successful!"+userData, Toast.LENGTH_SHORT).show()

                          val userId = userData["userId"]?:""

                       val token = userData["token"] ?: "" // Unique Google user ID
                       val email = userData["email"] ?: "Unknown Email"
                       val username = userData["username"] ?: "Unknown Name"
//                        val profilePicture = userData["picture"] ?: ""
                        Toast.makeText(this@MainActivity, "Google Login Successful!"+userId, Toast.LENGTH_SHORT).show()
                        saveLoginData(token,userId.toLong(),username, email)
                        // Save data locally
                       // saveGoogleUserSession(googleId, email, name, profilePicture)
                        Log.d("GoogleSignIn", "User Email: $userData")

                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, "Google Auth Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error calling API: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveLoginData(token: String, userId: Long, username: String, email: String) {
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.putLong("userId", userId)
        editor.putString("username", username)
        editor.putString("email", email)
        editor.apply() // Saves data asynchronously
    }

    private fun saveGoogleUserSession(googleId: String, email: String, name: String, profilePicture: String) {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("googleId", googleId)  // Store Google unique ID
        editor.putString("email", email)
        editor.putString("username", name)
        editor.putString("profilePicture", profilePicture)
        editor.putBoolean("isGoogleUser", true) // Flag to check Google login
        editor.apply()
    }


    private fun checkUserLoggedIn() {
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        val isGoogleUser = sharedPreferences.getBoolean("isGoogleUser", false)

        if (token != null) {
            // User is already logged in, redirect to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}
