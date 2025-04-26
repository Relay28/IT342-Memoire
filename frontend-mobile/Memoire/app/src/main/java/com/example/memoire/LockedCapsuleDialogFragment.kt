//package com.example.memoire
//
//import android.app.DatePickerDialog
//import android.app.Dialog
//import android.app.TimePickerDialog
//import android.os.Bundle
//import android.view.WindowManager
//import android.widget.Toast
//import androidx.fragment.app.DialogFragment
//import com.example.memoire.api.RetrofitClient
//import com.example.memoire.databinding.DialogLockCapsuleBinding
//import com.example.memoire.models.CapsuleAccessDTO
//import com.example.memoire.models.GrantAccessRequest
//import com.example.memoire.models.UserEntity
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.util.Calendar
//import java.util.Date
//
//class LockCapsuleDialogFragment(
//    private val capsuleId: Long,
//    private val onLockSet: (Date, AccessType) -> Unit
//) : DialogFragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
//
//    private lateinit var binding: DialogLockCapsuleBinding
//    private var selectedCalendar: Calendar = Calendar.getInstance()
//    private var friendsList: List<UserEntity> = emptyList()
//
//    sealed class AccessType {
//        object Private : AccessType()
//        object AllFriends : AccessType()
//        data class SpecificFriends(val friendIds: List<Long>) : AccessType()
//    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        binding = DialogLockCapsuleBinding.inflate(layoutInflater)
//
//        // Initialize with current time + 1 hour as default
//        selectedCalendar.add(Calendar.HOUR_OF_DAY, 1)
//
//        // Load friends list
//        loadFriends()
//
//        binding.btnSelectDate.setOnClickListener { showDatePicker() }
//        binding.btnSelectTime.setOnClickListener { showTimePicker() }
//        binding.btnCancel.setOnClickListener { dismiss() }
//
//        binding.radioSpecific.setOnClickListener {
//            if (friendsList.isEmpty()) {
//                Toast.makeText(context, "Loading friends...", Toast.LENGTH_SHORT).show()
//                loadFriends()
//            }
//        }
//
//        binding.btnConfirm.setOnClickListener {
//            val accessType = when (binding.accessRadioGroup.checkedRadioButtonId) {
//                R.id.radioPrivate -> AccessType.Private
//                R.id.radioFriends -> AccessType.AllFriends
//                R.id.radioSpecific -> {
//                    showFriendSelectionDialog()
//                    return@setOnClickListener
//                }
//                else -> AccessType.Private
//            }
//
//            confirmLock(accessType)
//        }
//
//        updateDateTimeDisplay()
//
//        return Dialog(requireContext()).apply {
//            setContentView(binding.root)
//            setCancelable(true)
//            window?.setLayout(
//                (resources.displayMetrics.widthPixels * 0.9).toInt(),
//                WindowManager.LayoutParams.WRAP_CONTENT
//            )
//        }
//    }
//
//
//
//    // Rest of your existing methods (showDatePicker, showTimePicker, onDateSet, onTimeSet, updateDateTimeDisplay)
//    // ...
//}