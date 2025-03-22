package com.example.memoire

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memoire.com.example.memoire.HomeActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back = findViewById<ImageView>(R.id.btn_back)
        val goEdit = findViewById<AppCompatButton>(R.id.btn_edit_profile)

        goEdit.setOnClickListener{
            val intent1 = Intent(this@ProfileActivity,EditProfileActivity::class.java)
            startActivity(intent1)
        }
        back.setOnClickListener{
            val intent = Intent(this@ProfileActivity,HomeActivity::class.java)
            startActivity(intent)
        }
    }
}