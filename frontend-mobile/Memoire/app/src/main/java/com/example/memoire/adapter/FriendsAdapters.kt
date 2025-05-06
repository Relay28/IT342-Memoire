package com.example.memoire.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.activities.UserProfileActivity
import com.example.memoire.models.UserEntity
import com.google.android.material.button.MaterialButton

class FriendsAdapter(
    private val onRemoveFriend: (UserEntity) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private var friends: List<UserEntity> = emptyList()

    fun updateItems(newItems: List<UserEntity>) {
        friends = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend2, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend, onRemoveFriend)
    }

    override fun getItemCount(): Int = friends.size

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfilePic: ImageView = itemView.findViewById(R.id.ivProfilePic)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val btnPrimary: MaterialButton = itemView.findViewById(R.id.btnPrimary)
        private val requestButtonsContainer: LinearLayout = itemView.findViewById(R.id.requestButtonsContainer)

        fun bind(
            friend: UserEntity,
            onRemoveFriend: (UserEntity) -> Unit
        ) {
            tvUsername.text = friend.username
            tvName.text = friend.name ?: "No name"

            // Show profile picture if available
            friend.profilePictureData?.let {
                try {
                    val imageBytes = Base64.decode(it, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ivProfilePic.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    ivProfilePic.setImageResource(R.drawable.ic_placeholder)
                }
            } ?: ivProfilePic.setImageResource(R.drawable.ic_placeholder)

            // Configure button
            btnPrimary.text = "View Profile"
            btnPrimary.setOnClickListener {
                val intent = Intent(itemView.context, UserProfileActivity::class.java).apply {
                    putExtra("userId", friend.id)
                }
                itemView.context.startActivity(intent)
            }

            // Long press to show remove option
            itemView.setOnLongClickListener {
                btnPrimary.text = "Remove"
                btnPrimary.setOnClickListener { onRemoveFriend(friend) }
                true
            }

            // Regular click redirects to profile
            itemView.setOnClickListener {
                if (btnPrimary.text == "Remove") {
                    btnPrimary.text = "View Profile"
                    btnPrimary.setOnClickListener {
                        val intent = Intent(itemView.context, UserProfileActivity::class.java).apply {
                            putExtra("userId", friend.id)
                        }
                        itemView.context.startActivity(intent)
                    }
                } else {
                    val intent = Intent(itemView.context, UserProfileActivity::class.java).apply {
                        putExtra("userId", friend.id)
                    }
                    itemView.context.startActivity(intent)
                }
            }

            // Make sure only the primary button is visible
            btnPrimary.visibility = View.VISIBLE
            requestButtonsContainer.visibility = View.GONE
        }
    }
}