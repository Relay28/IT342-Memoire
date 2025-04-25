
package com.example.memoire

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.adapter.LockedCapsuleAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.CountdownDTO
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask

class LockedCapsulesActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LockedCapsuleAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateView: TextView
    private var timer: Timer? = null
    private lateinit var sessionManager : SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_locked_capsules)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        setupViews()
        loadLockedCapsules()
        setupHeaderActions()
        setupBottomNavigation(R.id.navigation_tags)
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewLockedCapsules)
        progressBar = findViewById(R.id.progressBar1)
        emptyStateView = findViewById(R.id.emptyStateText)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LockedCapsuleAdapter(
            mutableListOf(),
            { capsuleId -> /* Handle capsule click if needed */ },
            { capsuleId -> showUnlockConfirmation(capsuleId) } ,
            {sessionManager}// Add unlock handler
        )
        recyclerView.adapter = adapter
    }

    private fun showUnlockConfirmation(capsuleId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Unlock")
            .setMessage("Are you sure you want to unlock this time capsule? This will cancel the locking and allow immediate access.")
            .setPositiveButton("Yes, Unlock") { _, _ ->
                unlockCapsule(capsuleId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun unlockCapsule(capsuleId: Long) {
        showLoading()
        RetrofitClient.instance.unlockTimeCapsule(capsuleId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                hideLoading()
                if (response.isSuccessful) {
                    // Remove the unlocked capsule from the list
                    adapter.removeCapsule(capsuleId)

                    // Show success message
                    Toast.makeText(
                        this@LockedCapsulesActivity,
                        "Capsule unlocked successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // If list is now empty, show empty state
                    if (adapter.itemCount == 0) {
                        showEmptyState("You have no locked capsules")
                    }
                } else {
                    // Show error message
                    Toast.makeText(
                        this@LockedCapsulesActivity,
                        "Failed to unlock capsule",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                hideLoading()
                showError("Network error. Please try again")
            }
        })
    }

    // Rest of the existing methods remain the same

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

    override fun showLoading() {
        progressBar.isVisible = true
        recyclerView.isVisible = false
        emptyStateView.isVisible = false
    }

    override fun hideLoading() {
        progressBar.isVisible = false
    }

    override fun showError(message: String) {
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