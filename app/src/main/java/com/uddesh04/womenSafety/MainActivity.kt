package com.uddesh04.womenSafety

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    // Declare ActivityResultLauncher for requesting permissions
    private lateinit var multiplePermissions: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the permissions launcher
        multiplePermissions = registerForActivityResult(RequestMultiplePermissions()) { result ->
            handlePermissionsResult(result)
        }

        // Set up notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MYID",
                "CHANNELFOREGROUND",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val m = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            m.createNotificationChannel(channel)
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val eNUM = sharedPreferences.getString("ENUM", "NONE")
        if (eNUM.equals("NONE", ignoreCase = true)) {
            startActivity(Intent(this, RegisterNumberActivity::class.java))
        } else {
            val textView = findViewById<TextView>(R.id.textNum)
            textView.text = "SOS Will Be Sent To\n$eNUM"
        }
    }

    // Function to handle the result of the permissions request
    private fun handlePermissionsResult(result: Map<String, Boolean>) {
        for ((key, value) in result) {
            if (!value) {
                val snackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    "Permission Must Be Granted!",
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setAction("Grant Permission") {
                    multiplePermissions.launch(arrayOf(key))
                    snackbar.dismiss()
                }
                snackbar.show()
            }
        }
    }

    // Function to stop the service
    fun stopService(view: View?) {
        val notificationIntent = Intent(this, ServiceMine::class.java)
        notificationIntent.action = "stop"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(notificationIntent)
            Snackbar.make(
                findViewById(android.R.id.content),
                "Service Stopped!",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    // Function to start the service with permission checks
    fun startServiceV(view: View?) {
        if (ContextCompat.checkSelfPermission(
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
        ) {
            val notificationIntent = Intent(this, ServiceMine::class.java)
            notificationIntent.action = "Start"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(notificationIntent)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Service Started!",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } else {
            multiplePermissions.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    // Function to show popup menu for additional options
    fun PopupMenu(view: View?) {
        val popupMenu = androidx.appcompat.widget.PopupMenu(this@MainActivity, view!!)
        popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.changeNum) {
                startActivity(Intent(this@MainActivity, RegisterNumberActivity::class.java))
            }
            true
        }
        popupMenu.show()
    }
}








//package com.uddesh04.womenSafety
//
//import android.Manifest
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.view.View
//import android.widget.TextView
//import androidx.activity.result.ActivityResultCallback
//import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import com.google.android.material.snackbar.Snackbar
//
//class MainActivity : AppCompatActivity() {
//    override fun onResume() {
//        super.onResume()
//        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
//        val eNUM = sharedPreferences.getString("ENUM", "NONE")
//        if (eNUM.equals("NONE", ignoreCase = true)) {
//            startActivity(Intent(this, RegisterNumberActivity::class.java))
//        } else {
//            val textView = findViewById<TextView>(R.id.textNum)
//            textView.text = "SOS Will Be Sent To\n$eNUM"
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(
//                    "MYID",
//                    "CHANNELFOREGROUND",
//                    NotificationManager.IMPORTANCE_DEFAULT
//                )
//
//                val m = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//                m.createNotificationChannel(channel)
//            }
//        }
//    }
//
//
//    private val multiplePermissions = registerForActivityResult(
//        RequestMultiplePermissions()
//    ) { result: Map<String, Boolean> ->
//        for ((key, value) in result) {
//            if (!value) {
//                val snackbar = Snackbar.make(
//                    findViewById(android.R.id.content),
//                    "Permission Must Be Granted!",
//                    Snackbar.LENGTH_INDEFINITE
//                )
//                snackbar.setAction("Grant Permission") {
//                    multiplePermissions.launch(arrayOf(key))
//                    snackbar.dismiss()
//                }
//                snackbar.show()
//            }
//        }
//    }
//
//
//
//    fun stopService(view: View?) {
//        val notificationIntent = Intent(this, ServiceMine::class.java)
//        notificationIntent.setAction("stop")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            applicationContext.startForegroundService(notificationIntent)
//            Snackbar.make(
//                findViewById(android.R.id.content),
//                "Service Stopped!",
//                Snackbar.LENGTH_LONG
//            ).show()
//        }
//    }
//
//    fun startServiceV(view: View?) {
//        if ((ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.SEND_SMS
//            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            val notificationIntent = Intent(this, ServiceMine::class.java)
//            notificationIntent.setAction("Start")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                applicationContext.startForegroundService(notificationIntent)
//                Snackbar.make(
//                    findViewById(android.R.id.content),
//                    "Service Started!",
//                    Snackbar.LENGTH_LONG
//                ).show()
//            }
//        } else {
//            multiplePermissions.launch(
//                arrayOf(
//                    Manifest.permission.SEND_SMS,
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                )
//            )
//        }
//    }
//
//    fun PopupMenu(view: View?) {
//        val popupMenu = androidx.appcompat.widget.PopupMenu(this@MainActivity, view!!)
//        popupMenu.menuInflater.inflate(R.menu.popup, popupMenu.menu)
//        popupMenu.setOnMenuItemClickListener { item ->
//            if (item.itemId == R.id.changeNum) {
//                startActivity(Intent(this@MainActivity, RegisterNumberActivity::class.java))
//            }
//            true
//        }
//        popupMenu.show()
//    }
//}