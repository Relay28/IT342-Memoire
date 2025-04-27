package com.example.memoire

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.memoire.api.RetrofitClient
import com.example.memoire.com.example.memoire.HomeActivity
import com.example.memoire.com.example.memoire.NotificationActivity
import com.example.memoire.activities.SearchActivity
import com.example.memoire.models.TimeCapsuleDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseActivity : AppCompatActivity() {

    protected fun setupBottomNavigation(selectedItemId: Int = -1) {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)

        if (selectedItemId != -1) {
            bottomNavigationView.selectedItemId = selectedItemId
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (this !is HomeActivity) {
                        navigateTo(HomeActivity::class.java)
                    }
                    true
                }
                R.id.navigation_add -> {
                    createNewTimeCapsule()
                    true
                }
                R.id.navigation_tags -> {
                    if (this !is CapsuleListActivity) {
                        navigateTo(CapsuleListActivity::class.java)
                    }
                    true
                }

                R.id.navigation_friendList -> {
                    if (this !is FriendListActivity) {
                        navigateTo(FriendListActivity::class.java)
                    }
                    true
                }
                R.id.navigation_timer -> {
                    if (this !is LockedCapsulesActivity) {
                        navigateTo(LockedCapsulesActivity::class.java)
                    }
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
            navigateTo(NotificationActivity::class.java)
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

    private fun createNewTimeCapsule() {
        val newCapsule = TimeCapsuleDTO(
            title = "Untitled",
            description = ""
        )

        showLoading()

        RetrofitClient.instance.createTimeCapsule(newCapsule).enqueue(object :
            Callback<TimeCapsuleDTO> {
            override fun onResponse(call: Call<TimeCapsuleDTO>, response: Response<TimeCapsuleDTO>) {
                hideLoading()

                if (response.isSuccessful && response.body() != null) {
                    val createdCapsule = response.body()!!
                    val intent = Intent(this@BaseActivity, CapsuleDetailActivity::class.java).apply {
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

    protected open fun showLoading() {
        // Can be overridden by child activities if needed
    }

    protected open fun hideLoading() {
        // Can be overridden by child activities if needed
    }

    protected open fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}