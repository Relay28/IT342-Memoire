package com.example.memoire.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.CapsuleDetailActivity
import com.example.memoire.R
import com.example.memoire.api.RetrofitClient
//import com.example.memoire.com.example.memoire.CapsuleDetailActivity
//import com.example.memoire.com.example.memoire.EditCapsuleActivity
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TimeCapsuleAdapter(private val context: Context, private var capsules: MutableList<TimeCapsuleDTO>) :
    RecyclerView.Adapter<TimeCapsuleAdapter.CapsuleViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    class CapsuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val createdDate: TextView = view.findViewById(R.id.tvCreatedDate)
        val openDate: TextView = view.findViewById(R.id.tvOpenDate)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val editButton: MaterialButton = view.findViewById(R.id.btnEdit)
        val deleteButton: ImageView = view.findViewById(R.id.ivDelete)
        val statusIcon: ImageView = view.findViewById(R.id.ivStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_capsule, parent, false)
        return CapsuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        val capsule = capsules[position]

        holder.title.text = capsule.title
        holder.description.text = capsule.description

        // Format dates
        holder.createdDate.text = "Created: ${capsule.createdAt?.let { dateFormat.format(it) } ?: "N/A"}"

        if (capsule.openDate != null) {
            holder.openDate.visibility = View.VISIBLE
            holder.openDate.text = "Scheduled to open: ${dateFormat.format(capsule.openDate)}"
        } else {
            holder.openDate.visibility = View.GONE
        }

        // Set status and icon
        holder.status.text = capsule.status
        when (capsule.status) {
            "UNPUBLISHED" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_unpublished)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
            }
            "CLOSED" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_locked)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
            }
            "PUBLISHED" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_published)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
            }
            "ARCHIVED" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_archived)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
            }
        }

        // Set click listeners
        holder.cardView.setOnClickListener {
            val intent = Intent(context, CapsuleDetailActivity::class.java)
            intent.putExtra("capsuleId", capsule.id.toString())  // Make sure the key matches and convert to string
            context.startActivity(intent)
        }

        holder.editButton.setOnClickListener {
            if (capsule.locked) {
                Toast.makeText(context, "Cannot edit locked capsules", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(context, CapsuleDetailActivity::class.java)
                intent.putExtra("CAPSULE_ID", capsule.id)
                context.startActivity(intent)
            }
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(capsule, position)
        }
    }

    override fun getItemCount() = capsules.size

    fun updateData(newCapsules: MutableList<TimeCapsuleDTO>) {
        capsules = newCapsules
        notifyDataSetChanged()
    }

    private fun showDeleteConfirmationDialog(capsule: TimeCapsuleDTO, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Time Capsule")
            .setMessage("Are you sure you want to delete '${capsule.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteTimeCapsule(capsule.id!!, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTimeCapsule(capsuleId: Long, position: Int) {
        RetrofitClient.instance.deleteTimeCapsule(capsuleId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    capsules.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "Time capsule deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete time capsule", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}