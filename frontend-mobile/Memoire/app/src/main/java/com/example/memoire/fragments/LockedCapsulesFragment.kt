package com.example.memoire.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
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

class LockedCapsulesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LockedCapsuleAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateView: TextView
    private var timer: Timer? = null
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_locked_capsules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        setupViews()
        loadLockedCapsules()
    }

    private fun setupViews() {
        recyclerView = requireView().findViewById(R.id.recyclerViewLockedCapsules)
        progressBar = requireView().findViewById(R.id.progressBar1)
        emptyStateView = requireView().findViewById(R.id.emptyStateText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LockedCapsuleAdapter(
            mutableListOf(),
            { capsuleId -> /* Handle capsule click if needed */ },
            { capsuleId -> showUnlockConfirmation(capsuleId) },
            { sessionManager }
        )
        recyclerView.adapter = adapter
    }

    private fun showUnlockConfirmation(capsuleId: Long) {
        AlertDialog.Builder(requireContext())
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
                    adapter.removeCapsule(capsuleId)
                    Toast.makeText(
                        requireContext(),
                        "Capsule unlocked successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (adapter.itemCount == 0) {
                        showEmptyState("You have no locked capsules")
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
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

    private fun loadLockedCapsules() {
        showLoading()
        RetrofitClient.instance.getClosedTimeCapsules().enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(
                call: Call<List<TimeCapsuleDTO>>,
                response: Response<List<TimeCapsuleDTO>>
            ) {
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
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
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
            override fun onResponse(
                call: Call<CountdownDTO>,
                response: Response<CountdownDTO>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { countdown ->
                        if (countdown.isOpen) {
                            loadLockedCapsules()
                        } else {
                            adapter.updateCountdownAtPosition(position, countdown)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<CountdownDTO>, t: Throwable) {
                // Silently fail
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        showEmptyState(message)
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized && adapter.itemCount > 0) {
            startCountdownUpdates()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}