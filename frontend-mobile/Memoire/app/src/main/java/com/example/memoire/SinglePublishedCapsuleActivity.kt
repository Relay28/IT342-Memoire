package com.example.memoire

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.adapter.PublishedCapsulesAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.TimeCapsuleDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SinglePublishedCapsuleActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var publishedCapsulesAdapter: PublishedCapsulesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_published_capsule)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        val capsuleId = intent.getLongExtra("capsuleId", -1L)
        if (capsuleId != -1L) {
            fetchCapsuleDetails(capsuleId)
        }
    }

    private fun fetchCapsuleDetails(capsuleId: Long) {
        RetrofitClient.instance.getTimeCapsule(capsuleId).enqueue(object : Callback<TimeCapsuleDTO> {
            override fun onResponse(call: Call<TimeCapsuleDTO>, response: Response<TimeCapsuleDTO>) {
                if (response.isSuccessful) {
                    response.body()?.let { capsule ->
                        publishedCapsulesAdapter = PublishedCapsulesAdapter(
                            listOf(capsule),
                            onItemClick = {}, // No action needed
                            onCommentClick = {} // No action needed
                        )
                        recyclerView.adapter = publishedCapsulesAdapter
                    }
                }
            }

            override fun onFailure(call: Call<TimeCapsuleDTO>, t: Throwable) {
                // Handle error
            }
        })
    }
}