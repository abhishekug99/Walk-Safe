package com.uddesh04.womenSafety

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.type.Date
import java.io.File
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var profilePhotoUri: Uri
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_IMAGE_CAPTURE = 1



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Check if user is already signed in and redirect
        if (auth.currentUser != null) {
            navigateToHomeScreenActivity()
            return
        }



        // Initialize Google Sign-In Client
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        firebaseAuthWithGoogle(idToken)
                    } else {
                        Toast.makeText(this, "Google sign-in failed: No ID token!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show()
            }
        }


        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            registerUser()
        }

        findViewById<Button>(R.id.btnUploadFromGallery).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        findViewById<Button>(R.id.btnCapturePhoto).setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                val photoFile: File? = createImageFile()
                photoFile?.let {
                    profilePhotoUri  = FileProvider.getUriForFile(this, "com.uddesh04.womenSafety.fileprovider", it)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, profilePhotoUri )
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

    }




    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToHomeScreenActivity()
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerUser() {
        val firstName = findViewById<EditText>(R.id.editTextFirstName).text.toString()
        val lastName = findViewById<EditText>(R.id.editTextLastName).text.toString()
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
        val dateOfBirth = findViewById<EditText>(R.id.editTextBirthDate).text.toString()
        val age = findViewById<EditText>(R.id.editTextAge).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
        val confirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword).text.toString()

        // Validate form
        if (firstName.isEmpty() || lastName.isEmpty() || age.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

//        DO NOT REMOVE BELOW CODE <-- ABHISHEK
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    saveUserDetailsToDatabase(user, firstName, lastName, age, dateOfBirth)
//                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
//                    navigateToHomeScreenActivity()
//                } else {
////                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//
//                }
//            }
//
//        // Here you could add code to save the user’s information to your database or Firebase.
//
//        // Show success message and navigate to login or main activity
//        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
//        navigateToHomeScreenActivity()
//    }

        checkIfEmailExists(email) { emailExists ->
            if (emailExists) {
                showEmailExistsPopup()
            } else {
                // Proceed with registration
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            saveUserDetailsToDatabase(user, firstName, lastName, age, dateOfBirth)
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                            navigateToHomeScreenActivity()
                        }
                        else {
                            showEmailExistsPopup()
//                            Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    profilePhotoUri = data?.data!!
                    findViewById<ImageView>(R.id.profileImagePreview).setImageURI(profilePhotoUri)
                }
                REQUEST_IMAGE_CAPTURE -> {
                    findViewById<ImageView>(R.id.profileImagePreview).setImageURI(profilePhotoUri)
                }
            }
        }
    }

    private fun saveUserDetailsToDatabase(
        user: FirebaseUser?,
        firstName: String? = null,
        lastName: String? = null,
        age: String? = null,
        dateOfBirth: String? = null
    ){
        val database = FirebaseDatabase.getInstance().reference
        val userId = user?.uid

        val userDetails = mutableMapOf<String, Any?>(
            "email" to user?.email,
            "name" to (user?.displayName ?: "$firstName $lastName"),
            "age" to age,
            "date_of_birth" to dateOfBirth
        )
        userId?.let{ id ->
            if (this::profilePhotoUri.isInitialized) {
                val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/$id.jpg")
                storageRef.putFile(profilePhotoUri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        userDetails["photo_url"] = uri.toString()
                        database.child("users").child(id).setValue(userDetails)
                    }.addOnFailureListener{Toast.makeText(this, "Failed to get photo URL: ${it.message}", Toast.LENGTH_SHORT).show()}

                    }.addOnFailureListener { exception ->
                    Log.e("FirebaseUpload", "Error uploading file", exception)}

            }
            else {
            database.child("users").child(id).setValue(userDetails)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createImageFile(): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timestamp}_", ".jpg", storageDir).apply {
            profilePhotoUri = Uri.fromFile(this)
    }
        }

    private fun checkIfEmailExists(email: String, callback: (Boolean) -> Unit) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    callback(!signInMethods.isNullOrEmpty()) // Email exists if signInMethods is not empty
                } else {
//                    Toast.makeText(this, "Error checking email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    callback(false) // Assume the email doesn't exist in case of error
                }
            }
    }

    private fun showEmailExistsPopup() {
        runOnUiThread{
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Email Already Exists")
        builder.setMessage("The email ID you entered is already registered. Please log in instead.")
        builder.setPositiveButton("OK") { _, _ ->
            // Navigate to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Dismiss dialog and stay on the registration page
            dialog.dismiss()
        }
        builder.create().show()
    }
    }


    private fun navigateToRegisterNumberActivit(){
        startActivity(Intent(this,RegisterNumberActivity::class.java))
    }
//    private fun navigateToMainActivity() {
//        startActivity(Intent(this, MainActivity::class.java))
//        finish()
//    }

    private fun navigateToHomeScreenActivity() {
        startActivity(Intent(this, HomeScreenActivity::class.java))
        finish()
    }
}
