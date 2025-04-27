package com.example.memoire.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.CapsuleContentEntity
import com.example.memoire.models.TimeCapsuleDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
class PublishedCapsulesAdapter(
    private var capsules: List<TimeCapsuleDTO>,
    private val onItemClick: (TimeCapsuleDTO) -> Unit
) : RecyclerView.Adapter<PublishedCapsulesAdapter.CapsuleViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    inner class CapsuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        private val indicatorsLayout: LinearLayout = itemView.findViewById(R.id.layoutIndicators)

        fun bind(capsule: TimeCapsuleDTO) {
            // Bind basic info
            itemView.findViewById<TextView>(R.id.tvCapsuleTitle).text = capsule.title ?: "Untitled Memory"
            itemView.findViewById<TextView>(R.id.tvCapsuleDescription).text = capsule.description ?: "No description"

            // Format dates
            val createdAt = capsule.createdAt?.let { dateFormat.format(it) } ?: "Unknown date"
            val openDate = capsule.openDate?.let { dateFormat.format(it) } ?: "Not specified"

            itemView.findViewById<TextView>(R.id.tvCreatedDate).text = "Created on: $createdAt"
            itemView.findViewById<TextView>(R.id.tvOpenedDate).text = "Opened on: $openDate"

            // Setup carousel if there are contents
            capsule.contents?.let { contents ->
                if (contents.isNotEmpty()) {
                    setupCarousel(contents)
                } else {
                    viewPager.visibility = View.GONE
                    indicatorsLayout.visibility = View.GONE
                }
            } ?: run {
                viewPager.visibility = View.GONE
                indicatorsLayout.visibility = View.GONE
            }
        }

        private fun setupCarousel(contents: List<CapsuleContentEntity>) {
            viewPager.visibility = View.VISIBLE
            indicatorsLayout.visibility = View.VISIBLE

            val adapter = CarouselAdapter(contents)
            viewPager.adapter = adapter

            // Setup indicators
            setupIndicators(contents.size)

            // Update indicators when page changes
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateIndicators(position)
                }
            })
        }

        private fun setupIndicators(count: Int) {
            indicatorsLayout.removeAllViews()
            for (i in 0 until count) {
                val indicator = ImageView(itemView.context).apply {
                    setImageResource(R.drawable.indicator_unselected)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(8, 0, 8, 0)
                    }
                }
                indicatorsLayout.addView(indicator)
            }
            updateIndicators(0) // Select first item by default
        }

        private fun updateIndicators(position: Int) {
            for (i in 0 until indicatorsLayout.childCount) {
                val indicator = indicatorsLayout.getChildAt(i) as ImageView
                indicator.setImageResource(
                    if (i == position) R.drawable.indicator_selected
                    else R.drawable.indicator_unselected
                )
            }
        }
    }

    inner class CarouselAdapter(private val contents: List<CapsuleContentEntity>) :
        RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

        inner class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.ivCarouselImage)
            val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_carousel_image, parent, false)
            return CarouselViewHolder(view)
        }

        override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
            val content = contents[position]
            holder.progressBar.visibility = View.VISIBLE

            if (content.contentType?.startsWith("image/") == true) {
                // Load image using downloadContent endpoint
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.capsuleContentInstance.downloadContent(content.id!!)

                        if (response.isSuccessful) {
                            val bitmap = BitmapFactory.decodeStream(response.body()?.byteStream())
                            withContext(Dispatchers.Main) {
                                holder.imageView.setImageBitmap(bitmap)
                                holder.progressBar.visibility = View.GONE
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                holder.imageView.setImageResource(R.drawable.ic_image)
                                holder.progressBar.visibility = View.GONE
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            holder.imageView.setImageResource(R.drawable.ic_image)
                            holder.progressBar.visibility = View.GONE
                        }
                    }
                }
            } else {
                // Show appropriate icon for non-image content
                when {
                    content.contentType?.startsWith("video/") == true -> {
                        holder.imageView.setImageResource(R.drawable.ic_video)
                    }
                    content.contentType?.startsWith("audio/") == true -> {
                        holder.imageView.setImageResource(R.drawable.ic_audio)
                    }
                    else -> {
                        holder.imageView.setImageResource(R.drawable.ic_file)
                    }
                }
                holder.progressBar.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = contents.size
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