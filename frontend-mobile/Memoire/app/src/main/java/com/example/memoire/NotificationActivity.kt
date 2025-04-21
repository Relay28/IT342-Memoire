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
import com.example.memoire.activities.SearchActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_notification)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val profile = findViewById<ImageView>(R.id.prof)

        profile.setOnClickListener{
            val intent = Intent(this@NotificationActivity,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val notificationBtn = findViewById<ImageView>(R.id.ivNotification)

        notificationBtn.setOnClickListener {
            val intent = Intent(this@NotificationActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val searchbtn = findViewById<ImageView>(R.id.ivSearch)

        searchbtn.setOnClickListener {
            val intent = Intent(this@NotificationActivity, SearchActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}