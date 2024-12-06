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
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SOSServiceActivity : BaseActivity() {
    private lateinit var multiplePermissions: ActivityResultLauncher<Array<String>>
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var trustedContacts: MutableList<String> = mutableListOf()
    private lateinit var startButton: MaterialButton
    private lateinit var stopButton: MaterialButton
    private var isServiceRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos_service)

        startButton = findViewById(R.id.start)
        stopButton = findViewById(R.id.stop)

        updateButtonStates()

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_profile).isChecked = true

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

        multiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            handlePermissionsResult(result)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MYID",
                "CHANNELFOREGROUND",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fetchTrustedContacts()
    }

    override fun onResume() {
        super.onResume()
        fetchTrustedContacts()
    }

    private fun fetchTrustedContacts() {
        val userId = auth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().reference

        databaseRef.child("users").child(userId).child("trusted_contacts")
            .get()
            .addOnSuccessListener { snapshot ->
                trustedContacts.clear()
                snapshot.children.mapNotNullTo(trustedContacts) { it.getValue(String::class.java) }
                updateTrustedContactsTextView()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch trusted contacts.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTrustedContactsTextView() {
        val textView = findViewById<TextView>(R.id.textNum)
        if (trustedContacts.isNotEmpty()) {
            textView.text = "SOS Will Be Sent To:\n${trustedContacts.joinToString("\n")}"
        } else {
            textView.text = "No trusted contacts set. Please register numbers."
        }
    }

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

    fun startService(view: View?) {
        if (checkPermissions()) {
            if (trustedContacts.isNotEmpty()) {
                for (contact in trustedContacts) {
                    sendSOSMessage(contact)
                }
                val intent = Intent(this, ServiceMine::class.java).apply { action = "START" }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, intent)
                    showSnackbar("SOS Messages Sent and Service Started!")
                } else {
                    startService(intent)
                }
                isServiceRunning = true
                updateButtonStates()
            } else {
                Toast.makeText(this, "No trusted contacts to send SOS.", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissions()
        }
    }

    fun stopService(view: View?) {
        val intent = Intent(this, ServiceMine::class.java).apply { action = "STOP" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopService(intent)
            showSnackbar("Service Stopped!")
        } else {
            stopService(intent)
        }
        isServiceRunning = false
        updateButtonStates()
    }

    private fun updateButtonStates() {
        if (isServiceRunning) {
            startButton.isEnabled = false
            startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            stopButton.isEnabled = true
            stopButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
        } else {
            startButton.isEnabled = true
            startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            stopButton.isEnabled = false
            stopButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun sendSOSMessage(contact: String) {
        val sosMessage = "Help! I am in danger. Please contact me immediately!"
        try {
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(contact, null, sosMessage, null, null)
            Toast.makeText(this, "SOS sent to $contact", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SOS to $contact", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun requestPermissions() {
        multiplePermissions.launch(
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = android.widget.PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.changeNumber -> {
                    startActivity(Intent(this, RegisterNumberActivity::class.java))
                    true
                }
                R.id.logOut -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}