package com.example.memoire.activities

import CapsuleGridAdapter
import PublicCapsuleGridAdapter
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.PublicSinglePublishedCapsuleActivity
import com.example.memoire.R
import com.example.memoire.SinglePublishedCapsuleActivity
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
    private var isPendingRequest: Boolean = false
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



        // Get user ID from intent
        userId = intent.getLongExtra("userId", 0)
        if (userId == 0L) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        isPendingRequest = intent.getBooleanExtra("isPendingRequest", false)
        if (isPendingRequest) {
            btnFriendAction.text = "Accept Request"
            btnFriendAction.isEnabled = true
        }

        // Modify the handleFriendAction() to use the existing friendshipId for pending requests
        btnFriendAction.setOnClickListener {
            handleFriendAction()
        }

        // Set up back button
        imgBack.setOnClickListener {
            finish()
        }
        loadUserPublicCapsules()
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
                        Toast.makeText(this@UserProfileActivity, "What"+userId, Toast.LENGTH_SHORT).show()
                        updateUI(profile)
                        fetchUserFriendsCount(userId)
                        fetchPublishedCapsuleCount(userId)
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

    private fun fetchPublishedCapsuleCount(userId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getPublicPublishedTimeCapsuleCountForUser(userId)
                if (response.isSuccessful) {
                    val publishedCount = response.body() ?: 0
                    tvOwnedCount.text = publishedCount.toString()
                } else {
                    tvOwnedCount.text = "0"
                    Log.e("USER PROFILE ACTIVITY", "Failed to fetch published capsule count: ${response.code()}")
                }
            } catch (e: Exception) {
                tvOwnedCount.text = "0"
                Log.e("USER PROFILE ACTIVITY", "Error fetching published capsule count", e)
            }
        }
    }
    private fun fetchUserFriendsCount(userId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.friendInstance.getUserFriendsCount(userId)
                if (response.isSuccessful) {
                    val friendCount = response.body() ?: 0
                    tvFriendsCount.text = friendCount.toString()
                } else {
                    tvFriendsCount.text = "0"
                    Log.e("USER PROFILE ACTIVITY", "Failed to fetch friends count: ${response.code()}")
                }
            } catch (e: Exception) {
                tvFriendsCount.text = "0"
                Log.e("USER PROFILE ACTIVITY", "Error fetching friends count", e)
            }
        }
    }

    private fun loadUserPublicCapsules() {
        showLoading(true)
        RetrofitClient.instance.getUserPublicPublishedTimeCapsules(userId).enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                if (response.isSuccessful) {
                    val capsules = response.body()?.filter { it.createdById == userId } ?: emptyList()
                    setupCapsuleGrid()
                } else {
                    showLoading(false)
                    Toast.makeText(this@UserProfileActivity, "Failed to load public capsules", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@UserProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPublicCapsulesWithContents(onComplete: (List<TimeCapsuleDTO>) -> Unit) {
        showLoading(true)
        RetrofitClient.instance.getPublicPublishedTimeCapsules().enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                if (response.isSuccessful) {
                    val capsules = response.body()?.filter { it.createdById == userId } ?: emptyList()
                    fetchContentsForCapsules(capsules, onComplete)
                } else {
                    showLoading(false)
                    Toast.makeText(this@UserProfileActivity, "Failed to load public capsules", Toast.LENGTH_SHORT).show()
                    onComplete(emptyList())
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@UserProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                onComplete(emptyList())
            }
        })
    }

    private fun fetchContentsForCapsules(
        capsules: List<TimeCapsuleDTO>,
        onComplete: (List<TimeCapsuleDTO>) -> Unit
    ) {
        var completedRequests = 0
        val totalCapsules = capsules.size
        val capsulesWithContent = mutableListOf<TimeCapsuleDTO>()

        if (capsules.isEmpty()) {
            showLoading(false)
            onComplete(emptyList())
            return
        }

        for (capsule in capsules) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    capsule.id?.let { id ->
                        val response = RetrofitClient.capsuleContentInstance.getPublicCapsuleContents(id)
                        withContext(Dispatchers.Main) {
                            completedRequests++
                            if (response.isSuccessful) {
                                val contents = response.body() ?: emptyList()
                                val capsuleWithContent = capsule.copy(contents = contents)
                                capsulesWithContent.add(capsuleWithContent)
                            }

                            if (completedRequests == totalCapsules) {
                                showLoading(false)
                                onComplete(capsulesWithContent)
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        completedRequests++
                        if (completedRequests == totalCapsules) {
                            showLoading(false)
                            onComplete(capsulesWithContent)
                        }
                    }
                }
            }
        }
    }

    private fun setupCapsuleGrid() {
        val gridView: GridView = findViewById(R.id.gv_capsules)
        fetchPublicCapsulesWithContents { capsulesWithContent ->
            val capsuleAdapter = PublicCapsuleGridAdapter(this, capsulesWithContent) { capsule ->
                val intent = Intent(this@UserProfileActivity, PublicSinglePublishedCapsuleActivity::class.java).apply {
                    putExtra("capsuleId", capsule.id)
                }
                startActivity(intent)
            }
            gridView.adapter = capsuleAdapter
            gridView.post {
                gridView.requestLayout()
            }
        }
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
      if(profile.profilePicture!=null) {
          // Load profile picture
          val profileImageBytes = Base64.decode(profile.profilePicture, Base64.DEFAULT)
          // Create a bitmap from the byte array
          val bitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.size)
          // Load the bitmap with Glide
          if (bitmap != null) {
              Glide.with(this)
                  .load(bitmap)  // Load the bitmap directly
                  .circleCrop()
                  .placeholder(R.drawable.ic_placeholder)
                  .into(imgProfilePicture)
          } else {
              imgProfilePicture.setImageResource(R.drawable.ic_placeholder)
          }
      }else{
          imgProfilePicture.setImageResource(R.drawable.ic_placeholder)
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
        when {
            isPendingRequest -> acceptFriendRequest(friendshipId)
            btnFriendAction.text.toString() == "Add Friend" -> sendFriendRequest()
            btnFriendAction.text.toString() == "Remove Friend" -> removeFriend()
            btnFriendAction.text.toString() == "Cancel Request" -> cancelFriendRequest()
        }
    }

    private fun acceptFriendRequest(friendshipId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.friendInstance.acceptFriendship(friendshipId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserProfileActivity, "Friend request accepted", Toast.LENGTH_SHORT).show()
                        btnFriendAction.text = "Remove Friend"
                        isFriend = true
                        isPendingRequest = false
                        this@UserProfileActivity.friendshipId = response.body()?.id ?: 0
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