package com.example.memoire

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.memoire.api.RetrofitClient
import com.example.memoire.databinding.DialogLockCapsuleBinding
import com.example.memoire.models.CapsuleAccessDTO
import com.example.memoire.models.GrantAccessRequest
import com.example.memoire.models.LockRequest
import com.example.memoire.models.UserEntity
import com.example.memoire.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Date

class LockCapsuleDialogFragment(
    private val capsuleId: Long,
    private val onLockSet: (Date) -> Unit
) : DialogFragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: DialogLockCapsuleBinding
    private var selectedCalendar: Calendar = Calendar.getInstance()
    private var friendsList: List<UserEntity> = emptyList()
    private val currentCalendar: Calendar = Calendar.getInstance()

    sealed class AccessType {
        object OnlyMe : AccessType()
        object Public : AccessType()
        object Friends : AccessType()
        data class SpecificFriends(val friendIds: List<Long>) : AccessType()
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogLockCapsuleBinding.inflate(layoutInflater)

        // Initialize with current time + 1 hour as default
        selectedCalendar.add(Calendar.HOUR_OF_DAY, 1)

        // Load friends list when dialog is created
        loadFriends()

        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.radioSpecific.setOnClickListener {
            if (friendsList.isEmpty()) {
                Toast.makeText(context, "Loading friends...", Toast.LENGTH_SHORT).show()
                loadFriends()
            }
        }

        binding.btnConfirm.setOnClickListener {
            val accessType = when (binding.accessRadioGroup.checkedRadioButtonId) {
                R.id.radioPrivate -> AccessType.OnlyMe
                R.id.radioFriends -> AccessType.Friends
                R.id.radioPublic -> AccessType.Public // Correct mapping for Public access
                R.id.radioSpecific -> {
                    if (friendsList.isEmpty()) {
                        Toast.makeText(context, "No friends available to select", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    showFriendSelectionDialog()
                    return@setOnClickListener
                }
                else -> AccessType.OnlyMe // Default to OnlyMe
            }

            lockCapsuleWithAccess(accessType)
        }

        updateDateTimeDisplay()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            this,
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        // Set minimum date to today (can't lock for past dates)
        val today = Calendar.getInstance()
        datePicker.datePicker.minDate = today.timeInMillis
        datePicker.show()
    }

    private fun showTimePicker() {
        // Get current time to compare
        val now = Calendar.getInstance()

        // Initialize with current selection or current time if needed
        val initialHour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = selectedCalendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            this,
            initialHour,
            initialMinute,
            false
        )

        // If selected date is today, we need to validate the time
        if (isSameDay(selectedCalendar, now)) {
            timePickerDialog.setOnShowListener {
                // We'll validate the time selection when the user confirms
            }
        }

        timePickerDialog.show()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        selectedCalendar.set(year, month, day)

        val now = Calendar.getInstance()
        if (isSameDay(selectedCalendar, now)) {
            // Ensure the time is valid for today
            if (selectedCalendar.timeInMillis <= now.timeInMillis) {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                selectedCalendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE) + 1)
            }
        }

        updateDateTimeDisplay()
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH),
            hour,
            minute
        )

        val now = Calendar.getInstance()
        if (isSameDay(tempCalendar, now) && tempCalendar.timeInMillis <= now.timeInMillis) {
            Toast.makeText(context, "Cannot select a past time for today", Toast.LENGTH_SHORT)
                .show()
            selectedCalendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
            selectedCalendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE) + 1)
        } else {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
            selectedCalendar.set(Calendar.MINUTE, minute)
        }

        updateDateTimeDisplay()
    }

    private fun updateDateTimeDisplay() {
        val dateText =
            android.text.format.DateFormat.format("MMM dd, yyyy", selectedCalendar.time).toString()
        binding.btnSelectDate.text = dateText

        // Format time as "hh:mm a" (e.g., "02:30 PM")
        val timeText =
            android.text.format.DateFormat.format("hh:mm a", selectedCalendar.time).toString()
        binding.btnSelectTime.text = timeText

        // Enable confirm button only if date is in future
        val now = Calendar.getInstance()
        binding.btnConfirm.isEnabled = selectedCalendar.timeInMillis > now.timeInMillis

        if (!binding.btnConfirm.isEnabled) {
            Toast.makeText(context, "Please select a future date and time", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loadFriends() {
        RetrofitClient.instance.getFriendsList().enqueue(object : Callback<List<UserEntity>> {
            override fun onResponse(
                call: Call<List<UserEntity>>,
                response: Response<List<UserEntity>>
            ) {
                if (response.isSuccessful) {
                    friendsList = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Failed to load friends", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<UserEntity>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showFriendSelectionDialog() {
        if (friendsList.isEmpty()) {
            Toast.makeText(context, "No friends available", Toast.LENGTH_SHORT).show()
            return
        }

        FriendSelectionDialogFragment(friendsList) { selectedFriendIds ->
            lockCapsuleWithAccess(AccessType.SpecificFriends(selectedFriendIds))
        }.show(parentFragmentManager, "FriendSelectionDialog")
    }

    private fun lockCapsuleWithAccess(accessType: AccessType) {
        val formattedDate = DateUtils.formatForApi(selectedCalendar.time)
        val lockRequest = LockRequest(openDate = formattedDate)

        Log.d("TimeCapsule", "Locking capsule with access type: $accessType")
        handleAccessAfterLock(accessType)
        RetrofitClient.instance.lockTimeCapsule(capsuleId, lockRequest).enqueue(
            object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("TimeCapsule", "Capsule locked successfully. Proceeding with access setup.")

                        onLockSet(selectedCalendar.time)
                        Toast.makeText(context, "Capsule locked successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("TimeCapsule", "Failed to lock capsule: ${response.code()}")
                        Toast.makeText(context, "Failed to lock capsule: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("TimeCapsule", "Error locking capsule: ${t.message}", t)
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun handleAccessAfterLock(accessType: AccessType) {
        Log.d("TimeCapsule", "Handling access type: $accessType")
        when (accessType) {
            is AccessType.OnlyMe -> {
                Log.d("TimeCapsule", "Calling restrictAccessToOwner API")
                restrictAccessToOwner()
            }
            is AccessType.Public -> {
                Log.d("TimeCapsule", "Calling grantPublicAccess API")
                grantPublicAccess()
            }
            is AccessType.Friends -> {
                Log.d("TimeCapsule", "Calling grantAccessToAllFriends API")
                grantAccessToAllFriends()
            }
            else -> {
                Log.d("TimeCapsule", "No specific access type to handle")
            }
        }
    }

    private fun restrictAccessToOwner() {
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

    private fun grantPublicAccess() {
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

    private fun grantAccessToAllFriends() {
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
}