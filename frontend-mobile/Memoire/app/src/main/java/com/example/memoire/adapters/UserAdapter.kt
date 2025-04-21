package com.example.memoire.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.models.User

class UserAdapter(private var userList: List<User>) :
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
                .into(holder.imgUser)
        } else {
            holder.imgUser.setImageResource(R.drawable.ic_placeholder)
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}
