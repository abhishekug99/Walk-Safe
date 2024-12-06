package com.uddesh04.womenSafety

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterNumberActivity : BaseActivity() {
    private var number: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_number)

        // Initialize UI components
        number = findViewById(R.id.numberEdit)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register Trusted Contact"

        // Set up Bottom Navigation and highlight "Contacts"
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_contacts).isChecked = true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun saveNumber(view: View?) {
        val numberString = number?.text.toString()

        if (numberString.length == 10) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid
                val databaseRef = FirebaseDatabase.getInstance().reference

                // Add trusted contact if less than 4 contacts exist
                databaseRef.child("users").child(userId).child("trusted_contacts")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val contacts = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                            if (contacts.size < 4) {
                                if (!contacts.contains(numberString)) {
                                    contacts.add(numberString)
                                    databaseRef.child("users").child(userId).child("trusted_contacts").setValue(contacts)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@RegisterNumberActivity,
                                                "Trusted Contact Saved!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        }.addOnFailureListener { error ->
                                            Toast.makeText(
                                                this@RegisterNumberActivity,
                                                "Error saving contact: ${error.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        this@RegisterNumberActivity,
                                        "Contact already exists.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@RegisterNumberActivity,
                                    "Cannot add more than 4 trusted contacts.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@RegisterNumberActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                Toast.makeText(this, "User not logged in. Please log in and try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Enter a valid 10-digit number!", Toast.LENGTH_SHORT).show()
        }
    }
}