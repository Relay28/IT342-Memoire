package com.example.memoire.com.example.memoire

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memoire.CapsuleDetailActivity
import com.example.memoire.CapsuleListActivity
import com.example.memoire.ProfileActivity
import com.example.memoire.R
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
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
        val profile = findViewById<ImageView>(R.id.prof)

        profile.setOnClickListener{
            val intent = Intent(this@HomeActivity,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val notificationBtn = findViewById<ImageView>(R.id.ivNotification)

        notificationBtn.setOnClickListener {
            val intent = Intent(this@HomeActivity, NotificationActivity::class.java)
            startActivity(intent)
            finish()
        }

        val searchbtn = findViewById<ImageView>(R.id.ivSearch)

        searchbtn.setOnClickListener {
            val intent = Intent(this@HomeActivity, SearchActivity::class.java)
            startActivity(intent)
            finish()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Navigate to the Home activity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                /*R.id.navigation_stats -> {
                    // Navigate to the Stats activity
                    val intent = Intent(this, StatsActivity::class.java)
                    startActivity(intent)
                    true
                }*/
                R.id.navigation_add -> {


                    val newCapsule = TimeCapsuleDTO(
                        title = "Untitled",
                        description = ""
                    )

                    // Show a loading indicator if you have one
                    // progressBar.visibility = View.VISIBLE

                    RetrofitClient.instance.createTimeCapsule(newCapsule).enqueue(object : retrofit2.Callback<TimeCapsuleDTO> {
                        override fun onResponse(call: retrofit2.Call<TimeCapsuleDTO>, response: retrofit2.Response<TimeCapsuleDTO>) {
                            // Hide loading indicator
                            // progressBar.visibility = View.GONE

                            if (response.isSuccessful && response.body() != null) {
                                val createdCapsule = response.body()!!
                                // Navigate to detail activity with the new capsule ID
                                val intent = Intent(this@HomeActivity, CapsuleDetailActivity::class.java).apply {
                                    putExtra("capsuleId", createdCapsule.id.toString())
                                    // You might want to add a flag to indicate this is a new capsule
                                    putExtra("isNewCapsule", true)
                                }
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@HomeActivity,
                                    "Failed to create capsule: ${response.message()}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<TimeCapsuleDTO>, t: Throwable) {
                            // Hide loading indicator
                            // progressBar.visibility = View.GONE

                            Toast.makeText(this@HomeActivity,
                                "Error: ${t.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    })
                    true
                }
                R.id.navigation_tags -> {
                    // Navigate to the Tags activity
                    val intent = Intent(this, CapsuleListActivity::class.java)
                    startActivity(intent)
                    true
                }/*
                R.id.navigation_timer -> {
                    // Navigate to the Timer activity
                    val intent = Intent(this, TimerActivity::class.java)
                    startActivity(intent)
                    true
                }*/
                else -> false
            }
        }
    }
}