package com.example.memoire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.adapters.FriendRequestsAdapter
import com.example.memoire.viewmodels.FriendListViewModel

class FriendRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoRequests: TextView
    private lateinit var friendRequestsAdapter: FriendRequestsAdapter
    private lateinit var viewModel: FriendListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_requests, container, false)

        recyclerView = view.findViewById(R.id.rvFriendRequests)
        tvNoRequests = view.findViewById(R.id.tvNoRequests)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(requireActivity())[FriendListViewModel::class.java]

        friendRequestsAdapter = FriendRequestsAdapter(
            onAccept = { friendship ->
                viewModel.acceptFriendship(friendship.id)
            },
            onDecline = { friendship ->
                viewModel.deleteFriendship(friendship.id)
            }
        )

        recyclerView.adapter = friendRequestsAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.friendRequests.observe(viewLifecycleOwner) { requests ->
            friendRequestsAdapter.updateItems(requests)

            // Show/hide the "No requests" message
            if (requests.isEmpty()) {
                tvNoRequests.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvNoRequests.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.fetchFriendRequests()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        viewModel.fetchFriendRequests()
    }
}