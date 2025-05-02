package com.example.memoire

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memoire.adapter.CapsuleAccessAdapter
import com.example.memoire.adapter.UserSearchAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.databinding.DialogGrantAccessBinding
import com.example.memoire.models.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GrantAccessDialog(
    private val context: Context,
    private val capsuleId: Long,
    private val onAccessGranted: () -> Unit
) : BottomSheetDialog(context) {

    private lateinit var binding: DialogGrantAccessBinding
    private lateinit var searchAdapter: UserSearchAdapter
    private lateinit var accessAdapter: CapsuleAccessAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogGrantAccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure bottom sheet behavior for better usability
        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            peekHeight = context.resources.displayMetrics.heightPixels / 2
            isDraggable = true
        }

        setupViews()
        loadCurrentAccess()
    }

    private fun setupViews() {
        // Setup search adapter
        searchAdapter = UserSearchAdapter(emptyList()) { user ->
            grantAccess(user.id, "EDITOR") // Automatically grant editor access
        }

        // Setup access adapter with optimized parameters
        accessAdapter = CapsuleAccessAdapter(
            mutableListOf(),
            onRoleChange = { accessId, newRole -> updateAccessRole(accessId, newRole) },
            onRemoveAccess = { accessId -> removeAccess(accessId) }
        )

        // Configure recycler views with proper layout managers and decorations
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            isNestedScrollingEnabled = false
        }

        binding.accessList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = accessAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            isNestedScrollingEnabled = false
        }

        // Setup search with debounce
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.Main).launch {
                delay(300) // Debounce delay
                text?.toString()?.takeIf { it.isNotEmpty() }?.let { query ->
                    searchUsers(query)
                } ?: run {
                    searchAdapter.updateData(emptyList())
                    binding.tvNoResults.visibility = View.GONE
                }
            }
        }

        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun loadCurrentAccess() {
        RetrofitClient.instance.getCapsuleAccesses(capsuleId).enqueue(
            object : Callback<List<CapsuleAccessDTO>> {
                override fun onResponse(call: Call<List<CapsuleAccessDTO>>, response: Response<List<CapsuleAccessDTO>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { accesses ->
                            accessAdapter.updateData(accesses)
                        }
                    }
                }
                override fun onFailure(call: Call<List<CapsuleAccessDTO>>, t: Throwable) {
                    Toast.makeText(context, "Failed to load access list", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun searchUsers(query: String) {
        binding.tvNoResults.visibility = View.GONE

        RetrofitClient.instance.searchProfiles2(query).enqueue(
            object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        val results = response.body()?.get("results") as? List<Map<String, Any>> ?: emptyList()
                        if (results.isEmpty()) {
                            binding.tvNoResults.visibility = View.VISIBLE
                        } else {
                            val users = results.mapNotNull { result ->

                                UserSearchDTO(
                                    id = (result["userId"] as? Number)?.toLong() ?: 0L,
                                    username = result["username"] as? String ?: "",
                                    name = result["name"] as? String ?: "",
                                    email = result["email"] as? String ?: "",
                                    profilePicture = result["profilePictureData"] as? String
                                )

                            }
                            filterAlreadyGrantedUsers(users)
                        }
                    }
                }
                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(context, "Search failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun filterAlreadyGrantedUsers(users: List<UserSearchDTO>) {
        RetrofitClient.instance.getCapsuleAccesses(capsuleId).enqueue(
            object : Callback<List<CapsuleAccessDTO>> {
                override fun onResponse(call: Call<List<CapsuleAccessDTO>>, response: Response<List<CapsuleAccessDTO>>) {
                    val existingUserIds = response.body()?.map { it.userId } ?: emptyList()
                    val filteredUsers = users.filter { it.id !in existingUserIds }

                    if (filteredUsers.isEmpty()) {
                        binding.tvNoResults.visibility = View.VISIBLE
                        binding.tvNoResults.text = "All matching users already have access"
                    } else {
                        searchAdapter.updateData(filteredUsers)
                    }
                }
                override fun onFailure(call: Call<List<CapsuleAccessDTO>>, t: Throwable) {
                    searchAdapter.updateData(users) // Fallback
                }
            }
        )
    }

    private fun grantAccess(userId: Long, role: String) {
        val request = GrantAccessRequest(capsuleId, userId, role)

        RetrofitClient.instance.grantAccess(request).enqueue(
            object : Callback<CapsuleAccessDTO> {
                override fun onResponse(call: Call<CapsuleAccessDTO>, response: Response<CapsuleAccessDTO>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Editor access granted", Toast.LENGTH_SHORT).show()
                        onAccessGranted()
                        loadCurrentAccess() // Refresh the list
                        binding.editTextSearch.text?.clear()
                        searchAdapter.updateData(emptyList())
                    }
                }
                override fun onFailure(call: Call<CapsuleAccessDTO>, t: Throwable) {
                    Toast.makeText(context, "Failed to grant access", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun updateAccessRole(accessId: Long, newRole: String) {
        val request = UpdateRoleRequest(newRole)

        RetrofitClient.instance.updateAccessRole(accessId, request).enqueue(
            object : Callback<CapsuleAccessDTO> {
                override fun onResponse(call: Call<CapsuleAccessDTO>, response: Response<CapsuleAccessDTO>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Role updated", Toast.LENGTH_SHORT).show()
                        loadCurrentAccess() // Refresh
                    }
                }
                override fun onFailure(call: Call<CapsuleAccessDTO>, t: Throwable) {
                    Toast.makeText(context, "Failed to update role", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun removeAccess(accessId: Long) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Remove Access")
            .setMessage("Are you sure you want to remove this access?")
            .setPositiveButton("Remove") { _, _ ->
                RetrofitClient.instance.removeAccess(accessId).enqueue(
                    object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Access removed", Toast.LENGTH_SHORT).show()
                                loadCurrentAccess() // Refresh
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(context, "Failed to remove access", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun dismiss() {
        searchJob?.cancel()
        super.dismiss()
    }
}