package com.example.memoire.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.CapsuleDetailActivity
import com.example.memoire.CommentsDialog
import com.example.memoire.FriendRequestsFragment
import com.example.memoire.R
import com.example.memoire.adapter.PublishedCapsulesAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private var adapter: PublishedCapsulesAdapter? = null
    private var publishedCapsules: List<TimeCapsuleDTO> = emptyList()
    private lateinit var emptyStateText: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        handleNotificationIntent(requireActivity().intent)

        // Initialize views
        recyclerView = view.findViewById(R.id.rvPublishedCapsules)
        emptyStateText = view.findViewById(R.id.emptyStateText)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PublishedCapsulesAdapter(
            emptyList(),
            onItemClick = { capsule ->
                // Navigate to capsule detail
                val intent = Intent(requireActivity(), CapsuleDetailActivity::class.java)
                intent.putExtra("capsuleId", capsule.id.toString())
                startActivity(intent)
            },
            onCommentClick = { capsule ->
                // Show comments dialog
                showCommentsDialog(capsule)
            }
        )
        recyclerView.adapter = adapter

        fetchPublishedCapsules()
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    private fun showCommentsDialog(capsule: TimeCapsuleDTO) {
        val userId = sessionManager.getUserSession()["userId"] as Long ?: -1L
        CommentsDialog(requireContext(), capsule, userId).show()
    }

    private fun fetchPublishedCapsules() {
        val call = RetrofitClient.instance.getPublishedTimeCapsules()
        call.enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(
                call: Call<List<TimeCapsuleDTO>>,
                response: Response<List<TimeCapsuleDTO>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { capsules ->
                        publishedCapsules = capsules
                        adapter?.updateData(capsules)
                        updateEmptyState()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load capsules",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateEmptyState()
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                updateEmptyState()
            }
        })
    }

    private fun updateEmptyState() {
        if (publishedCapsules.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.extras?.let { extras ->
            val type = extras.getString("type")

            when (type) {
                "FRIEND_REQUEST" -> {
                    // Navigate to FriendRequestsFragment
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FriendRequestsFragment())
                        .addToBackStack(null)
                        .commit()
                }
                "PUBLISHED_CAPSULE" -> {

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                }
                else -> {
                    // Handle other types if needed
                }
            }
        }
    }
}