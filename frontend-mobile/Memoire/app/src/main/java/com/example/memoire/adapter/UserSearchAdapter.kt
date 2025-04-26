package com.example.memoire.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.databinding.ItemUserSearchBinding
import com.example.memoire.models.UserSearchDTO
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
            // Hide name field since we're not using it
           // textName.visibility = View.GONE

            // Load profile picture if available
            user.profilePicture?.let { url ->
                Glide.with(root.context)
                    .load(url)
                    .circleCrop()
                    .into(imageProfile)
            }

            root.setOnClickListener { onUserSelected(user) }
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<UserSearchDTO>) {
        users = newUsers
        notifyDataSetChanged()
    }
}