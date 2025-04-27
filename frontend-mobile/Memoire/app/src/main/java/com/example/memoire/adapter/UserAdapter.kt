package com.example.memoire.adapters

import android.content.Intent
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

        val profileUrl = user.profilePicture
        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(profileUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .circleCrop()
                .into(holder.imgUser)
        } else {
            holder.imgUser.setImageResource(R.drawable.ic_placeholder)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
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