//package com.example.memoire.com.example.memoire
//
//import android.content.Intent
//import android.os.Build
//import android.os.Bundle
//import android.view.View
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.memoire.BaseActivity
//import com.example.memoire.CapsuleDetailActivity
//import com.example.memoire.CapsuleListActivity
//import com.example.memoire.CommentsDialog
//import com.example.memoire.LockedCapsulesActivity
//import com.example.memoire.ProfileActivity
//import com.example.memoire.R
//import com.example.memoire.activities.SearchActivity
//import com.example.memoire.adapter.PublishedCapsulesAdapter
//import com.example.memoire.api.RetrofitClient
//import com.example.memoire.models.TimeCapsuleDTO
//import com.example.memoire.utils.SessionManager
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class HomeActivity : BaseActivity() {
//
//    private lateinit var recyclerView: RecyclerView
//    private var adapter: PublishedCapsulesAdapter? = null
//    private var publishedCapsules: List<TimeCapsuleDTO> = emptyList()
//    private lateinit var emptyStateText: TextView
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_home)
//        handleNotificationIntent(intent)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_home)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//        //Header and Bottom nav
//        setupHeaderActions()
//        setupBottomNavigation(R.id.navigation_home)
//
//
//        fetchPublishedCapsules()
//
//        requestNotificationPermission()
//        val profile = findViewById<ImageView>(R.id.prof)
//
//        profile.setOnClickListener{
//            val intent = Intent(this@HomeActivity,ProfileActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//        // Initialize RecyclerView
//        recyclerView = findViewById(R.id.rvPublishedCapsules)
//        emptyStateText = findViewById(R.id.emptyStateText)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter = PublishedCapsulesAdapter(
//            emptyList(),
//            onItemClick = { capsule ->
//                // Navigate to capsule detail
//                val intent = Intent(this@HomeActivity, CapsuleDetailActivity::class.java)
//                intent.putExtra("capsuleId", capsule.id.toString())
//                startActivity(intent)
//            },
//            onCommentClick = { capsule ->
//                // Show comments dialog
//                showCommentsDialog(capsule)
//            }
//        )
//        recyclerView.adapter = adapter
//
//
//
//
//
//        val notificationBtn = findViewById<ImageView>(R.id.ivNotification)
//
//        notificationBtn.setOnClickListener {
//            val intent = Intent(this@HomeActivity, NotificationActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        val searchbtn = findViewById<ImageView>(R.id.ivSearch)
//
//        searchbtn.setOnClickListener {
//            val intent = Intent(this@HomeActivity, SearchActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//
//
//    }
//
//    private fun requestNotificationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            requestPermissions(
//                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
//                1001  // Random request code
//            )
//        }
//    }
//
//    private fun showCommentsDialog(capsule: TimeCapsuleDTO) {
//        // Get current user ID from session
//        val sessionManager = SessionManager(this)
//        val userId = sessionManager.getUserSession()["userId"] as Long  ?: -1L
//
//        // Show the comments dialog
//        CommentsDialog(this, capsule, userId).show()
//    }
//    private fun fetchPublishedCapsules() {
//        val call = RetrofitClient.instance.getPublishedTimeCapsules()
//        call.enqueue(object : Callback<List<TimeCapsuleDTO>> {
//            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
//                if (response.isSuccessful) {
//                    response.body()?.let { capsules ->
//                        publishedCapsules = capsules
//                        // Update the existing adapter instead of creating a new one
//                        adapter.updateData(capsules)
//                        updateEmptyState()
//                    }
//                } else {
//                    Toast.makeText(this@HomeActivity, "Failed to load capsules", Toast.LENGTH_SHORT).show()
//                    updateEmptyState()
//                }
//            }
//
//            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
//                Toast.makeText(this@HomeActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
//                updateEmptyState()
//            }
//        })
//    }
//    private fun updateEmptyState() {
//        if (publishedCapsules.isEmpty()) {
//            emptyStateText.visibility = View.VISIBLE
//            recyclerView.visibility = View.GONE
//        } else {
//            emptyStateText.visibility = View.GONE
//            recyclerView.visibility = View.VISIBLE
//        }
//    }
//
//    private fun handleNotificationIntent(intent: Intent?) {
//        intent?.extras?.let { extras ->
//            val type = extras.getString("type")
//            val itemId = extras.getString("itemId")
//            val itemType = extras.getString("itemType")
//
//            when (type) {
////                "FRIEND_REQUEST" -> navigateToFriendRequests()
////                "TIME_CAPSULE_OPEN" -> navigateToTimeCapsule(itemId)
//                // other cases
//                else -> {}
//            }
//        }
//    }
//}