package com.uddesh04.womenSafety

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.versionedparcelable.ParcelField
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MyProfileActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var trustedNumbersList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)


        // Firebase Authentication
        auth = Firebase.auth
        user = auth.currentUser

        // Set up toolbar with back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_profile).isChecked = true

        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Get UI references
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val nameField = findViewById<TextView>(R.id.textUserName)
        val dobField = findViewById<TextView>(R.id.textDateOfBirth)
        val ageField = findViewById<TextView>(R.id.textAge)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val addContactButton = findViewById<Button>(R.id.btnAddContact)
        val listView = findViewById<ListView>(R.id.listViewContacts)


        // Load user details
        val profileImageView = findViewById<ImageView>(R.id.profileImageView)
        fetchUserDetails(nameField, dobField, ageField, emailField, profileImageView)

        //call of login with google
        emailField.setText(user?.email)
        nameField.text = user?.displayName ?: "Unknown User"
//

        // Load Trusted Contacts
        trustedNumbersList = getTrustedContacts()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, trustedNumbersList)
        listView.adapter = adapter

        // Set Trusted Contact
        listView.setOnItemClickListener { _, _, position, _ ->
            setTrustedContact(trustedNumbersList[position])
            Toast.makeText(this, "Trusted Contact Updated!", Toast.LENGTH_SHORT).show()


        }

        // Handle long press to delete a trusted contact
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val contactToDelete = trustedNumbersList[position]
            val deleteDialog = android.app.AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete") { _, _ ->
                    trustedNumbersList.remove(contactToDelete)
                    adapter.notifyDataSetChanged()
                    saveTrustedContacts(trustedNumbersList)

                    // Update SharedPreferences and Firebase
                    val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
                    val currentTrustedContact = sharedPreferences.getString("ENUM", "")

                    if (contactToDelete == currentTrustedContact) {
                        val myEdit = sharedPreferences.edit()
                        myEdit.putString("ENUM", trustedNumbersList.firstOrNull() ?: "")
                        myEdit.apply()
                    }

                    val userId = user?.uid ?: return@setPositiveButton
                    val databaseRef = FirebaseDatabase.getInstance().reference
                    databaseRef.child("users").child(userId).child("trusted_contacts")
                        .setValue(trustedNumbersList)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Contact deleted successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            deleteDialog.show()
            true
        }

        // Save Email Changes
        findViewById<Button>(R.id.btnSaveEmail).setOnClickListener {
            val newEmail = emailField.text.toString()
            updateEmail(newEmail)
        }

        // Save Password Changes
        findViewById<Button>(R.id.btnSavePassword).setOnClickListener {
            val newPassword = passwordField.text.toString()
            updatePassword(newPassword)
        }

        // Add New Contact
        addContactButton.setOnClickListener {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            startActivityForResult(pickContactIntent, CONTACT_PICKER_REQUEST)
        }
    }

    private fun fetchUserDetails(
        nameField: TextView,
        dobField: TextView,
        ageField: TextView,
        emailField: EditText,
        profileImageView: ImageView
    ) {
        val userId = user?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference

        // Fetch data from the Realtime Database
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown User"
                    val dob = snapshot.child("date_of_birth").getValue(String::class.java) ?: "N/A"
                    val age = snapshot.child("age").getValue(String::class.java) ?: "N/A"
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    val photoUrl = snapshot.child("photo_url").getValue(String::class.java)

                    // Set data to the respective fields
                    nameField.text = name
                    dobField.text = dob
                    ageField.text = age
                    emailField.setText(email)

                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this@MyProfileActivity)
                            .load(photoUrl)
                            .placeholder(R.drawable.default_profile_picture)
                            .into(profileImageView)
                    }else {
                        profileImageView.setImageResource(R.drawable.default_profile_picture)}

                } else {
                    Toast.makeText(this@MyProfileActivity, "User details not found in the database.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyProfileActivity, "Error loading profile data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun getTrustedContacts(): MutableList<String> {
//        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
//        val contacts = sharedPreferences.getStringSet("TRUSTED_CONTACTS", emptySet()) ?: emptySet()
//        return contacts.toMutableList()
        val userId = user?.uid ?: return mutableListOf()
        val contactsList = mutableListOf<String>()
        val databaseRef = FirebaseDatabase.getInstance().reference

        databaseRef.child("users").child(userId).child("trusted_contacts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.children.forEach { contact ->
                            contact.getValue(String::class.java)?.let { contactsList.add(it) }
                        }
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyProfileActivity, "Failed to load contacts: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        return contactsList

    }

    private fun saveTrustedContacts(contacts: List<String>) {
//        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putStringSet("TRUSTED_CONTACTS", contacts.toSet())
//        editor.apply()
        val userId = user?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().reference
        databaseRef.child("users").child(userId).child("trusted_contacts").setValue(contacts)
    }

    private fun setTrustedContact(contact: String) {
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("ENUM", contact)
        editor.apply()
    }

    private fun updateEmail(newEmail: String) {
        user?.updateEmail(newEmail)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val database = FirebaseDatabase.getInstance().reference
                    database.child("users").child(user?.uid?:"").child("emial")
                        .setValue(newEmail)
                    Toast.makeText(this, "Email Updated Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to Update Email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updatePassword(newPassword: String) {
        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to Update Password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
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
                    trustedNumbersList.add(number)
                    adapter.notifyDataSetChanged()
                    saveTrustedContacts(trustedNumbersList)
                    Toast.makeText(this, "Contact Added: $number", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val CONTACT_PICKER_REQUEST = 1
    }
}
