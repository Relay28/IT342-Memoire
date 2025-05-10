package com.example.memoire.adapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.CapsuleDetailActivity
import com.example.memoire.GrantAccessDialog
import com.example.memoire.R
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.CapsuleAccessDTO
import com.example.memoire.models.GrantAccessRequest
import com.example.memoire.models.LockRequest
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.models.UserEntity
import com.example.memoire.utils.DateUtils
import com.example.memoire.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class TimeCapsuleAdapter(private val context: Context, private var capsules: MutableList<TimeCapsuleDTO>) :
    RecyclerView.Adapter<TimeCapsuleAdapter.CapsuleViewHolder>() {

    // Track if we're currently loading friend data to prevent multiple API calls
    private var loadingFriends = false
    private var friendsList: List<UserEntity> = emptyList()

    sealed class AccessType {
        object OnlyMe : AccessType()
        object Public : AccessType()
        object Friends : AccessType()
        data class SpecificFriends(val friendIds: List<Long>) : AccessType()
    }

    inner class CapsuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val createdDate: TextView = view.findViewById(R.id.tvCreatedDate)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val shareButton: ImageButton = view.findViewById(R.id.btnShare)
        val lockButton: ImageButton = view.findViewById(R.id.btnLock)
        val deleteButton: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_capsule, parent, false)
        return CapsuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        val capsule = capsules[position]

        holder.title.text = capsule.title
        holder.description.text = capsule.description

        // Format dates using DateUtils
        holder.createdDate.text = "Created: ${capsule.createdAt?.let { DateUtils.formatDateForDisplay(it) } ?: "N/A"}"

        // Set status and icon
        holder.status.text = capsule.status
        when (capsule.status?.uppercase(Locale.getDefault())) {
            "UNPUBLISHED" -> {
                holder.status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unpublishedv2, 0, 0, 0)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
            }
            "CLOSED" -> {
                holder.status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_locked, 0, 0, 0)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
            }
            "PUBLISHED" -> {
                holder.status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_published, 0, 0, 0)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))
            }
            "ARCHIVED" -> {
                holder.status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_locked, 0, 0, 0)
                holder.status.setTextColor(context.getColor(R.color.MemoireRed))

                // Add long-press listener for archived capsules
                holder.cardView.setOnLongClickListener {
                    showUnarchivePopupMenu(holder.cardView, capsule, position)
                    true
                }
            }
        }

        // Set click listeners
        holder.cardView.setOnClickListener {
            openCapsuleDetail(capsule.id)
        }

        holder.shareButton.setOnClickListener {
            val context = holder.itemView.context
            GrantAccessDialog(context, capsule.id!!) {
                // Optional: refresh the list if needed
            }.show()
        }

        holder.lockButton.setOnClickListener {
            showLockDialog(capsule.id!!)
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(capsule, position)
        }
    }

    private fun showUnarchivePopupMenu(view: View, capsule: TimeCapsuleDTO, position: Int) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menu.add("Unarchive Capsule")
        popupMenu.setOnMenuItemClickListener { menuItem ->
            if (menuItem.title == "Unarchive Capsule") {
                toggleArchiveStatus(capsule, position)
                true
            } else {
                false
            }
        }
        popupMenu.show()
    }

    private fun toggleArchiveStatus(capsule: TimeCapsuleDTO, position: Int) {
        val sessionManager = SessionManager(context)
        val currentUserId = sessionManager.getUserSession()["userId"] as? Long

        if (capsule.createdById != currentUserId) {
            Toast.makeText(context, "You are not authorized to unarchive this capsule", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.archiveTimeCapsule(capsule.id!!).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Refresh the list after successful status update
                    fetchUpdatedCapsules2()
                    Toast.makeText(context, "Capsule status updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update capsule status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun getItemCount() = capsules.size

    fun updateData(newCapsules: MutableList<TimeCapsuleDTO>) {
        capsules = newCapsules
        notifyDataSetChanged()
    }

    private fun openCapsuleDetail(capsuleId: Long?) {
        capsuleId?.let {
            val intent = Intent(context, CapsuleDetailActivity::class.java)
            intent.putExtra("capsuleId", it.toString())
            context.startActivity(intent)
        }
    }

    // Integrated lock dialog functionality directly into adapter
    private fun showLockDialog(capsuleId: Long) {
        // If we don't have friends list yet, start loading it
        if (friendsList.isEmpty() && !loadingFriends) {
            loadingFriends = true
            loadFriends()
        }

        // Create a bottom sheet dialog for better UX
        val dialog = BottomSheetDialog(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_lock_capsule, null)
        dialog.setContentView(dialogView)

        // Initialize date/time elements
        val btnSelectDate = dialogView.findViewById<TextView>(R.id.btnSelectDate)
        val btnSelectTime = dialogView.findViewById<TextView>(R.id.btnSelectTime)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        val radioPrivate = dialogView.findViewById<View>(R.id.radioPrivate)
        val radioFriends = dialogView.findViewById<View>(R.id.radioFriends)
        val radioSpecific = dialogView.findViewById<View>(R.id.radioSpecific)
        // Modify this section in TimeCapsuleAdapter's showLockDialog
        val accessRadioGroup = dialogView.findViewById<RadioGroup>(R.id.accessRadioGroup)

        // Initialize with current time + 1 hour as default
        val selectedCalendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }

        // Update the date/time display
        fun updateDateTimeDisplay() {
            val dateText = android.text.format.DateFormat.format("MMM dd, yyyy", selectedCalendar.time).toString()
            btnSelectDate.text = dateText

            val timeText = android.text.format.DateFormat.format("hh:mm a", selectedCalendar.time).toString()
            btnSelectTime.text = timeText

            // Enable confirm button only if date is in future
            val now = Calendar.getInstance()
            btnConfirm.isEnabled = selectedCalendar.timeInMillis > now.timeInMillis
        }

        // Initialize the date/time display
        updateDateTimeDisplay()

        // Set up date picker
        btnSelectDate.setOnClickListener {
            val datePicker = DatePickerDialog(
                context,
                { _, year, month, day ->
                    selectedCalendar.set(year, month, day)
                    updateDateTimeDisplay()
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            )
            // Set minimum date to tomorrow (can't lock for past dates)
            val minCalendar = Calendar.getInstance()
            datePicker.datePicker.minDate = minCalendar.timeInMillis
            datePicker.show()
        }

        // Set up time picker
        btnSelectTime.setOnClickListener {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    selectedCalendar.set(Calendar.MINUTE, minute)
                    updateDateTimeDisplay()
                },
                selectedCalendar.get(Calendar.HOUR_OF_DAY),
                selectedCalendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        // Handle confirm button
        btnConfirm.setOnClickListener {
            val accessType = when (accessRadioGroup.checkedRadioButtonId) { // Use checkedRadioButtonId\
                //                R.id.radioPrivate -> AccessType.OnlyMe
                R.id.radioFriends -> AccessType.Friends
                R.id.radioPublic -> AccessType.Public
                R.id.radioSpecific -> {
                    showFriendSelectionUI(capsuleId, selectedCalendar.time)
                    dialog.dismiss()
                    return@setOnClickListener
                }
                else -> AccessType.OnlyMe
            }

            lockCapsuleWithAccess(capsuleId, selectedCalendar.time, accessType)
            dialog.dismiss()
        }

        // Handle cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Handle specific friends radio button
        radioSpecific.setOnClickListener {
            if (friendsList.isEmpty()) {
                Toast.makeText(context, "Loading friends...", Toast.LENGTH_SHORT).show()
                loadFriends()
            }
        }

        dialog.show()
    }

    private fun loadFriends() {
        RetrofitClient.instance.getFriendsList().enqueue(object : Callback<List<UserEntity>> {
            override fun onResponse(call: Call<List<UserEntity>>, response: Response<List<UserEntity>>) {
                loadingFriends = false
                if (response.isSuccessful) {
                    friendsList = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Failed to load friends", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<UserEntity>>, t: Throwable) {
                loadingFriends = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showFriendSelectionUI(capsuleId: Long, unlockDate: Date) {
        if (friendsList.isEmpty()) {
            Toast.makeText(context, "No friends available", Toast.LENGTH_SHORT).show()
            return
        }

        // This is where you'd show your FriendSelectionDialogFragment
        // For simplicity, I'm using a basic dialog instead
        val friendNames = friendsList.map { it.username }.toTypedArray()
        val checkedItems = BooleanArray(friendNames.size) { false }

        AlertDialog.Builder(context)
            .setTitle("Select Friends")
            .setMultiChoiceItems(friendNames, checkedItems) { _, _, _ -> }
            .setPositiveButton("Confirm") { dialog, _ ->
                val selectedFriendIds = mutableListOf<Long>()
                for (i in checkedItems.indices) {
                    if (checkedItems[i]) {
                        selectedFriendIds.add(friendsList[i].id)
                    }
                }
                lockCapsuleWithAccess(capsuleId, unlockDate, AccessType.SpecificFriends(selectedFriendIds))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun lockCapsuleWithAccess(capsuleId: Long, unlockDate: Date, accessType: AccessType) {
        val formattedDate = DateUtils.formatForApi(unlockDate)
        val lockRequest = LockRequest(openDate = formattedDate)

        Log.d("TimeCapsule", "Local time selected: ${DateUtils.formatDateForDisplay(unlockDate)} ${DateUtils.formatTimeForDisplay(unlockDate)}")
        Log.d("TimeCapsule", "Formatted for API as: $formattedDate")

        RetrofitClient.instance.lockTimeCapsule(capsuleId, lockRequest).enqueue(
            object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        handleAccessAfterLock(capsuleId, accessType)
                        Toast.makeText(context, "Capsule locked successfully", Toast.LENGTH_SHORT).show()
                        fetchUpdatedCapsules()
                    } else {
                        Toast.makeText(context, "Failed to lock capsule: ${response.code()}", Toast.LENGTH_SHORT).show()
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e("TimeCapsule", "Error body: $errorBody")
                        } catch (e: Exception) {
                            Log.e("TimeCapsule", "Could not read error body", e)
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("TimeCapsule", "Network error", t)
                }
            }
        )
    }
    private fun handleAccessAfterLock(capsuleId: Long,accessType: AccessType) {
        Log.d("TimeCapsule", "Handling access type: $accessType")
        when (accessType) {
            AccessType.OnlyMe -> {
                Log.d("TimeCapsule", "Calling restrictAccessToOwner API")
                restrictAccessToOwner(capsuleId)
            }
            AccessType.Public -> {
                Log.d("TimeCapsule", "Calling grantPublicAccess API")
                grantPublicAccess(capsuleId)
            }
           AccessType.Friends -> {
                Log.d("TimeCapsule", "Calling grantAccessToAllFriends API")
                grantAccessToAllFriends(capsuleId)
            }
            else -> {
                Log.d("TimeCapsule", "No specific access type to handle")
            }
        }
    }

    private fun restrictAccessToOwner(capsuleId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.restrictAccessToOwner(capsuleId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Access restricted to only you", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to restrict access: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun grantPublicAccess(capsuleId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.grantPublicAccess(capsuleId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Capsule is now public", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to make capsule public: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun grantAccessToAllFriends(capsuleId: Long) {
        RetrofitClient.instance.grantAccessToAllFriends(capsuleId, "VIEWER")
            .enqueue(object : Callback<List<CapsuleAccessDTO>> {
                override fun onResponse(
                    call: Call<List<CapsuleAccessDTO>>,
                    response: Response<List<CapsuleAccessDTO>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Access granted to all friends",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to grant access to all friends",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<CapsuleAccessDTO>>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun grantAccessToSpecificFriends(capsuleId: Long, friendIds: List<Long>) {
        friendIds.forEach { friendId ->
            val request = GrantAccessRequest(capsuleId, friendId, "VIEWER")
            RetrofitClient.instance.grantAccessToSpecificFriends(request)
                .enqueue(object : Callback<CapsuleAccessDTO> {
                    override fun onResponse(
                        call: Call<CapsuleAccessDTO>,
                        response: Response<CapsuleAccessDTO>
                    ) {
                        if (!response.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Failed to grant access to some friends",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<CapsuleAccessDTO>, t: Throwable) {
                        Toast.makeText(
                            context,
                            "Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun fetchUpdatedCapsules() {
        // Call the API to get the updated list of capsules
        RetrofitClient.instance.getUnpublishedTimeCapsules().enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                if (response.isSuccessful) {
                    val newCapsules = response.body() as? MutableList<TimeCapsuleDTO> ?: mutableListOf()
                    updateData(newCapsules)
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                Toast.makeText(context, "Failed to refresh capsules: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUpdatedCapsules2() {
        // Call the API to get the updated list of capsules
        RetrofitClient.instance.getArchivedTimeCapsules().enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(call: Call<List<TimeCapsuleDTO>>, response: Response<List<TimeCapsuleDTO>>) {
                if (response.isSuccessful) {
                    val newCapsules = response.body() as? MutableList<TimeCapsuleDTO> ?: mutableListOf()
                    updateData(newCapsules)
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                Toast.makeText(context, "Failed to refresh capsules: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmationDialog(capsule: TimeCapsuleDTO, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Time Capsule")
            .setMessage("Are you sure you want to delete '${capsule.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteTimeCapsule(capsule.id!!, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTimeCapsule(capsuleId: Long, position: Int) {
        RetrofitClient.instance.deleteTimeCapsule(capsuleId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    capsules.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "Time capsule deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete time capsule", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}