package com.example.memoire

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.memoire.activities.SearchActivity
import com.example.memoire.api.RetrofitClient
import com.example.memoire.fragments.CapsuleListFragment
import com.example.memoire.fragments.FriendListFragment
import com.example.memoire.fragments.HomeFragment
import com.example.memoire.fragments.LockedCapsulesFragment
import com.example.memoire.fragments.NotificationFragment
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainContainerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        // Set up bottom navigation
        setupBottomNavigation()

        // Set up header actions
        setupHeaderActions()

        // Load initial fragment based on intent or default
        if (savedInstanceState == null) {
            val initialFragment = when (intent?.getStringExtra("fragment")) {
                "capsuleList" -> CapsuleListFragment()
                "friendList" -> FriendListFragment()
                "lockedCapsules" -> LockedCapsulesFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, initialFragment)
                .commit()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_add -> {
                    createNewTimeCapsule()
                    true
                }
                R.id.navigation_tags -> {
                    replaceFragment(CapsuleListFragment())
                    true
                }
                R.id.navigation_friendList -> {
                    replaceFragment(FriendListFragment())
                    true
                }
                R.id.navigation_timer -> {
                    replaceFragment(LockedCapsulesFragment())
                    true
                }
                else -> false
            }
        }
    }

    protected fun setupHeaderActions() {
        findViewById<ImageView>(R.id.prof)?.setOnClickListener {
            navigateTo(ProfileActivity::class.java)
        }

        findViewById<ImageView>(R.id.ivNotification)?.setOnClickListener {
            replaceFragment(NotificationFragment())
        }

        findViewById<ImageView>(R.id.ivSearch)?.setOnClickListener {
            navigateTo(SearchActivity::class.java)
        }
    }
    private fun <T : Activity> navigateTo(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    protected open fun showLoading() {
        // Can be overridden by child activities if needed
    }

    protected open fun hideLoading() {
        // Can be overridden by child activities if needed
    }
    private fun createNewTimeCapsule() {
        val newCapsule = TimeCapsuleDTO(
            title = "Untitled",
            description = ""
        )

        showLoading()

        RetrofitClient.instance.createTimeCapsule(newCapsule).enqueue(object :
            Callback<TimeCapsuleDTO> {
            override fun onResponse(
                call: Call<TimeCapsuleDTO>,
                response: Response<TimeCapsuleDTO>
            ) {
                hideLoading()

                if (response.isSuccessful && response.body() != null) {
                    val createdCapsule = response.body()!!
                    val intent =
                        Intent(this@MainContainerActivity, CapsuleDetailActivity::class.java).apply {
                            putExtra("capsuleId", createdCapsule.id.toString())
                            putExtra("isNewCapsule", true)
                        }
                    startActivity(intent)
                } else {
                    showError("Failed to create capsule: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TimeCapsuleDTO>, t: Throwable) {
                hideLoading()
                showError("Error: ${t.message}")
            }
        })
    }

    protected open fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}