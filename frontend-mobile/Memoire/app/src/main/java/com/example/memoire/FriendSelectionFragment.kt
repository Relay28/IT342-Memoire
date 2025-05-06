package com.example.memoire;

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.activities.UserProfileActivity
import com.example.memoire.databinding.DialogFriendSelectionBinding
import com.example.memoire.databinding.ItemFriendBinding
import com.example.memoire.models.UserEntity

class FriendSelectionDialogFragment(
    private val friends: List<UserEntity>,
    private val onFriendsSelected: (List<Long>) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogFriendSelectionBinding
    private val selectedFriends = mutableListOf<Long>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFriendSelectionBinding.inflate(layoutInflater)

        // Setup RecyclerView with friends list
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.friendsRecyclerView.adapter = FriendsAdapter(friends) { userId, isChecked ->
            if (isChecked) {
                selectedFriends.add(userId)
            } else {
                selectedFriends.remove(userId)
            }
        }

        binding.btnConfirm.setOnClickListener {
            onFriendsSelected(selectedFriends.toList())
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
            window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private inner class FriendsAdapter(
            private val friends: List<UserEntity>,
            private val onFriendSelected: (Long, Boolean) -> Unit
    ) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

        inner class FriendViewHolder(val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
            val binding = ItemFriendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
            )
            return FriendViewHolder(binding)
        }

        override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
            val friend = friends[position]
            holder.binding.friendName.text = friend.name ?: friend.username

            // Handle checkbox selection
            holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onFriendSelected(friend.id, isChecked)
            }

            // Handle item click to redirect to UserProfileActivity
            holder.binding.root.setOnClickListener {
                val intent = Intent(requireActivity(), UserProfileActivity::class.java).apply {
                    putExtra("userId", friend.id) // Pass the user ID to the activity
                }
                requireActivity().startActivity(intent) // Start the UserProfileActivity
            }
        }

        override fun getItemCount() = friends.size
    }
}
