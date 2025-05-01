package com.example.memoire.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.databinding.ItemUserSearchBinding
import com.example.memoire.models.UserSearchDTO
import com.example.memoire.extensions.dpToPx

class UserSearchAdapter(
    private var users: List<UserSearchDTO>,
    private val onUserSelected: (UserSearchDTO) -> Unit
) : RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemUserSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserSearchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        with(holder.binding) {
            textUsername.text = user.username
            textEmail.text = user.email

            // Load profile picture if available
            user.profilePicture?.let { byteArray ->
                Glide.with(root.context)
                    .load(byteArray)
                    .circleCrop()
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imageProfile)
            } ?: run {
                imageProfile.setImageResource(R.drawable.ic_placeholder)
            }
            // Set a reasonable min height but let the constraints handle the actual layout
            root.minimumHeight = 56.dpToPx(root.context)
            root.setOnClickListener { onUserSelected(user) }
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<UserSearchDTO>) {
        users = newUsers
        notifyDataSetChanged()
    }
}