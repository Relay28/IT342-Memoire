package com.example.memoire

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.ProfileActivity
import com.example.memoire.R
import com.example.memoire.adapter.TimeCapsuleAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.com.example.memoire.CreateCapsuleActivity
import com.example.memoire.com.example.memoire.HomeActivity
import com.example.memoire.com.example.memoire.NotificationActivity
import com.example.memoire.com.example.memoire.SearchActivity
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CapsuleListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeCapsuleAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateView: TextView
    private lateinit var chipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_capsule_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewCapsules)
        progressBar = findViewById(R.id.progressBar)
        emptyStateView = findViewById(R.id.emptyStateText)
        chipGroup = findViewById(R.id.chipGroup)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TimeCapsuleAdapter(this, mutableListOf())
        recyclerView.adapter = adapter

        // Setup header actions
        setupHeaderActions()

        // Setup bottom navigation
        setupBottomNavigation()

        // Setup filter chips
        setupFilterChips()

        // Load user's time capsules by default
        loadUserTimeCapsules()
    }
    private fun setupHeaderActions() {
        val profile = findViewById<ImageView>(R.id.prof)
        profile.setOnClickListener {
            val intent = Intent(this@CapsuleListActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        val notificationBtn = findViewById<ImageView>(R.id.ivNotification)
        notificationBtn.setOnClickListener {
            val intent = Intent(this@CapsuleListActivity, NotificationActivity::class.java)
            startActivity(intent)
        }

        val searchBtn = findViewById<ImageView>(R.id.ivSearch)
        searchBtn.setOnClickListener {
            val intent = Intent(this@CapsuleListActivity, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_tags

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_add -> {
                    val intent = Intent(this, CreateCapsuleActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_tags -> {
                    // Already on this page
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFilterChips() {
        val chipAll = findViewById<Chip>(R.id.chipAll)
        val chipPublished = findViewById<Chip>(R.id.chipPublished)
        val chipClosed = findViewById<Chip>(R.id.chipClosed)
        val chipUnpublished = findViewById<Chip>(R.id.chipUnpublished)
        val chipArchived = findViewById<Chip>(R.id.chipArchived)

        chipAll.setOnClickListener { loadUserTimeCapsules() }
        chipPublished.setOnClickListener { loadCapsulesByStatus("PUBLISHED") }
        chipClosed.setOnClickListener { loadCapsulesByStatus("CLOSED") }
        chipUnpublished.setOnClickListener { loadCapsulesByStatus("UNPUBLISHED") }
        chipArchived.setOnClickListener { loadCapsulesByStatus("ARCHIVED") }
    }

    private fun loadUserTimeCapsules() {
        showLoading()
        RetrofitClient.instance.getUserTimeCapsules().enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                hideLoading()
                if (response.isSuccessful) {
                    val capsules = response.body() ?: emptyList()
                    updateUI(capsules)
                } else {
                    showError("Failed to load time capsules: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                hideLoading()
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun loadCapsulesByStatus(status: String) {
        showLoading()
        val apiCall = when (status) {
            "PUBLISHED" -> RetrofitClient.instance.getPublishedTimeCapsules()
            "CLOSED" -> RetrofitClient.instance.getClosedTimeCapsules()
            "UNPUBLISHED" -> RetrofitClient.instance.getUnpublishedTimeCapsules()
            "ARCHIVED" -> RetrofitClient.instance.getArchivedTimeCapsules()
            else -> RetrofitClient.instance.getUserTimeCapsules()
        }

        apiCall.enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                hideLoading()
                if (response.isSuccessful) {
                    val capsules = response.body() ?: emptyList()
                    updateUI(capsules)
                } else {
                    showError("Failed to load $status capsules: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                hideLoading()
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun updateUI(capsules: List<TimeCapsuleDTO>) {
        if (capsules.isEmpty()) {
            recyclerView.isVisible = false
            emptyStateView.isVisible = true
        } else {
            recyclerView.isVisible = true
            emptyStateView.isVisible = false
            adapter.updateData(capsules.toMutableList())
        }
    }

    private fun showLoading() {
        progressBar.isVisible = true
        recyclerView.isVisible = false
        emptyStateView.isVisible = false
    }

    private fun hideLoading() {
        progressBar.isVisible = false
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        emptyStateView.text = "Something went wrong. Please try again."
        emptyStateView.isVisible = true
        recyclerView.isVisible = false
    }
}