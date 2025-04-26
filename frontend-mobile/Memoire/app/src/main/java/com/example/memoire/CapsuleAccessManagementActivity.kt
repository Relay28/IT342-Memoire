package com.example.memoire

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memoire.adapter.CapsuleAccessAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.databinding.ActivityCapsuleAccessManagementBinding
import com.example.memoire.models.CapsuleAccessDTO
import com.example.memoire.models.UpdateRoleRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CapsuleAccessManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCapsuleAccessManagementBinding
    private lateinit var adapter: CapsuleAccessAdapter
    private var capsuleId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCapsuleAccessManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        capsuleId = intent.getLongExtra("capsuleId", -1L)
        if (capsuleId == -1L) {
            Toast.makeText(this, "Invalid capsule ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadAccessList()
        setupAddButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Access"
    }

    private fun setupRecyclerView() {
        adapter = CapsuleAccessAdapter(
            mutableListOf(),
            onRoleChange = { accessId, newRole ->
                updateAccessRole(accessId, newRole)
            },
            onRemoveAccess = { accessId ->
                removeAccess(accessId)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CapsuleAccessManagementActivity)
            adapter = this@CapsuleAccessManagementActivity.adapter
        }
    }

    private fun setupAddButton() {
        binding.fabAddAccess.setOnClickListener {
            GrantAccessDialog(this, capsuleId) {
                loadAccessList() // Refresh after granting access
            }.show()
        }
    }

    private fun loadAccessList() {
        RetrofitClient.instance.getCapsuleAccesses(capsuleId).enqueue(
            object : Callback<List<CapsuleAccessDTO>> {
                override fun onResponse(
                    call: Call<List<CapsuleAccessDTO>>,
                    response: Response<List<CapsuleAccessDTO>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { accesses ->
                            adapter.updateData(accesses.toMutableList())
                        }
                    } else {
                        Toast.makeText(
                            this@CapsuleAccessManagementActivity,
                            "Failed to load access list",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<CapsuleAccessDTO>>, t: Throwable) {
                    Toast.makeText(
                        this@CapsuleAccessManagementActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun updateAccessRole(accessId: Long, newRole: String) {
        val request = UpdateRoleRequest(newRole)

        RetrofitClient.instance.updateAccessRole(accessId, request).enqueue(
            object : Callback<CapsuleAccessDTO> {
                override fun onResponse(
                    call: Call<CapsuleAccessDTO>,
                    response: Response<CapsuleAccessDTO>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@CapsuleAccessManagementActivity,
                            "Access role updated",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadAccessList()
                    } else {
                        Toast.makeText(
                            this@CapsuleAccessManagementActivity,
                            "Failed to update role",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CapsuleAccessDTO>, t: Throwable) {
                    Toast.makeText(
                        this@CapsuleAccessManagementActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun removeAccess(accessId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Remove Access")
            .setMessage("Are you sure you want to remove this access?")
            .setPositiveButton("Remove") { _, _ ->
                performRemoveAccess(accessId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performRemoveAccess(accessId: Long) {
        RetrofitClient.instance.removeAccess(accessId).enqueue(
            object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@CapsuleAccessManagementActivity,
                            "Access removed",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadAccessList()
                    } else {
                        Toast.makeText(
                            this@CapsuleAccessManagementActivity,
                            "Failed to remove access",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@CapsuleAccessManagementActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}