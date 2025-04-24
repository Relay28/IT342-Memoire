package com.example.memoire.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.models.CapsuleContentEntity
import com.example.memoire.models.TimeCapsuleDTO
import java.text.SimpleDateFormat
import java.util.Locale

class PublishedCapsulesAdapter(
    private var capsules: List<TimeCapsuleDTO>,
    private val onItemClick: (TimeCapsuleDTO) -> Unit
) : RecyclerView.Adapter<PublishedCapsulesAdapter.CapsuleViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    inner class CapsuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(capsule: TimeCapsuleDTO) {
            // Bind data to views
            itemView.findViewById<TextView>(R.id.tvCapsuleTitle).text = capsule.title ?: "Untitled Memory"
            itemView.findViewById<TextView>(R.id.tvCapsuleDescription).text = capsule.description ?: "No description"

            // Format dates
            val createdAt = capsule.createdAt?.let { dateFormat.format(it) } ?: "Unknown date"
            val openDate = capsule.openDate?.let { dateFormat.format(it) } ?: "Not specified"

            itemView.findViewById<TextView>(R.id.tvCreatedDate).text = "Created on: $createdAt"
            itemView.findViewById<TextView>(R.id.tvOpenedDate).text = "Opened on: $openDate"

            // Load images if available
            val imagesRecyclerView = itemView.findViewById<RecyclerView>(R.id.rvImages)
            capsule.contents?.filterIsInstance<CapsuleContentEntity>()?.let { contents ->
                val imageContents = contents.filter { it.isImage }
                if (imageContents.isNotEmpty()) {
                    imagesRecyclerView.visibility = View.VISIBLE
                    imagesRecyclerView.layoutManager = GridLayoutManager(itemView.context, 2)
                    imagesRecyclerView.adapter = CapsuleImagesAdapter(imageContents)
                } else {
                    imagesRecyclerView.visibility = View.GONE
                }
            } ?: run {
                imagesRecyclerView.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_published_capsule, parent, false)
        return CapsuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        holder.bind(capsules[position])
    }

    override fun getItemCount(): Int = capsules.size

    fun updateData(newCapsules: List<TimeCapsuleDTO>) {
        this.capsules = newCapsules
        notifyDataSetChanged()
    }
}

class CapsuleImagesAdapter(private val images: List<CapsuleContentEntity>) :
    RecyclerView.Adapter<CapsuleImagesAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(image: CapsuleContentEntity) {
            // Use Glide or similar library to load the image
            Glide.with(itemView.context)
                .load(image.filePath) // Adjust based on your actual image URL/path
                .placeholder(R.drawable.ic_placeholder)
                .into(itemView.findViewById<ImageView>(R.id.ivCapsuleImage))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_capsule_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}