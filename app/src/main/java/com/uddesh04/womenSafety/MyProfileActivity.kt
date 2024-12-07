package com.uddesh04.womenSafety

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.InputType
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


class MyProfileActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var trustedNumbersList: MutableList<String>
    private lateinit var adapter: TrustedContactsAdapter
    private var user: FirebaseUser? = null
    private lateinit var imageViewProfile: ImageView

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private var storageReference: StorageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        // Firebase Authentication
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        imageViewProfile = findViewById(R.id.profileImageView)

        // Load profile image from Firebase
        loadProfileImage()

        // Set click listener to choose an image
        imageViewProfile.setOnClickListener {
            showImagePickerDialog()
        }

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_profile).isChecked = true

        // UI References
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val nameField = findViewById<TextView>(R.id.textUserName)
        val listView = findViewById<ListView>(R.id.listViewContacts)
        val addContactButton = findViewById<Button>(R.id.btnAddContact)
        val changePasswordButton = findViewById<Button>(R.id.btnChangePassword) // Change Password Button
        val dateOfBirthField = findViewById<EditText>(R.id.editTextDateOfBirth)
        val ageField = findViewById<EditText>(R.id.editTextAge)
        val saveAgeAndDobButton = findViewById<Button>(R.id.btnSaveAgeAndDob)

        saveAgeAndDobButton.setOnClickListener {
            val dob = dateOfBirthField.text.toString()
            val age = ageField.text.toString()

            if (dob.isEmpty() || age.isEmpty()) {
                Toast.makeText(this, "Please enter both Date of Birth and Age.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = user?.uid ?: return@setOnClickListener
            val database = FirebaseDatabase.getInstance().reference

            database.child("users").child(userId).child("date_of_birth").setValue(dob)
            database.child("users").child(userId).child("age").setValue(age)

            Toast.makeText(this, "Age and Date of Birth updated successfully!", Toast.LENGTH_SHORT).show()
        }

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

        // Handle Change Password
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Profile Image")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take a photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { uploadImageToFirebase(it) }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let { uploadImageToFirebase(it) }
        }
    }

    private fun uploadImageToFirebase(image: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageData = byteArrayOutputStream.toByteArray()

        val ref = storageReference.child("profile_images/${user?.uid}.jpg")
        ref.putBytes(imageData)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToDatabase(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val ref = storageReference.child("profile_images/${user?.uid}.jpg")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToDatabase(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val database = FirebaseDatabase.getInstance().reference
        user?.uid?.let {
            database.child("users").child(it).child("profile_image").setValue(imageUrl)
            Glide.with(this).load(imageUrl).into(imageViewProfile)
            Toast.makeText(this, "Image updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage() {
        val database = FirebaseDatabase.getInstance().reference
        user?.uid?.let {
            database.child("users").child(it).child("profile_image").get()
                .addOnSuccessListener { snapshot ->
                    val imageUrl = snapshot.getValue(String::class.java)
                    imageUrl?.let {
                        Glide.with(this).load(it).into(imageViewProfile)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load profile image", Toast.LENGTH_SHORT).show()
                }
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
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val oldPasswordInput = dialogView.findViewById<EditText>(R.id.editTextOldPassword)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.editTextNewPassword)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.editTextConfirmPassword)

        // Eye icons
        val toggleOldPasswordVisibility = dialogView.findViewById<ImageView>(R.id.toggleOldPasswordVisibility)
        val toggleNewPasswordVisibility = dialogView.findViewById<ImageView>(R.id.toggleNewPasswordVisibility)
        val toggleConfirmPasswordVisibility = dialogView.findViewById<ImageView>(R.id.toggleConfirmPasswordVisibility)

        // Handle eye icon click for password visibility toggle
        setupPasswordToggle(toggleOldPasswordVisibility, oldPasswordInput)
        setupPasswordToggle(toggleNewPasswordVisibility, newPasswordInput)
        setupPasswordToggle(toggleConfirmPasswordVisibility, confirmPasswordInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<Button>(R.id.btnSubmitChange).setOnClickListener {
            val oldPassword = oldPasswordInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.isEmpty() || oldPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(user!!.email!!, oldPassword)
            user?.reauthenticate(credential)
                ?.addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user?.updatePassword(newPassword)
                            ?.addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(this, "Failed to update password.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Old password is incorrect.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        dialog.show()
    }

    // Helper function to toggle password visibility using an ImageView (eye icon)
    private fun setupPasswordToggle(toggleIcon: ImageView, editText: EditText) {
        toggleIcon.setOnClickListener {
            if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleIcon.setImageResource(R.drawable.ic_eye_off) // Update icon to "eye-off"
            } else {
                // Hide password
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleIcon.setImageResource(R.drawable.ic_eye) // Update icon to "eye"
            }
            // Move cursor to the end of the text
            editText.setSelection(editText.text.length)
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