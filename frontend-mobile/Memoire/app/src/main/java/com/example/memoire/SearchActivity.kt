// SearchActivity.kt
package com.example.memoire.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.adapters.UserAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.com.example.memoire.HomeActivity
import com.example.memoire.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: UserAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText

    private var fullUserList = listOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerView = findViewById(R.id.recyclerViewUsers)
        searchInput = findViewById(R.id.searchinput)

        adapter = UserAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Back button to return to the home screen
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Live search functionality
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    fetchUsersFromApi(query)  // Make API call with search query
                } else {
                    fullUserList = emptyList()  // Clear the list if input is empty
                    adapter.updateList(fullUserList)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchUsersFromApi("")  // Fetch all users on initial load (optional)
    }

    private fun fetchUsersFromApi(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Make the API call to search users by name
                val response = RetrofitClient.instance.searchUsersByName(query)
                if (response.isSuccessful) {
                    val users = response.body()
                    if (!users.isNullOrEmpty()) {
                        fullUserList = users
                        withContext(Dispatchers.Main) {
                            adapter.updateList(fullUserList)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SearchActivity, "No users found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SearchActivity, "Failed to fetch users: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
