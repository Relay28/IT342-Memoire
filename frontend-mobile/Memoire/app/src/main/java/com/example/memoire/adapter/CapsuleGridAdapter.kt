import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.CapsuleContentEntity
import com.example.memoire.models.TimeCapsuleDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CapsuleGridAdapter(
    private val context: Context,
    private var capsules: List<TimeCapsuleDTO>,
    private val onCapsuleClick: (TimeCapsuleDTO) -> Unit
) : BaseAdapter() {

    private val TAG = "CapsuleGridAdapter"

    override fun getCount(): Int {
        Log.d(TAG, "getCount called: ${capsules.size} capsules available")
        return capsules.size
    }

    override fun getItem(position: Int): Any {
        Log.d(TAG, "getItem called for position: $position")
        return capsules[position]
    }

    override fun getItemId(position: Int): Long {
        val id = capsules[position].id ?: 0L
        Log.d(TAG, "getItemId called for position: $position, id: $id")
        return id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d(TAG, "getView called for position: $position")
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_capsule_grid, parent, false)
        val capsule = capsules[position]
        val imgCapsule = view.findViewById<ImageView>(R.id.contentThumbnail1)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.VISIBLE

        if (!capsule.contents.isNullOrEmpty()) {
            Log.d(TAG, "Rendering first content for capsule ID: ${capsule.id}")
            val firstContent = capsule.contents.first()
            renderContent(firstContent, imgCapsule, progressBar)
        } else {
            Log.d(TAG, "No contents available for capsule ID: ${capsule.id}")
            progressBar.visibility = View.GONE
            imgCapsule.setImageResource(R.drawable.mmrlogo_removebg_preview)
        }

        view.setOnClickListener {
            Log.d(TAG, "Capsule clicked: ID = ${capsule.id}, Title = ${capsule.title}")
            onCapsuleClick(capsule)
        }
        return view
    }

    private fun renderContent(content: CapsuleContentEntity, imgCapsule: ImageView, progressBar: ProgressBar) {
        Log.d(TAG, "Rendering content: ID = ${content.id}, Type = ${content.contentType}")
        progressBar.visibility = View.VISIBLE

        if (content.id != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Use the downloadContent API to fetch the image as a Bitmap
                    val response = RetrofitClient.capsuleContentInstance.downloadContent(content.id)
                    if (response.isSuccessful) {

                        val bitmap = BitmapFactory.decodeStream(response.body()?.byteStream())

                        withContext(Dispatchers.Main) {
                            if (bitmap != null) {
                                imgCapsule.setImageBitmap(bitmap)
                            } else {
                                imgCapsule.setImageResource(R.drawable.mmrlogo_removebg_preview) // Fallback placeholder
                            }
                            progressBar.visibility = View.GONE
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, "Failed to download content: ${response.code()} - ${response.message()}")
                            imgCapsule.setImageResource(R.drawable.mmrlogo_removebg_preview) // Fallback placeholder
                            progressBar.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Error downloading content", e)
                        imgCapsule.setImageResource(R.drawable.mmrlogo_removebg_preview) // Fallback placeholder
                        progressBar.visibility = View.GONE
                    }
                }
            }
        } else {
            Log.d(TAG, "Content is not an image or ID is null, using placeholder")
            imgCapsule.setImageResource(R.drawable.mmrlogo_removebg_preview) // Fallback placeholder
            progressBar.visibility = View.GONE
        }
    }

    fun updateData(newCapsules: List<TimeCapsuleDTO>) {
        Log.d(TAG, "Updating adapter data with ${newCapsules.size} capsules")
        this.capsules = newCapsules
        notifyDataSetChanged()
    }
}