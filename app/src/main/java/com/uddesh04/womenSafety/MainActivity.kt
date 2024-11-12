package com.uddesh04.womenSafety

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var multiplePermissions: ActivityResultLauncher<Array<String>>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val popupMenuButton = findViewById<Button>(R.id.btnPopupMenu)
        popupMenuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Initialize the permissions launcher
        multiplePermissions = registerForActivityResult(RequestMultiplePermissions()) { result ->
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
    }

    override fun onResume() {
        super.onResume()
        displayTrustedContact() // Refresh trusted contact display if the user updates it
    }

//    override fun onResume() {
//        super.onResume()
//        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
//        val trustedNumber = sharedPreferences.getString("ENUM", "NONE")
//        if (trustedNumber.equals("NONE", ignoreCase = true)) {
//            // Launch RegisterNumberActivity if no trusted contact is registered
//            startActivity(Intent(this, RegisterNumberActivity::class.java))
//        } else {
//            // Display the trusted number
//            val textView = findViewById<TextView>(R.id.textNum)
//            textView.text = "SOS Will Be Sent To\n$trustedNumber"
//        }
//    }

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


    // Handle permissions result
    private fun handlePermissionsResult(result: Map<String, Boolean>) {
        for ((permission, isGranted) in result) {
            if (!isGranted) {
                // Show a snackbar if any permission is denied
                val snackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    "Permission Must Be Granted!",
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setAction("Grant Permission") {
                    multiplePermissions.launch(arrayOf(permission))
                    snackbar.dismiss()
                }
                snackbar.show()
            }
        }
    }

    // Start the SOS service if permissions are granted
    fun startService(view: View?) {
        if (checkPermissions()) {
            val intent = Intent(this, ServiceMine::class.java).apply { action = "START" }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
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
            applicationContext.startForegroundService(intent)
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
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
                    auth.signOut()
                    Toast.makeText(this, "Logged Out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent (this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
