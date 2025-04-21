package com.example.memoire

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.example.memoire.databinding.DialogLockCapsuleBinding
import java.util.Calendar
import java.util.Date

class LockCapsuleDialogFragment(
    private val onLockSet: (Date) -> Unit
) : DialogFragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: DialogLockCapsuleBinding
    private var selectedCalendar: Calendar = Calendar.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogLockCapsuleBinding.inflate(layoutInflater)

        // Initialize with current time + 1 hour as default
        selectedCalendar.add(Calendar.HOUR_OF_DAY, 1)

        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            onLockSet(selectedCalendar.time)
            dismiss()
        }

        updateDateTimeDisplay()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
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
        // Set minimum date to tomorrow (can't lock for past dates)
        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.datePicker.minDate = minCalendar.timeInMillis
        datePicker.show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            this,
            selectedCalendar.get(Calendar.HOUR_OF_DAY),
            selectedCalendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        selectedCalendar.set(year, month, day)
        updateDateTimeDisplay()
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
        selectedCalendar.set(Calendar.MINUTE, minute)
        updateDateTimeDisplay()
    }

    private fun updateDateTimeDisplay() {
        binding.tvSelectedDate.text = android.text.format.DateFormat.getDateFormat(context).format(selectedCalendar.time)
        binding.tvSelectedTime.text = android.text.format.DateFormat.getTimeFormat(context).format(selectedCalendar.time)

        // Enable confirm button only if date is in future
        val now = Calendar.getInstance()
        binding.btnConfirm.isEnabled = selectedCalendar.timeInMillis > now.timeInMillis
    }
}