//package com.example.memoire;
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.os.bundleOf
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.memoire.adapter.CapsuleAccessAdapter;
//import com.example.memoire.api.RetrofitClient
//
//class CurrentAccessFragment : Fragment() {
//    private lateinit var binding: FragmentCurrentAccessBinding
//    private lateinit var adapter:CapsuleAccessAdapter
//
//    companion object {
//        fun newInstance(capsuleId: Long) = CurrentAccessFragment().apply {
//            arguments = bundleOf("capsuleId" to capsuleId)
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        binding = FragmentCurrentAccessBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        adapter = CapsuleAccessAdapter(mutableListOf(),
//                onRoleChange = { accessId, newRole -> updateRole(accessId, newRole) },
//                onRemove = { accessId -> removeAccess(accessId) }
//        )
//
//        binding.recyclerView.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = this@CurrentAccessFragment.adapter
//        }
//
//        loadAccessList()
//    }
//
//    private fun loadAccessList() {
//        RetrofitClient.instance.getCapsuleAccesses(requireArguments().getLong("capsuleId"))
//                .enqueue(/* ... */)
//    }
//}