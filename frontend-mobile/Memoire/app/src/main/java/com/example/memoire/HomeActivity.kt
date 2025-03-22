package com.example.memoire.com.example.memoire

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memoire.ProfileActivity
import com.example.memoire.R
import com.example.memoire.utils.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val user = AuthManager.getUserInfo(this) // Inside an Activity
        val id = user?.id
        if (user != null) {
            Toast.makeText(this@HomeActivity, "${id ?: "Unknown"} exists", Toast.LENGTH_SHORT).show()
        }
        val profile = findViewById<ImageView>(R.id.prof)

        profile.setOnClickListener{
            val intent = Intent(this@HomeActivity,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}