package com.example.memoire

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.adapters.FriendsAdapter
import com.example.memoire.models.UserEntity
import com.example.memoire.viewmodels.FriendListViewModel

class FriendsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var viewModel: FriendListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        recyclerView = view.findViewById(R.id.rvFriends)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(requireActivity())[FriendListViewModel::class.java]

        friendsAdapter = FriendsAdapter(
            onRemoveFriend = { user ->
                viewModel.findFriendshipById(user.id).observe(viewLifecycleOwner) { friendship ->
                    if (friendship != null) {
                        viewModel.deleteFriendship(friendship.id)
                    }
                }
            }
        )

        recyclerView.adapter = friendsAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            println("Friends list updated: ${friends.size} items") // Debug log
            friendsAdapter.updateItems(friends)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.fetchFriends()
    }
}