package com.example.memoire

import android.os.Bundle
import android.view.View
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
import com.example.memoire.adapter.LockedCapsuleAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.CountdownDTO
import com.example.memoire.models.TimeCapsuleDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask

class LockedCapsulesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LockedCapsuleAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateView: TextView
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_locked_capsules)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        loadLockedCapsules()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewLockedCapsules)
        progressBar = findViewById(R.id.progressBar1)
        emptyStateView = findViewById(R.id.emptyStateText)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LockedCapsuleAdapter(mutableListOf()) { capsuleId ->
            // Handle capsule click if needed
        }
        recyclerView.adapter = adapter
    }

    private fun loadLockedCapsules() {
        showLoading()
        RetrofitClient.instance.getClosedTimeCapsules().enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                hideLoading()
                if (response.isSuccessful) {
                    val capsules = response.body() ?: emptyList()
                    if (capsules.isEmpty()) {
                        showEmptyState("You have no locked capsules")
                    } else {
                        showCapsules(capsules)
                        startCountdownUpdates()
                    }
                } else {
                    showError("Failed to load locked capsules")
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                hideLoading()
                showError("Network error. Please try again")
            }
        })
    }

    private fun startCountdownUpdates() {
        timer?.cancel()
        timer = Timer().apply {
            // Update immediately and then every second
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        updateAllCountdowns()
                    }
                }
            }, 0, 1000)
        }
    }

    private fun updateAllCountdowns() {
        for (i in 0 until adapter.itemCount) {
            val capsule = adapter.getItemAtPosition(i)
            updateSingleCountdown(capsule.id!!.toLong(), i)
        }
    }

    private fun updateSingleCountdown(capsuleId: Long, position: Int) {
        RetrofitClient.instance.getCountdown(capsuleId).enqueue(object : Callback<CountdownDTO> {
            override fun onResponse(call: Call<CountdownDTO>, response: Response<CountdownDTO>) {
                if (response.isSuccessful) {
                    response.body()?.let { countdown ->
                        if (countdown.isOpen) {
                            // Refresh the list if any capsule is ready to open
                            loadLockedCapsules()
                        } else {
                            adapter.updateCountdownAtPosition(position, countdown)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<CountdownDTO>, t: Throwable) {
                // Silently fail - we'll try again on next update
            }
        })
    }

    private fun showCapsules(capsules: List<TimeCapsuleDTO>) {
        adapter.updateData(capsules.toMutableList())
        recyclerView.isVisible = true
        emptyStateView.isVisible = false
    }

    private fun showEmptyState(message: String) {
        recyclerView.isVisible = false
        emptyStateView.text = message
        emptyStateView.isVisible = true
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        showEmptyState(message)
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (adapter.itemCount > 0) {
            startCountdownUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}