package com.example.memoire

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memoire.adapter.UserSearchAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.databinding.DialogGrantAccessBinding
import com.example.memoire.models.CapsuleAccessDTO
import com.example.memoire.models.GrantAccessRequest
import com.example.memoire.models.UserSearchDTO
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
) : Dialog(context) {

    private lateinit var binding: DialogGrantAccessBinding
    private lateinit var adapter: UserSearchAdapter
    private var searchJob: Job? = null

    init {
        setContentView(R.layout.dialog_grant_access)
        setupViews()
    }

    private fun setupViews() {
        binding = DialogGrantAccessBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Setup RecyclerView
        adapter = UserSearchAdapter(emptyList()) { user ->
            showRoleSelectionDialog(user)
        }
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@GrantAccessDialog.adapter
        }

        // Setup search with debounce (300ms delay)
        binding.editTextSearch.doAfterTextChanged { editable ->
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                editable?.toString()?.takeIf { it.length >= 1 }?.let { query ->
                    searchUsers(query)
                } ?: run {
                    adapter.updateData(emptyList())
                }
            }
        }

        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun searchUsers(query: String) {
        RetrofitClient.instance.searchUsersForGrantAccess(query).enqueue(
            object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        parseSearchResults(response.body())
                    } else {
                        showError("Search failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    showError("Network error: ${t.message}")
                }
            }
        )
    }
    private fun parseSearchResults(body: Map<String, Any>?) {
        try {
            val results = body?.get("results") as? List<Map<String, Any>> ?: return
            val users = results.mapNotNull { result ->
                UserSearchDTO(
                    id = (result["userId"] as? Number)?.toLong() ?: return@mapNotNull null,
                    username = result["username"] as? String ?: "",
                    name = "", // Not used in your current DTO
                    email = result["email"] as? String ?: "",
                    profilePicture = result["profilePicture"] as? String
                )
            }
            filterAlreadyGrantedUsers(users)
        } catch (e: Exception) {
            showError("Failed to parse results")
        }
    }

    private fun filterAlreadyGrantedUsers(users: List<UserSearchDTO>) {
        RetrofitClient.instance.getCapsuleAccesses(capsuleId).enqueue(
            object : Callback<List<CapsuleAccessDTO>> {
                override fun onResponse(
                    call: Call<List<CapsuleAccessDTO>>,
                    response: Response<List<CapsuleAccessDTO>>
                ) {
                    val existingUserIds = response.body()?.map { it.userId } ?: emptyList()
                    val filteredUsers = users.filter { it.id !in existingUserIds }
                    adapter.updateData(filteredUsers)
                }

                override fun onFailure(call: Call<List<CapsuleAccessDTO>>, t: Throwable) {
                    // If we can't verify existing accesses, show all results
                    adapter.updateData(users)
                }
            }
        )
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    private fun showRoleSelectionDialog(user: UserSearchDTO) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Grant access to ${user.username}")
            .setItems(arrayOf("Editor", "Viewer")) { _, which ->
                val role = if (which == 0) "EDITOR" else "VIEWER"
                grantAccess(user.id, role)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun grantAccess(userId: Long, role: String) {
        val request = GrantAccessRequest(capsuleId, userId, role)

        RetrofitClient.instance.grantAccess(request).enqueue(object : Callback<CapsuleAccessDTO> {
            override fun onResponse(call: Call<CapsuleAccessDTO>, response: Response<CapsuleAccessDTO>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Access granted", Toast.LENGTH_SHORT).show()
                    onAccessGranted()
                    dismiss()
                } else {
                    Toast.makeText(context, "Failed to grant access", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CapsuleAccessDTO>, t: Throwable) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun dismiss() {
        searchJob?.cancel()
        super.dismiss()
    }
}