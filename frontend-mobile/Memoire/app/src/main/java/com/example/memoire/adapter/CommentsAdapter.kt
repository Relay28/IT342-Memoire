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
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.ReportDTO
import com.example.memoire.models.ReportRequest
import com.example.memoire.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
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
        val heartIcon: ImageView = itemView.findViewById(R.id.ivHeartIcon)
        val reactionCount: TextView = itemView.findViewById(R.id.tvReactionCount)

        fun bindComment(comment: CommentEntity) {
            username.text = comment.username ?: "Unknown User"
            commentText.text = comment.text

            val formattedDate = comment.createdAt?.let { dateFormat.format(it) } ?: "Unknown date"
            commentDate.text = formattedDate

            // Decode and render the user profile image
            comment.userProfileImage?.let { profilePic ->
                if (profilePic.isNotEmpty()) {
                    try {
                        val decodedBytes = android.util.Base64.decode(profilePic, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        avatar.setImageBitmap(bitmap)
                    } catch (e: IllegalArgumentException) {
                        avatar.setImageResource(R.drawable.ic_placeholder)
                    }
                } else {
                    avatar.setImageResource(R.drawable.ic_placeholder)
                }
            } ?: run {
                avatar.setImageResource(R.drawable.ic_placeholder)
            }

            // Long press to show options
            itemView.setOnLongClickListener {
                showOptionsDialog(itemView.context, comment)
                true
            }
        }

        private fun showOptionsDialog(context: Context, comment: CommentEntity) {
            val options = mutableListOf<String>()

            // Add "Delete" option only if the comment belongs to the current user
            if (comment.userId == currentUserId) {
                options.add("Delete")
            }

            // Add "Report" option for all comments
            options.add("Report")

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Choose an action")
            builder.setItems(options.toTypedArray()) { dialog, which ->
                when (options[which]) {
                    "Delete" -> {
                        onDeleteComment(comment.id)
                        dialog.dismiss()
                    }
                    "Report" -> {
                        reportComment(context, comment)
                        dialog.dismiss()
                    }
                }
            }
            builder.create().show()
        }

        private fun reportComment(context: Context, comment: CommentEntity) {
            val sessionManager = SessionManager(context)
            val reporterId = sessionManager.getUserSession()["userId"] as? Long ?: return

            // Create the ReportRequest with the required values
            val reportRequest = ReportRequest(
                reporterId = reporterId,
                reportedID = comment.id,
                itemType = "Comment",
                status = "" // Leave status as an empty string
            )

            // Use the provided endpoint to send the report
            RetrofitClient.instance.createReport(reportRequest).enqueue(object : Callback<ReportDTO> {
                override fun onResponse(call: Call<ReportDTO>, response: Response<ReportDTO>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Comment reported successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to report comment", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReportDTO>, t: Throwable) {
                    Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
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
        fetchReactionData(holder, comment)
        handleHeartIconClick(holder, comment)
    }

    private fun fetchReactionData(holder: CommentViewHolder, comment: CommentEntity) {
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

    private fun handleHeartIconClick(holder: CommentViewHolder, comment: CommentEntity) {
        holder.heartIcon.setOnClickListener {
            RetrofitClient.commentInstance.isReacted(comment.id).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val isReacted = response.body() ?: false
                        if (isReacted) {
                            RetrofitClient.commentInstance.deleteReaction(comment.id).enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        holder.heartIcon.setImageResource(R.drawable.ic_heart1)
                                        fetchReactionData(holder, comment)
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {}
                            })
                        } else {
                            RetrofitClient.commentInstance.addReaction(comment.id, ReactionRequest("Like")).enqueue(object : Callback<Int> {
                                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                                    if (response.isSuccessful) {
                                        holder.heartIcon.setImageResource(R.drawable.ic_heart2)
                                        fetchReactionData(holder, comment)
                                    }
                                }

                                override fun onFailure(call: Call<Int>, t: Throwable) {}
                            })
                        }
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {}
            })
        }
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<CommentEntity>) {
        this.comments = newComments
        notifyDataSetChanged()
    }
}