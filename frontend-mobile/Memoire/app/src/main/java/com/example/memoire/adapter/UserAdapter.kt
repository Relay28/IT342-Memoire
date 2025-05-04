package com.example.memoire.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.activities.UserProfileActivity
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.ProfileDTO2

class UserAdapter(private var userList: List<ProfileDTO2>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUserName: TextView = itemView.findViewById(R.id.txtUserName)
        val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.txtUserName.text = user.username

        // Use the itemView's context for Glide
        val context = holder.itemView.context

        // Set default image first, in case of any issues
        holder.imgUser.setImageResource(R.drawable.ic_placeholder)

        try {
            // Only proceed if profilePicture is not null and not empty
            if (user.profilePicture != null && user.profilePicture.isNotEmpty()) {
                val profileImageBytes = Base64.decode(user.profilePicture, Base64.DEFAULT)

                // Create a bitmap from the byte array
                val bitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.size)

                // Load the bitmap with Glide only if bitmap is not null
                if (bitmap != null) {
                    Glide.with(context)
                        .load(bitmap)
                        .circleCrop()
                        .placeholder(R.drawable.ic_placeholder)
                        .into(holder.imgUser)
                }
            }
        } catch (e: Exception) {
            Log.e("UserAdapter", "Error loading profile image for user ${user.username}: ${e.message}", e)
            // Image remains as default placeholder set earlier
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("userId", user.userId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<ProfileDTO2>) {
        userList = newList
        notifyDataSetChanged()
    }
}