package com.example.memoire

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memoire.adapter.CapsuleAccessAdapter
import com.example.memoire.adapter.UserSearchAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.databinding.DialogGrantAccessBinding
import com.example.memoire.models.CapsuleAccessDTO
import com.example.memoire.models.GrantAccessRequest
import com.example.memoire.models.UpdateRoleRequest
import com.example.memoire.models.UserSearchDTO
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
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
) : Dialog(context, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog) {

    private lateinit var binding: DialogGrantAccessBinding
    private lateinit var searchAdapter: UserSearchAdapter
    private lateinit var accessAdapter: CapsuleAccessAdapter
    private var searchJob: Job? = null
    init {
        setContentView(R.layout.dialog_grant_access)
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        setupViews()
    }

    private fun setupViews() {
        binding = DialogGrantAccessBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Setup tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Current Access"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Add Users"))

        // Setup adapters
        searchAdapter = UserSearchAdapter(emptyList()) { user ->
            showRoleSelectionDialog(user)
        }
        accessAdapter = CapsuleAccessAdapter(
            mutableListOf(),
            onRoleChange = { accessId, newRole -> updateAccessRole(accessId, newRole) },
            onRemoveAccess = { accessId -> removeAccess(accessId) }
        )

        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }

        binding.accessList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = accessAdapter
        }

        // Tab selection listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showCurrentAccess()
                    1 -> showSearchView()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Setup search
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                text?.toString()?.takeIf { it.isNotEmpty() }?.let { query ->
                    searchUsers(query)
                } ?: run {
                    searchAdapter.updateData(emptyList())
                    binding.tvNoResults.visibility = View.GONE
                }
            }
        }

        binding.buttonCancel.setOnClickListener { dismiss() }

        // Load current access by default
        showCurrentAccess()
    }

    private fun showCurrentAccess() {
        binding.accessList.visibility = View.VISIBLE
        binding.searchView.visibility = View.GONE

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

    private fun showSearchView() {
        binding.accessList.visibility = View.GONE
        binding.searchView.visibility = View.VISIBLE
        binding.editTextSearch.requestFocus()
    }

    private fun searchUsers(query: String) {
        binding.tvNoResults.visibility = View.GONE

        RetrofitClient.instance.searchProfiles(query).enqueue(
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
                                    profilePicture = result["profilePicture"] as? String
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

    private fun showRoleSelectionDialog(user: UserSearchDTO) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Grant access to ${user.username}")
            .setItems(arrayOf("Editor - Can edit content", "Viewer - Read only")) { _, which ->
                grantAccess(user.id, if (which == 0) "EDITOR" else "VIEWER")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun grantAccess(userId: Long, role: String) {
        val request = GrantAccessRequest(capsuleId, userId, role)

        RetrofitClient.instance.grantAccess(request).enqueue(
            object : Callback<CapsuleAccessDTO> {
                override fun onResponse(call: Call<CapsuleAccessDTO>, response: Response<CapsuleAccessDTO>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Access granted", Toast.LENGTH_SHORT).show()
                        onAccessGranted()
                        showCurrentAccess() // Refresh the list
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
                        showCurrentAccess() // Refresh
                    }
                }
                override fun onFailure(call: Call<CapsuleAccessDTO>, t: Throwable) {
                    Toast.makeText(context, "Failed to update role", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun removeAccess(accessId: Long) {
        AlertDialog.Builder(context)
            .setTitle("Remove Access")
            .setMessage("Are you sure you want to remove this access?")
            .setPositiveButton("Remove") { _, _ ->
                RetrofitClient.instance.removeAccess(accessId).enqueue(
                    object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Access removed", Toast.LENGTH_SHORT).show()
                                showCurrentAccess() // Refresh
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