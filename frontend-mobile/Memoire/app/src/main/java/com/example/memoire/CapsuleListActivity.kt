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
import java.util.Locale

class CapsuleListActivity : BaseActivity() {

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

        // Setup filter chips
        setupFilterChips()

        // Load user's time capsules by default
        loadUserTimeCapsules()
        setupHeaderActions()
        setupBottomNavigation(R.id.navigation_tags)
    }


    private fun setupFilterChips() {
        val chipAll = findViewById<Chip>(R.id.chipAll)
        val chipPublished = findViewById<Chip>(R.id.chipPublished)
        val chipUnpublished = findViewById<Chip>(R.id.chipUnpublished)

        chipAll.setOnClickListener { loadUserTimeCapsules() }
        chipPublished.setOnClickListener { loadCapsulesByStatus("PUBLISHED") }
        chipUnpublished.setOnClickListener { loadCapsulesByStatus("UNPUBLISHED") }
    }

    private fun loadUserTimeCapsules() {
        showLoading()
        RetrofitClient.instance.getUserTimeCapsules().enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                hideLoading()
                if (response.isSuccessful) {
                    val allCapsules = response.body() ?: emptyList()
                    // Filter out closed capsules
                    val filteredCapsules = allCapsules.filter {
                        it.status?.uppercase(Locale.getDefault()) != "CLOSED" &&
                                it.status?.uppercase(Locale.getDefault()) != "ARCHIVED"
                    }
                    updateUI(filteredCapsules)
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
            "UNPUBLISHED" -> RetrofitClient.instance.getUnpublishedTimeCapsules()
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

    override fun showLoading() {
        progressBar.isVisible = true
        recyclerView.isVisible = false
        emptyStateView.isVisible = false
    }

    override  fun hideLoading() {
        progressBar.isVisible = false
    }

   override  fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        emptyStateView.text = "Something went wrong. Please try again."
        emptyStateView.isVisible = true
        recyclerView.isVisible = false
    }
}