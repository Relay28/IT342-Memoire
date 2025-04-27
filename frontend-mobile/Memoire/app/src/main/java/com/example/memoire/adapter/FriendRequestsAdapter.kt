package com.example.memoire.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.api.FriendshipEntity
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.ProfileDTO2
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class FriendRequestsAdapter(
    private val onAccept: (FriendshipEntity) -> Unit,
    private val onDecline: (FriendshipEntity) -> Unit
) : RecyclerView.Adapter<FriendRequestsAdapter.RequestViewHolder>() {

    private var requests: List<FriendshipEntity> = emptyList()

    fun updateItems(newItems: List<FriendshipEntity>) {
        requests = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend2, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request, onAccept, onDecline)
    }

    override fun getItemCount(): Int = requests.size

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfilePic: ImageView = itemView.findViewById(R.id.ivProfilePic)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val btnPrimary: MaterialButton = itemView.findViewById(R.id.btnPrimary)
        private val requestButtonsContainer: LinearLayout = itemView.findViewById(R.id.requestButtonsContainer)
        private val btnAccept: MaterialButton = itemView.findViewById(R.id.btnAccept)
        private val btnDecline: MaterialButton = itemView.findViewById(R.id.btnDecline)

        fun bind(
            request: FriendshipEntity,
            onAccept: (FriendshipEntity) -> Unit,
            onDecline: (FriendshipEntity) -> Unit
        ) {
            // Show default values while loading
            tvUsername.text = "Loading..."
            tvName.text = ""
            ivProfilePic.setImageResource(R.drawable.ic_placeholder)
            Log.d("WHATS IN THE REQUEST" , request.toString())
            // Get the requester's ID from the friendship
            val requesterId = request.id ?: request.id?: return

            // Fetch the public profile
            RetrofitClient.instance.getPublicProfile2(requesterId).enqueue(object : Callback<ProfileDTO2> {
                override fun onResponse(call: Call<ProfileDTO2>, response: Response<ProfileDTO2>) {
                    if (response.isSuccessful) {
                        val profile = response.body()
                        profile?.let {
                            Log.d("TESTSDS" , it.toString())
                            tvUsername.text = it.username
                            tvName.text = it.name ?: "No name"

                            // Set profile picture if available
                            it.profilePicture?.let { picture ->
                                try {
                                    val imageBytes = Base64.decode(picture, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    ivProfilePic.setImageBitmap(bitmap)
                                } catch (e: Exception) {
                                    ivProfilePic.setImageResource(R.drawable.ic_placeholder)
                                }
                            } ?: ivProfilePic.setImageResource(R.drawable.ic_placeholder)
                        }
                    } else {
                        // Handle error case
                        tvUsername.text = "User"
                        tvName.text = "Unknown"
                        ivProfilePic.setImageResource(R.drawable.ic_placeholder)
                    }
                }

                override fun onFailure(call: Call<ProfileDTO2>, t: Throwable) {
                    // Handle failure case
                    tvUsername.text = "User"
                    tvName.text = "Unknown"
                    ivProfilePic.setImageResource(R.drawable.ic_placeholder)
                }
            })

            // Configure buttons for friend request
            btnPrimary.visibility = View.GONE
            requestButtonsContainer.visibility = View.VISIBLE

            btnAccept.setOnClickListener { onAccept(request) }
            btnDecline.setOnClickListener { onDecline(request) }
        }
    }
}