
package com.example.memoire.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.models.CountdownDTO
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.utils.SessionManager
import com.google.android.material.button.MaterialButton

class LockedCapsuleAdapter(
    private var capsules: MutableList<TimeCapsuleDTO>,
    private val onCapsuleClick: (Long) -> Unit,
    private val onUnlockClick: (Long) -> Unit,
    private var sessionManager: () -> SessionManager
) : RecyclerView.Adapter<LockedCapsuleAdapter.LockedCapsuleViewHolder>() {

    private val countdowns = mutableMapOf<Long, CountdownDTO>()

    inner class LockedCapsuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val days: TextView = itemView.findViewById(R.id.tvDays)
        val hours: TextView = itemView.findViewById(R.id.tvHours)
        val minutes: TextView = itemView.findViewById(R.id.tvMinutes)
        val seconds: TextView = itemView.findViewById(R.id.tvSeconds)
        val openDate: TextView = itemView.findViewById(R.id.tvOpenDate)
        val unlockButton: MaterialButton = itemView.findViewById(R.id.btnUnlock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockedCapsuleViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_locked_capsule, parent, false)
        return LockedCapsuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: LockedCapsuleViewHolder, position: Int) {
        val capsule = capsules[position]
        holder.title.text = capsule.title
        holder.description.text = capsule.description

        // Set open date text
        capsule.openDate?.let {
            holder.openDate.text = "Opening on: ${it.toLocaleString()}"
        }

        // Check if current user is the owner to show/hide unlock button
        val isOwner = capsule.createdById == getCurrentUserId() // This method needs to be implemented
        holder.unlockButton.visibility = if (isOwner) View.VISIBLE else View.GONE

        // Set unlock button click listener
        holder.unlockButton.setOnClickListener {
            capsule.id?.let { id -> onUnlockClick(id.toLong()) }
        }

        // Update countdown UI
        countdowns[capsule.id]?.let { countdown ->
            if (countdown.isOpen) {
                setReadyToOpenState(holder)
            } else {
                setCountdownState(holder, countdown)
            }
        } ?: setLoadingState(holder)

        holder.itemView.setOnClickListener {
            capsule.id?.let { id -> onCapsuleClick(id.toLong()) }
        }
    }

    private fun getCurrentUserId(): Long {
        return sessionManager().getUserSession()["userId"] as Long
    }

    private fun setReadyToOpenState(holder: LockedCapsuleViewHolder) {
        holder.days.text = "00"
        holder.hours.text = "00"
        holder.minutes.text = "00"
        holder.seconds.text = "00"
        holder.openDate.text = holder.itemView.context.getString(R.string.ready_to_open)
    }

    private fun setCountdownState(holder: LockedCapsuleViewHolder, countdown: CountdownDTO) {
        holder.days.text = countdown.days.toString().padStart(2, '0')
        holder.hours.text = countdown.hours.toString().padStart(2, '0')
        holder.minutes.text = countdown.minutes.toString().padStart(2, '0')
        holder.seconds.text = countdown.seconds.toString().padStart(2, '0')
    }

    private fun setLoadingState(holder: LockedCapsuleViewHolder) {
        holder.days.text = "--"
        holder.hours.text = "--"
        holder.minutes.text = "--"
        holder.seconds.text = "--"
        holder.openDate.text = holder.itemView.context.getString(R.string.loading)
    }

    override fun getItemCount(): Int = capsules.size

    fun updateData(newCapsules: MutableList<TimeCapsuleDTO>) {
        capsules = newCapsules
        countdowns.clear()
        notifyDataSetChanged()
    }

    fun getItemAtPosition(position: Int): TimeCapsuleDTO = capsules[position]

    fun updateCountdownAtPosition(position: Int, countdown: CountdownDTO) {
        val capsuleId = capsules[position].id
        countdowns[capsuleId!!.toLong()] = countdown
        notifyItemChanged(position)
    }

    fun removeCapsule(capsuleId: Long) {
        val position = capsules.indexOfFirst { it.id == capsuleId }
        if (position != -1) {
            capsules.removeAt(position)
            countdowns.remove(capsuleId)
            notifyItemRemoved(position)
        }
    }
}