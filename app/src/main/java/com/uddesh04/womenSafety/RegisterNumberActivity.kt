package com.uddesh04.womenSafety

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class RegisterNumberActivity : BaseActivity() {
    private var number: TextInputEditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_number)

        number = findViewById(R.id.numberEdit)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back arrow
        supportActionBar?.title = "Register Trusted Contact"

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)

    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Handle the back button click to navigate back
        return true
    }

    fun saveNumber(view: View?) {
        val numberString = number!!.text.toString()
        if (numberString.length == 10) {
            val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("ENUM", numberString)
            myEdit.apply()

            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid
                val databaseRef = FirebaseDatabase.getInstance().reference
                databaseRef.child("users").child(userId).child("trusted_contacts")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val contacts = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                            if (!contacts.contains(numberString)) {
                                contacts.add(numberString)
                                databaseRef.child("users").child(userId).child("trusted_contacts").setValue(contacts)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@RegisterNumberActivity, "Trusted Contact Saved!", Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener { error ->
                                        Toast.makeText(this@RegisterNumberActivity, "Error saving contact: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this@RegisterNumberActivity, "Contact already exists.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@RegisterNumberActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }


            Toast.makeText(this, "Trusted Contact Saved!", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, SOSServiceActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the back stack
//            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Enter Valid Number!", Toast.LENGTH_SHORT).show()
        }
    }
}
