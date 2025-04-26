package com.example.memoire.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.databinding.ItemCapsuleAccessBinding
import com.example.memoire.models.CapsuleAccessDTO

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

        // Here you would load user details (username, profile picture, etc.)
        // You might want to fetch user details based on access.userId
        holder.binding.textUsername.text = "User ID: ${access.userId}"
        holder.binding.textRole.text = "Role: ${access.role}"

        // Setup role change spinner
        val roles = arrayOf("EDITOR", "VIEWER")
        val currentRoleIndex = roles.indexOf(access.role)
        holder.binding.spinnerRole.setSelection(currentRoleIndex)

        holder.binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newRole = roles[position]
                if (newRole != access.role) {
                    onRoleChange(access.id, newRole)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        holder.binding.buttonRemove.setOnClickListener {
            onRemoveAccess(access.id)
        }
    }

    override fun getItemCount() = accesses.size

    fun updateData(newAccesses: List<CapsuleAccessDTO>) {
        accesses.clear()
        accesses.addAll(newAccesses)
        notifyDataSetChanged()
    }
}