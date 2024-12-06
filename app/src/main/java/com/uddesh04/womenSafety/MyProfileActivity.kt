package com.uddesh04.womenSafety

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MyProfileActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var trustedNumbersList: MutableList<String>
    private lateinit var adapter: TrustedContactsAdapter
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        // Firebase Authentication
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Bottom Navigation
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_profile).isChecked = true

        // UI References
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val nameField = findViewById<TextView>(R.id.textUserName)
        val listView = findViewById<ListView>(R.id.listViewContacts)
        val addContactButton = findViewById<Button>(R.id.btnAddContact)

        // Load user details
        fetchUserDetails(nameField, emailField)

        // Initialize trusted contacts
        trustedNumbersList = mutableListOf()
        adapter = TrustedContactsAdapter(this, trustedNumbersList) { contact ->
            deleteContact(contact)
        }
        listView.adapter = adapter
        fetchTrustedContacts()

        // Add new contact
        addContactButton.setOnClickListener {
            if (trustedNumbersList.size >= 4) {
                Toast.makeText(this, "You can only add up to 4 trusted contacts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            startActivityForResult(pickContactIntent, CONTACT_PICKER_REQUEST)
        }
    }

    private fun fetchUserDetails(nameField: TextView, emailField: EditText) {
        val userId = user?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference

        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown User"
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""

                    // Populate fields
                    nameField.text = name
                    emailField.setText(email)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyProfileActivity, "Error loading profile data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTrustedContacts() {
        val userId = user?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference

        database.child("users").child(userId).child("trusted_contacts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trustedNumbersList.clear()
                    snapshot.children.mapNotNullTo(trustedNumbersList) { it.getValue(String::class.java) }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyProfileActivity, "Failed to load contacts: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteContact(contact: String) {
        trustedNumbersList.remove(contact)
        adapter.notifyDataSetChanged()
        saveTrustedContacts()
        Toast.makeText(this, "Contact deleted successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun saveTrustedContacts() {
        val userId = user?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(userId).child("trusted_contacts").setValue(trustedNumbersList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val contactUri = data?.data ?: return
            val cursor = contentResolver.query(contactUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val number = it.getString(numberIndex)
                    if (!trustedNumbersList.contains(number)) {
                        trustedNumbersList.add(number)
                        adapter.notifyDataSetChanged()
                        saveTrustedContacts()
                        Toast.makeText(this, "Contact Added: $number", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Contact already exists.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        private const val CONTACT_PICKER_REQUEST = 1
    }
}