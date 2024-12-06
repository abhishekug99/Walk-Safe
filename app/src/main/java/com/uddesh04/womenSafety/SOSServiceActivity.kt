package com.uddesh04.womenSafety

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.widget.Toolbar


class SOSServiceActivity : BaseActivity() {
    private lateinit var multiplePermissions: ActivityResultLauncher<Array<String>>
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos_service)

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_profile).isChecked = true

        //Toolbar
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        val popupMenuButton = findViewById<ImageButton>(R.id.btnPopupMenu)
        popupMenuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)



        // Initialize the permissions launcher
        multiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            handlePermissionsResult(result)
        }

        // Set up the notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MYID",
                "CHANNELFOREGROUND",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        displayTrustedContact()

//        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
//        setupBottomNavigation(bottomNavigationView)
//        bottomNavigationView.menu.findItem(R.id.nav_profile).isChecked = true
    }

    //toolBarHandle
    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, HomeScreenActivity::class.java) // Navigate to HomeActivity
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear the back stack
        startActivity(intent)
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        displayTrustedContact() // Refresh trusted contact display if the user updates it
    }


    private fun displayTrustedContact() {
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val trustedNumber = sharedPreferences.getString("ENUM", "NONE")
        val textView = findViewById<TextView>(R.id.textNum)
        if (trustedNumber != "NONE") {
            textView.text = "SOS Will Be Sent To\n$trustedNumber"
        } else {
            textView.text = "No trusted contact set. Please register a number."
        }
    }


    //NEW handle permissions
    private fun handlePermissionsResult(result: Map<String, Boolean>) {
        if (result.values.any { !it }) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "All permissions must be granted for SOS functionality!",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Grant Permissions") {
                requestPermissions()
            }.show()
        }
    }
    // Handle permissions result
//    private fun handlePermissionsResult(result: Map<String, Boolean>) {
//        for ((permission, isGranted) in result) {
//            if (!isGranted) {
//                // Show a snackbar if any permission is denied
//                val snackbar = Snackbar.make(
//                    findViewById(android.R.id.content),
//                    "Permission Must Be Granted!",
//                    Snackbar.LENGTH_INDEFINITE
//                )
//                snackbar.setAction("Grant Permission") {
//                    multiplePermissions.launch(arrayOf(permission))
//                    snackbar.dismiss()
//                }
//                snackbar.show()
//            }
//        }
//    }

    // Start the SOS service if permissions are granted
    fun startService(view: View?) {
        if (checkPermissions()) {
            val intent = Intent(this, ServiceMine::class.java).apply { action = "START" }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
                //applicationContext.startForegroundService(intent)
                showSnackbar("Service Started!")
            } else {
                startService(intent)
            }
        } else {
            requestPermissions()
        }
    }

    // Stop the SOS service
    fun stopService(view: View?) {
        val intent = Intent(this, ServiceMine::class.java).apply { action = "STOP" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
//            applicationContext.startForegroundService(intent)
            showSnackbar("Service Stopped!")
        } else {
            stopService(intent)
        }
    }

    // Helper function to show a snackbar message
    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    // Check if required permissions are granted
    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Request necessary permissions
    private fun requestPermissions() {
        multiplePermissions.launch(
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private fun handleLogoutSuccess() {
        Toast.makeText(this, "Logged Out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Display popup menu for additional options
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.changeNumber -> {
                    startActivity(Intent(this, RegisterNumberActivity::class.java))
                    true
                }

                R.id.logOut -> {
                    // Check if the user is signed in with Google
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
                        if (googleSignInAccount != null) {
                            // Google sign out and revoke access
                            googleSignInClient.revokeAccess().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    auth.signOut()
                                    handleLogoutSuccess()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to log out from Google",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            // Firebase (Email/Password) logout
                            auth.signOut()
                            handleLogoutSuccess()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to log out. No active user.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }
}

