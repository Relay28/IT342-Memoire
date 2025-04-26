package com.example.memoire.adapter

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.api.RetrofitClient
import com.example.memoire.databinding.ItemCapsuleAccessBinding
import com.example.memoire.models.CapsuleAccessDTO
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.UserSearchDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class CapsuleAccessAdapter(
    private var accesses: MutableList<CapsuleAccessDTO>,
    private val onRoleChange: (Long, String) -> Unit,
    private val onRemoveAccess: (Long) -> Unit
) : RecyclerView.Adapter<CapsuleAccessAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCapsuleAccessBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCapsuleAccessBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val access = accesses[position]

        with(holder.binding) {
            // Set username temporarily
            textUsername.text = "Loading..."

            // Initialize Spinner
            val roles = arrayOf("EDITOR", "VIEWER")
            val adapter = ArrayAdapter(
                root.context,
                R.layout.simple_spinner_item,
                roles
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spinnerRole.adapter = adapter

            // Set current role
            val currentRoleIndex = roles.indexOf(access.role.uppercase())
            if (currentRoleIndex >= 0) {
                spinnerRole.setSelection(currentRoleIndex)
            }

            // Ensure proper dimensions
            root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Load user details
            loadUserDetails(access.userId) { user ->
                textUsername.text = user.username

                // Load profile picture with proper URL handling
                val profilePictureUrl = user.profilePicture?.let {
                    "${RetrofitClient.BASE_URL}uploads/$it"
                }

                Glide.with(root.context)
                    .load("${RetrofitClient.BASE_URL}uploads/${user.profilePicture}")
                    .placeholder((com.example.memoire.R.drawable.default_profile)) // Your placeholder drawable
                    .error(R.drawable.picture_frame) // Fallback if error occurs
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageProfile)
            }

            spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val newRole = roles[position]
                    if (newRole != access.role) {
                        onRoleChange(access.id, newRole)
                        accesses[holder.adapterPosition] = access.copy(role = newRole)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            btnRemove.setOnClickListener {
                onRemoveAccess(access.id)
            }
        }
    }

    private fun loadUserDetails(userId: Long, callback: (ProfileDTO) -> Unit) {
        RetrofitClient.instance.getPublicProfile(userId).enqueue(object : Callback<ProfileDTO> {
            override fun onResponse(call: Call<ProfileDTO>, response: Response<ProfileDTO>) {
                if (response.isSuccessful) {
                    response.body()?.let { callback(it) }
                } else {
                    callback(createFallbackProfile(userId))
                }
            }

            override fun onFailure(call: Call<ProfileDTO>, t: Throwable) {
                callback(createFallbackProfile(userId))
            }
        })
    }

    private fun createFallbackProfile(userId: Long): ProfileDTO {
        return ProfileDTO(
            id = userId,
            username = "user_$userId",
            name = "User $userId",
            email = "user$userId@example.com",
            profilePicture = null
        )
    }

    override fun getItemCount() = accesses.size

    fun updateData(newAccesses: List<CapsuleAccessDTO>) {
        accesses.clear()
        accesses.addAll(newAccesses)
        notifyDataSetChanged()
    }
}