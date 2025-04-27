package com.example.memoire.adapter

import CommentEntity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
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

        fun bindComment(comment: CommentEntity) {
            username.text = comment.user?.username ?: "Unknown User"
            commentText.text = comment.text

            val formattedDate = if (comment.createdAt != null) {
                dateFormat.format(comment.createdAt)
            } else {
                "Unknown date"
            }
            commentDate.text = formattedDate

            // If we have a profile picture, load it with Glide or similar library
            comment.user?.profilePicture?.let { profilePic ->
                if (profilePic.isNotEmpty()) {
                    // Example with Glide (you would need to add the Glide dependency)
                    // Glide.with(itemView.context)
                    //     .load(profilePic)
                    //     .circleCrop()
                    //     .into(avatar)
                } else {
                    avatar.setImageResource(R.drawable.ic_placeholder)
                }
            } ?: run {
                avatar.setImageResource(R.drawable.ic_placeholder)
            }

            // Long press to delete own comments
            if (comment.user?.id == currentUserId) {
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
        holder.bindComment(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<CommentEntity>) {
        this.comments = newComments
        notifyDataSetChanged()
    }
}
