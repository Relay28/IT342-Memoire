package com.example.memoire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.memoire.api.RetrofitClient
import com.example.memoire.com.example.memoire.HomeActivity
import com.example.memoire.models.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val username = findViewById<EditText>(R.id.usernameInput)
        val email = findViewById<EditText>(R.id.emailInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        val confirmPassword = findViewById<EditText>(R.id.confirmPasswordInput)
        val goLoginbtn = findViewById<TextView>(R.id.loginPrompt)

        val registerBtn = findViewById<Button>(R.id.registerButton)

        goLoginbtn.setOnClickListener(){
            finish()
        }

        registerBtn.setOnClickListener(){
            val userVal = username.text.toString().trim()
            val passVal = password.text.toString().trim()
            val confPass = confirmPassword.text.toString().trim()
            val emailVal = email.text.toString().trim()


            if(userVal.isNotEmpty() && emailVal.isNotEmpty()&&passVal.isNotEmpty()&&confPass.isNotEmpty()) {
                if(confPass.equals(passVal))
                     RegisterUser(userVal, emailVal, passVal, confPass)
                else
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun RegisterUser(username: String, email:String ,password: String,confirmPassword:String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Create a RegisterRequest object with the user information
                val registerRequest = RegisterRequest(username, email, password)

                val response = RetrofitClient.instance.register(registerRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()
                        Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        // 🔹 Navigate to another activity after login
                        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error calling API: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}