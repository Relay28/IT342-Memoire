package com.example.memoire.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.R
import com.example.memoire.adapters.CapsuleGridAdapter
import com.example.memoire.api.FriendshipRequest
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.progressindicator.CircularProgressIndicator
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileActivity : AppCompatActivity() {

    private lateinit var imgBack: ImageButton
    private lateinit var imgProfilePicture: CircleImageView
    private lateinit var txtUsername: TextView
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtBio: TextView
    private lateinit var btnFriendAction: Button
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvUsername: TextView
    private lateinit var tvOwnedCount: TextView
    private lateinit var tvFriendsCount: TextView
    private lateinit var tvSharedCount: TextView
    private lateinit var recyclerView: RecyclerView

    private var userId: Long = 0
    private var isFriend: Boolean = false
    private var hasPendingRequest: Boolean = false
    private var isReceiver: Boolean = false
    private var friendshipId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Initialize views
        imgBack = findViewById(R.id.imgBack)
        imgProfilePicture = findViewById(R.id.imgProfilePicture)
        txtUsername = findViewById(R.id.txtUsername)
        txtName = findViewById(R.id.txtName)
        txtEmail = findViewById(R.id.txtEmail)
        txtBio = findViewById(R.id.txtBio)
        btnFriendAction = findViewById(R.id.btnFriendAction)
        progressIndicator = findViewById(R.id.progress_indicator)
        tvUsername = findViewById(R.id.tv_username)
        tvOwnedCount = findViewById(R.id.tv_owned_count)
        tvFriendsCount = findViewById(R.id.tv_friends_count)
        tvSharedCount = findViewById(R.id.tv_shared_count)
        recyclerView = findViewById(R.id.rv_capsules)

        // Get user ID from intent
        userId = intent.getLongExtra("userId", 0)
        if (userId == 0L) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up back button
        imgBack.setOnClickListener {
            finish()
        }

        // Show loading indicator
        showLoading(true)

        // Load user profile data
        loadUserProfile()

        // Check friendship status
        checkFriendshipStatus()

        // Set up friend action button click listener
        btnFriendAction.setOnClickListener {
            handleFriendAction()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun loadUserProfile() {
        val call = RetrofitClient.instance.getPublicProfile(userId)
        call.enqueue(object : Callback<ProfileDTO> {
            override fun onResponse(call: Call<ProfileDTO>, response: Response<ProfileDTO>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        updateUI(profile)
                        loadUserCapsules()
                    } else {
                        showLoading(false)
                        Toast.makeText(this@UserProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(this@UserProfileActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileDTO>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@UserProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserCapsules() {
        // This is just a placeholder. You would implement actual capsule loading here.
        // For now, we'll just show an empty adapter
        recyclerView.adapter = CapsuleGridAdapter(emptyList()) { /* Capsule click handler */ }
        showLoading(false)
    }

    private fun updateUI(profile: ProfileDTO) {
        txtUsername.text = "@${profile.username}"
        tvUsername.text = "@${profile.username}"
        txtName.text = profile.name
        txtEmail.text = profile.email
        txtBio.text = profile.biography ?: "No bio available"

        // Update stats
        tvOwnedCount.text = "0" // This would be updated from actual data
        tvFriendsCount.text = "0" // This would be updated from actual data
        tvSharedCount.text = "0" // This would be updated from actual data

        // Load profile picture
        if (!profile.profilePicture.isNullOrEmpty()) {
            Glide.with(this)
                .load(profile.profilePicture)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(imgProfilePicture)
        } else {
            imgProfilePicture.setImageResource(R.drawable.default_profile)
        }
    }

    private fun checkFriendshipStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if users are friends
                val areFriendsResponse = RetrofitClient.friendInstance.areFriends(userId)
                isFriend = areFriendsResponse.body() ?: false

                // Check if there's a pending request
                val hasPendingResponse = RetrofitClient.friendInstance.hasPendingRequest(userId)
                hasPendingRequest = hasPendingResponse.body() ?: false

                // Check if current user is receiver of a friend request
                val isReceiverResponse = RetrofitClient.friendInstance.isReceiver(userId)
                isReceiver = isReceiverResponse.body() ?: false

                // Get friendship ID if exists
                if (isFriend || hasPendingRequest) {
                    val friendshipResponse = RetrofitClient.friendInstance.findByUsers(userId)
                    val friendship = friendshipResponse.body()
                    if (friendship != null) {
                        friendshipId = friendship.id
                    }
                }

                withContext(Dispatchers.Main) {
                    updateFriendButton()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserProfileActivity, "Error checking friendship: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnFriendAction.text = "Add Friend"
                }
            }
        }
    }

    private fun updateFriendButton() {
        when {
            isFriend -> {
                btnFriendAction.text = "Remove Friend"
            }
            hasPendingRequest && !isReceiver -> {
                btnFriendAction.text = "Cancel Request"
            }
            hasPendingRequest && isReceiver -> {
                btnFriendAction.text = "Accept Request"
            }
            else -> {
                btnFriendAction.text = "Add Friend"
            }
        }
    }

    private fun handleFriendAction() {
        btnFriendAction.isEnabled = false
        when (btnFriendAction.text.toString()) {
            "Add Friend" -> sendFriendRequest()
            "Remove Friend" -> removeFriend()
            "Cancel Request" -> cancelFriendRequest()
            "Accept Request" -> acceptFriendRequest()
        }
    }

    private fun sendFriendRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = FriendshipRequest(userId)
                val response = RetrofitClient.friendInstance.createFriendship(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserProfileActivity, "Friend request sent", Toast.LENGTH_SHORT).show()
                        btnFriendAction.text = "Cancel Request"
                        hasPendingRequest = true
                        isReceiver = false
                        friendshipId = response.body()?.id ?: 0
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Failed to send request", Toast.LENGTH_SHORT).show()
                    }
                    btnFriendAction.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnFriendAction.isEnabled = true
                }
            }
        }
    }

    private fun removeFriend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.friendInstance.deleteFriendship(friendshipId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserProfileActivity, "Friend removed", Toast.LENGTH_SHORT).show()
                        btnFriendAction.text = "Add Friend"
                        isFriend = false
                        hasPendingRequest = false
                        isReceiver = false
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Failed to remove friend", Toast.LENGTH_SHORT).show()
                    }
                    btnFriendAction.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnFriendAction.isEnabled = true
                }
            }
        }
    }

    private fun cancelFriendRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.friendInstance.cancelRequest(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserProfileActivity, "Friend request canceled", Toast.LENGTH_SHORT).show()
                        btnFriendAction.text = "Add Friend"
                        hasPendingRequest = false
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Failed to cancel request", Toast.LENGTH_SHORT).show()
                    }
                    btnFriendAction.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnFriendAction.isEnabled = true
                }
            }
        }
    }

    private fun acceptFriendRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.friendInstance.acceptFriendship(friendshipId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserProfileActivity, "Friend request accepted", Toast.LENGTH_SHORT).show()
                        btnFriendAction.text = "Remove Friend"
                        isFriend = true
                        hasPendingRequest = false
                        isReceiver = false
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Failed to accept request", Toast.LENGTH_SHORT).show()
                    }
                    btnFriendAction.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnFriendAction.isEnabled = true
                }
            }
        }
    }
}