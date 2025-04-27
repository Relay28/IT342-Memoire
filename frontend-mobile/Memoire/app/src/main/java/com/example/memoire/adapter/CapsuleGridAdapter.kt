package com.example.memoire.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.models.TimeCapsuleDTO

class CapsuleGridAdapter(
    private val capsules: List<TimeCapsuleDTO>,
    private val onCapsuleClick: (TimeCapsuleDTO) -> Unit
) : RecyclerView.Adapter<CapsuleGridAdapter.CapsuleViewHolder>() {

    inner class CapsuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      //  val imgCapsule: ImageView = itemView.findViewById(R.id.img_capsule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_capsule, parent, false)
        return CapsuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        val capsule = capsules[position]

//        // If the capsule has a cover image, load it with Glide
//        if (!capsule.coverImage.isNullOrEmpty()) {
//            Glide.with(holder.itemView.context)
//                .load(capsule.coverImage)
//                .placeholder(R.drawable.placeholder_capsule)
//                .error(R.drawable.placeholder_capsule)
//                .centerCrop()
//                .into(holder.imgCapsule)
//        } else {
//            // Use placeholder image
//            holder.imgCapsule.setImageResource(R.drawable.placeholder_capsule)
//        }
//
//        holder.itemView.setOnClickListener {
//            onCapsuleClick(capsule)
//        }
    }

    override fun getItemCount(): Int = capsules.size
}