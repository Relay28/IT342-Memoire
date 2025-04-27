package com.example.memoire

import CommentEntity
import CommentRequest
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.adapter.CommentsAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.databinding.DialogCommentsBinding
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class CommentsDialog(
    private val context: Context,
    private val capsule: TimeCapsuleDTO,
    private val currentUserId: Long
) {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var commentsAdapter: CommentsAdapter
    private var comments: MutableList<CommentEntity> = mutableListOf()

    fun show() {
        dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_comments, null)
        dialog.setContentView(view)

        setupUI(view)
        loadComments()

        dialog.show()
    }

    private fun setupUI(view: View) {
        // Set up the original post preview
        val tvPostTitle = view.findViewById<TextView>(R.id.tvPostTitle)
        val tvPostDescription = view.findViewById<TextView>(R.id.tvPostDescription)

        tvPostTitle.text = capsule.title ?: "Untitled Memory"
        tvPostDescription.text = capsule.description ?: "No description"

        // Set up the close button
        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        // Set up the RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvComments)
        recyclerView.layoutManager = LinearLayoutManager(context)

        commentsAdapter = CommentsAdapter(comments, currentUserId) { commentId ->
            deleteComment(commentId)
        }

        recyclerView.adapter = commentsAdapter

        // Set up the comment input
        val etComment = view.findViewById<EditText>(R.id.etComment)
        val btnSendComment = view.findViewById<ImageButton>(R.id.btnSendComment)

        btnSendComment.setOnClickListener {
            val commentText = etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                postComment(commentText)
                etComment.text.clear()
            }
        }
    }

    private fun loadComments() {
        capsule.id?.let { capsuleId ->
            RetrofitClient.commentInstance.getCommentsByCapsule(capsuleId)
                .enqueue(object : Callback<List<CommentEntity>> {
                    override fun onResponse(call: Call<List<CommentEntity>>, response: Response<List<CommentEntity>>) {
                        if (response.isSuccessful) {
                            val fetchedComments = response.body() ?: emptyList()
                            comments.clear()
                            comments.addAll(fetchedComments)
                            commentsAdapter.updateComments(comments)

                            // Update empty state visibility
                            dialog.findViewById<TextView>(R.id.tvNoComments)?.visibility =
                                if (comments.isEmpty()) View.VISIBLE else View.GONE
                        } else {
                            Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<CommentEntity>>, t: Throwable) {
                        Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun postComment(text: String) {
        capsule.id?.let { capsuleId ->
            val commentRequest = CommentRequest(text)

            RetrofitClient.commentInstance.createComment(capsuleId, commentRequest)
                .enqueue(object : Callback<CommentEntity> {
                    override fun onResponse(call: Call<CommentEntity>, response: Response<CommentEntity>) {
                        if (response.isSuccessful) {
                            response.body()?.let { newComment ->
                                // Add the new comment to our list and update the adapter
                                comments.add(newComment)
                                commentsAdapter.updateComments(comments)

                                // Hide empty state if this was the first comment
                                dialog.findViewById<TextView>(R.id.tvNoComments)?.visibility = View.GONE

                                // Scroll to the new comment
                                dialog.findViewById<RecyclerView>(R.id.rvComments)?.scrollToPosition(comments.size - 1)
                            }
                        } else {
                            Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CommentEntity>, t: Throwable) {
                        Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun deleteComment(commentId: Long) {
        RetrofitClient.commentInstance.deleteComment(commentId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        // Remove the comment from our list
                        comments.removeIf { it.id == commentId }
                        commentsAdapter.updateComments(comments)

                        // Show empty state if we deleted the last comment
                        dialog.findViewById<TextView>(R.id.tvNoComments)?.visibility =
                            if (comments.isEmpty()) View.VISIBLE else View.GONE

                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}