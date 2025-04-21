package com.example.memoire.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.models.CountdownDTO
import com.example.memoire.models.TimeCapsuleDTO

class LockedCapsuleAdapter(
    private var capsules: MutableList<TimeCapsuleDTO>,
    private val onCapsuleClick: (Long) -> Unit
) : RecyclerView.Adapter<LockedCapsuleAdapter.LockedCapsuleViewHolder>() {

    private val countdowns = mutableMapOf<Long, CountdownDTO>()

    inner class LockedCapsuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val days: TextView = itemView.findViewById(R.id.tvDays)
        val hours: TextView = itemView.findViewById(R.id.tvHours)
        val minutes: TextView = itemView.findViewById(R.id.tvMinutes)
        val seconds: TextView = itemView.findViewById(R.id.tvSeconds)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockedCapsuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_locked_capsule, parent, false)
        return LockedCapsuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: LockedCapsuleViewHolder, position: Int) {
        val capsule = capsules[position]
        holder.title.text = capsule.title

        countdowns[capsule.id]?.let { countdown ->
            if (countdown.isOpen) {
                setReadyToOpenState(holder)
            } else {
                setCountdownState(holder, countdown)
            }
        } ?: setLoadingState(holder)

        holder.itemView.setOnClickListener { onCapsuleClick(capsule.id!!.toLong()) }
    }

    private fun setReadyToOpenState(holder: LockedCapsuleViewHolder) {
        holder.days.text = "00"
        holder.hours.text = "00"
        holder.minutes.text = "00"
        holder.seconds.text = "00"
        holder.status.text = holder.itemView.context.getString(R.string.ready_to_open)
    }

    private fun setCountdownState(holder: LockedCapsuleViewHolder, countdown: CountdownDTO) {
        holder.days.text = countdown.days.toString().padStart(2, '0')
        holder.hours.text = countdown.hours.toString().padStart(2, '0')
        holder.minutes.text = countdown.minutes.toString().padStart(2, '0')
        holder.seconds.text = countdown.seconds.toString().padStart(2, '0')
        holder.status.text = holder.itemView.context.getString(R.string.time_until_unlock)
    }

    private fun setLoadingState(holder: LockedCapsuleViewHolder) {
        holder.days.text = "--"
        holder.hours.text = "--"
        holder.minutes.text = "--"
        holder.seconds.text = "--"
        holder.status.text = holder.itemView.context.getString(R.string.loading)
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
}