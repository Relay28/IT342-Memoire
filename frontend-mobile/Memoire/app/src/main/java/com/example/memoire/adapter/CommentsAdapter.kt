package com.example.memoire.adapter

import CommentEntity
import ReactionRequest
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class CommentsAdapter(
    private var comments: List<CommentEntity>,
    private val currentUserId: Long,
    private val onDeleteComment: (Long) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.ivUserAvatar)
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val commentText: TextView = itemView.findViewById(R.id.tvCommentText)
        val commentDate: TextView = itemView.findViewById(R.id.tvCommentDate)
        val heartIcon: ImageView = itemView.findViewById(R.id.ivHeartIcon) // Add this
        val reactionCount: TextView = itemView.findViewById(R.id.tvReactionCount) // Add this


        fun bindComment(comment: CommentEntity) {
            username.text = comment.username ?: "Unknown User"
            commentText.text = comment.text

            val formattedDate = if (comment.createdAt != null) {
                dateFormat.format(comment.createdAt)
            } else {
                "Unknown date"
            }
            commentDate.text = formattedDate

            // Decode and render the user profile image
            comment.userProfileImage?.let { profilePic ->
                if (profilePic.isNotEmpty()) {
                    try {
                        val decodedBytes = android.util.Base64.decode(profilePic, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        avatar.setImageBitmap(bitmap)
                    } catch (e: IllegalArgumentException) {
                        avatar.setImageResource(R.drawable.ic_placeholder) // Fallback for invalid data
                    }
                } else {
                    avatar.setImageResource(R.drawable.ic_placeholder)
                }
            } ?: run {
                avatar.setImageResource(R.drawable.ic_placeholder)
            }

            // Long press to delete own comments
            if (comment.userId == currentUserId) {
                itemView.setOnLongClickListener {
                    showDeleteDialog(itemView.context, comment.id)
                    true
                }
            } else {
                itemView.setOnLongClickListener(null)
            }
        }
        private fun showDeleteDialog(context: Context, commentId: Long) {
            AlertDialog.Builder(context)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete") { dialog, _ ->
                    onDeleteComment(commentId)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bindComment(comment)

        // Fetch reaction data
        fetchReactionData(holder, comment)

        // Handle heart icon click
        holder.heartIcon.setOnClickListener {
            RetrofitClient.commentInstance.isReacted(comment.id).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val isReacted = response.body() ?: false
                        if (isReacted) {
                            // Delete reaction
                            RetrofitClient.commentInstance.deleteReaction(comment.id).enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        // Update UI to show unliked state
                                        holder.heartIcon.setImageResource(R.drawable.ic_heart1)
                                        fetchReactionData(holder, comment)
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    // Handle failure
                                }
                            })
                        } else {
                            // Add reaction
                            RetrofitClient.commentInstance.addReaction(comment.id, ReactionRequest("Like")).enqueue(object : Callback<Int> {
                                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                                    if (response.isSuccessful) {
                                        // Update UI to show liked state
                                        holder.heartIcon.setImageResource(R.drawable.ic_heart2)
                                        fetchReactionData(holder, comment)
                                    }
                                }

                                override fun onFailure(call: Call<Int>, t: Throwable) {
                                    // Handle failure
                                }
                            })
                        }
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    // Handle failure
                }
            })
        }
    }

    private fun fetchReactionData(holder: CommentViewHolder, comment: CommentEntity) {
        // Fetch reaction count
        RetrofitClient.commentInstance.getReactionCountByCommentId(comment.id).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    holder.reactionCount.text = response.body()?.toString() ?: "0"
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                holder.reactionCount.text = "0"
            }
        })

        // Fetch reaction status
        RetrofitClient.commentInstance.isReacted(comment.id).enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isReacted = response.body() ?: false
                    holder.heartIcon.setImageResource(if (isReacted) R.drawable.ic_heart2 else R.drawable.ic_heart1)
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                holder.heartIcon.setImageResource(R.drawable.ic_heart1)
            }
        })
    }
    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<CommentEntity>) {
        this.comments = newComments
        notifyDataSetChanged()
    }
}
