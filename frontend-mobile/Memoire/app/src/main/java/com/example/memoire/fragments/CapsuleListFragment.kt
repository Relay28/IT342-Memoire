package com.example.memoire.fragments

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
import com.example.memoire.adapter.TimeCapsuleAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class CapsuleListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeCapsuleAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateView: TextView
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_capsule_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewCapsules)
        progressBar = view.findViewById(R.id.progressBar)
        emptyStateView = view.findViewById(R.id.emptyStateText)
        tabLayout = view.findViewById(R.id.tabLayout)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TimeCapsuleAdapter(requireContext(), mutableListOf())
        recyclerView.adapter = adapter

        // Setup tabs
        setupTabs()

        // Load "All" capsules by default
        loadAllCapsules()
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Unpublished"))
        tabLayout.addTab(tabLayout.newTab().setText("Archived"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "Unpublished" -> loadCapsulesByStatus("UNPUBLISHED")
                    "Archived" -> loadCapsulesByStatus("ARCHIVED")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadAllCapsules() {
        showLoading()
        RetrofitClient.instance.getUserTimeCapsules().enqueue(object :
            Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(
                call: Call<List<TimeCapsuleDTO>>,
                response: Response<List<TimeCapsuleDTO>>
            ) {
                hideLoading()
                if (response.isSuccessful) {
                    val allCapsules = response.body() ?: emptyList()
                    // Filter capsules to only include "Unpublished" and "Archived"
                    val filteredCapsules = allCapsules.filter {
                        it.status?.uppercase(Locale.getDefault()) in listOf("UNPUBLISHED")
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
            "UNPUBLISHED" -> RetrofitClient.instance.getUnpublishedTimeCapsules()
            "ARCHIVED" -> RetrofitClient.instance.getArchivedTimeCapsules()
            else -> RetrofitClient.instance.getUserTimeCapsules()
        }

        apiCall.enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(
                call: Call<List<TimeCapsuleDTO>>,
                response: Response<List<TimeCapsuleDTO>>
            ) {
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        emptyStateView.text = "Something went wrong. Please try again."
        emptyStateView.isVisible = true
        recyclerView.isVisible = false
    }
}