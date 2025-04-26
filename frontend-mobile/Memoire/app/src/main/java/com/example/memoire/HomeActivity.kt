package com.example.memoire.com.example.memoire

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.BaseActivity
import com.example.memoire.CapsuleDetailActivity
import com.example.memoire.CapsuleListActivity
import com.example.memoire.LockedCapsulesActivity
import com.example.memoire.ProfileActivity
import com.example.memoire.R
import com.example.memoire.adapter.PublishedCapsulesAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PublishedCapsulesAdapter
    private var publishedCapsules: List<TimeCapsuleDTO> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        handleNotificationIntent(intent)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestNotificationPermission()
        val profile = findViewById<ImageView>(R.id.prof)

        profile.setOnClickListener{
            val intent = Intent(this@HomeActivity,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rvPublishedCapsules)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PublishedCapsulesAdapter(emptyList()) { /* No click action needed */ }
        recyclerView.adapter = adapter





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
        setupHeaderActions()
        setupBottomNavigation(R.id.navigation_tags)
        fetchPublishedCapsules()


    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001  // Random request code
            )
        }
    }

    private fun fetchPublishedCapsules() {
        val call = RetrofitClient.instance.getPublishedTimeCapsules()
        call.enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                if (response.isSuccessful) {
                    response.body()?.let { capsules ->
                        publishedCapsules = capsules
                        adapter.updateData(capsules)
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load capsules", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun handleNotificationIntent(intent: Intent?) {
        intent?.extras?.let { extras ->
            val type = extras.getString("type")
            val itemId = extras.getString("itemId")
            val itemType = extras.getString("itemType")

            when (type) {
//                "FRIEND_REQUEST" -> navigateToFriendRequests()
//                "TIME_CAPSULE_OPEN" -> navigateToTimeCapsule(itemId)
                // other cases
                else -> {}
            }
        }
    }
}