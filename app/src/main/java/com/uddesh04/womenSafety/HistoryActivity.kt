package com.uddesh04.womenSafety

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.uddesh04.womenSafety.adapter.HistoryAdapter
import com.uddesh04.womenSafety.SOSRecord

class HistoryActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val recordsList = mutableListOf<SOSRecord>()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(recordsList)
        recyclerView.adapter = historyAdapter

        // Set up Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)

        // Fetch Data from Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        fetchHistoryData()
    }

    private fun fetchHistoryData() {
        val userId = auth.currentUser?.uid ?: return
        val sosServiceRef = database.child("users").child(userId).child("sos_service_tp")

        sosServiceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recordsList.clear()
                for (recordSnapshot in snapshot.children) {
                    val startTime = recordSnapshot.child("start_time").getValue(String::class.java) ?: ""
                    val stopTime = recordSnapshot.child("stop_time").getValue(String::class.java) ?: ""
                    val duration = recordSnapshot.child("duration").getValue(Long::class.java) ?: 0L

                    val sosRecord = SOSRecord(startTime, stopTime, duration)
                    recordsList.add(sosRecord)
                }
                historyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
