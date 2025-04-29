package com.example.memoire.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.memoire.FriendRequestsFragment
import com.example.memoire.FriendsFragment
import com.example.memoire.R
import com.example.memoire.viewmodels.FriendListViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FriendListFragment : Fragment() {
    private lateinit var viewModel: FriendListViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friend_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[FriendListViewModel::class.java]

        // Setup ViewPager and TabLayout
        setupViewPager()
    }

    private fun setupViewPager() {
        viewPager = requireView().findViewById(R.id.viewPager)
        tabLayout = requireView().findViewById(R.id.tabLayout)

        // Create adapter for the viewPager
        val adapter = FriendPagerAdapter(this)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Friends"
                1 -> "Requests"
                else -> ""
            }
        }.attach()
    }

    // ViewPager adapter to manage fragments
    private inner class FriendPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> FriendsFragment()
                1 -> FriendRequestsFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}