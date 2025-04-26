//package com.example.memoire
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.os.bundleOf
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.memoire.adapter.UserSearchAdapter
//import com.example.memoire.models.UserSearchDTO
//import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//class AddUsersFragment : Fragment() {
//    private lateinit var binding: FragmentAddUsersBinding
//    private lateinit var adapter: UserSearchAdapter
//    private var searchJob: Job? = null
//
//    companion object {
//        fun newInstance(capsuleId: Long) = AddUsersFragment().apply {
//            arguments = bundleOf("capsuleId" to capsuleId)
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        binding = FragmentAddUsersBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        adapter = UserSearchAdapter(emptyList()) { user ->
//            showRoleSelectionDialog(user)
//        }
//
//        binding.recyclerView.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = this@AddUsersFragment.adapter
//        }
//
//        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
//            searchJob?.cancel()
//            searchJob = CoroutineScope(Dispatchers.Main).launch {
//                delay(300)
//                text?.let { searchUsers(it.toString()) }
//            }
//        }
//    }
//
//    private fun showRoleSelectionDialog(user: UserSearchDTO) {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Grant access to ${user.username}")
//            .setItems(arrayOf("Editor", "Viewer")) { _, which ->
//                grantAccess(user.id, if (which == 0) "EDITOR" else "VIEWER")
//            }
//            .show()
//    }
//}