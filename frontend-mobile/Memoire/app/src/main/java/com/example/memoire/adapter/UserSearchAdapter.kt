package com.example.memoire.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
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
            if (user.profilePicture != null) {
                try {
                    val profileImageBytes = Base64.decode(user.profilePicture, Base64.DEFAULT)

                    // Create a bitmap from the byte array
                    val bitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.size)

                    // Load the bitmap with Glide
                    if (bitmap != null) {
                        Glide.with(root.context)
                            .load(bitmap)  // Load the bitmap directly
                            .circleCrop()
                            .placeholder(R.drawable.ic_placeholder)
                            .into(imageProfile)
                    } else {
                        imageProfile.setImageResource(R.drawable.ic_placeholder)
                    }
                } catch (e: Exception) {
                    Log.e("UserAdapter", "Error processing profile image", e)
                    imageProfile.setImageResource(R.drawable.ic_placeholder)
                }
            } else {
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