package com.example.memoire.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.CapsuleDetailActivity
import com.example.memoire.LockCapsuleDialogFragment
import com.example.memoire.R
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.LockRequest
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.utils.DateUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class TimeCapsuleAdapter(private val context: Context, private var capsules: MutableList<TimeCapsuleDTO>) :
    RecyclerView.Adapter<TimeCapsuleAdapter.CapsuleViewHolder>() {

    inner class CapsuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val createdDate: TextView = view.findViewById(R.id.tvCreatedDate)
        val openDate: TextView = view.findViewById(R.id.tvOpenDate)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val editButton: MaterialButton = view.findViewById(R.id.btnEdit)
        val publishButton: MaterialButton = view.findViewById(R.id.btnPublish)
        val deleteButton: ImageView = view.findViewById(R.id.ivDelete)
        val statusIcon: ImageView = view.findViewById(R.id.ivStatus)
        val viewDetailsButton: MaterialButton = view.findViewById(R.id.btnViewDetails)
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

        // Format dates using DateUtils
        holder.createdDate.text = "Created: ${capsule.createdAt?.let { DateUtils.formatDateForDisplay(it) } ?: "N/A"}"

        if (capsule.openDate != null) {
            holder.openDate.visibility = View.VISIBLE
            holder.openDate.text = "Scheduled to open: ${DateUtils.formatDateForDisplay(capsule.openDate)} at ${DateUtils.formatTimeForDisplay(capsule.openDate)}"
        } else {
            holder.openDate.visibility = View.GONE
        }

        // Set status and icon
        holder.status.text = capsule.status
        when (capsule.status?.uppercase(Locale.getDefault())) {
            "UNPUBLISHED" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_unpublished)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
                holder.publishButton.text = "Publish"
                holder.publishButton.isEnabled = true
            }
            "CLOSED" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_locked)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
                holder.publishButton.text = "Locked"
                holder.publishButton.isEnabled = false
            }
            "PUBLISHED" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_published)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
                holder.publishButton.text = "Published"
                holder.publishButton.isEnabled = false
            }
            "ARCHIVED" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_archived)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
                holder.publishButton.text = "Archived"
                holder.publishButton.isEnabled = false
            }
        }

        // Set click listeners
        holder.cardView.setOnClickListener {
            openCapsuleDetail(capsule.id)
        }

        holder.editButton.setOnClickListener {
            if (capsule.locked) {
                Toast.makeText(context, "Cannot edit locked capsules", Toast.LENGTH_SHORT).show()
            } else {
                openCapsuleDetail(capsule.id)
            }
        }

        holder.publishButton.setOnClickListener {
            when (capsule.status?.uppercase(Locale.getDefault())) {
                "UNPUBLISHED" -> showLockDialog(capsule.id!!.toLong())
                else -> Toast.makeText(context, "This capsule is already ${capsule.status?.lowercase()}", Toast.LENGTH_SHORT).show()
            }
        }

        holder.viewDetailsButton.setOnClickListener {
            openCapsuleDetail(capsule.id)
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

    private fun openCapsuleDetail(capsuleId: Long?) {
        capsuleId?.let {
            val intent = Intent(context, CapsuleDetailActivity::class.java)
            intent.putExtra("capsuleId", it.toString())
            context.startActivity(intent)
        }
    }

    private fun showLockDialog(capsuleId: Long) {
        val dialog = LockCapsuleDialogFragment { selectedDate ->
            lockTimeCapsule(capsuleId, selectedDate)
        }

        // Ensure we're using the FragmentManager from an AppCompatActivity
        if (context is AppCompatActivity) {
            dialog.show(context.supportFragmentManager, "LockCapsuleDialog")
        } else {
            Toast.makeText(context, "Error: Could not show lock dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun lockTimeCapsule(capsuleId: Long, openDate: Date) {
        // No need to convert - just send the date as local time
        // The formatForApi method already handles the conversion to UTC
        val formattedDate = DateUtils.formatForApi(openDate)

        // Log for debugging
        Log.d("TimeCapsule", "Local time selected: ${DateUtils.formatDateForDisplay(openDate)} ${DateUtils.formatTimeForDisplay(openDate)}")
        Log.d("TimeCapsule", "Formatted for API as: $formattedDate")

        // Create lock request with properly formatted date
        val lockRequest = LockRequest(openDate = formattedDate)

        RetrofitClient.instance.lockTimeCapsule(capsuleId, lockRequest).enqueue(
            object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Capsule locked successfully", Toast.LENGTH_SHORT).show()
                        refreshCapsuleData()
                    } else {
                        Toast.makeText(context, "Failed to lock capsule: ${response.code()}", Toast.LENGTH_SHORT).show()
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e("TimeCapsule", "Error body: $errorBody")
                        } catch (e: Exception) {
                            Log.e("TimeCapsule", "Could not read error body", e)
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("TimeCapsule", "Network error", t)
                }
            }
        )
    }

    private fun refreshCapsuleData() {
        // You might want to implement a callback to notify the activity to refresh data
        // For now, we'll just notify the adapter that data might have changed
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