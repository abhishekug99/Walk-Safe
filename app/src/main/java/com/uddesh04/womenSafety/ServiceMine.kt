package com.uddesh04.womenSafety

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.github.tbouron.shakedetector.library.ShakeDetector
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class ServiceMine : Service() {
    private var isRunning: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var myLocation: String = "Location Unavailable"
    override fun onBind(intent: Intent): IBinder? = null
    private var trustedContacts: MutableList<String> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()

        ShakeDetector.create(this) {
            sendSOS()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            myLocation = location?.let {
                "http://maps.google.com/maps?q=loc:${it.latitude},${it.longitude}"
            } ?: "Location Unavailable"
        }
    }

    private fun sendSOS() {
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val trustedNumber = sharedPreferences.getString("ENUM", "NONE")
        if (trustedNumber != "NONE") {
            SmsManager.getDefault().sendTextMessage(
                trustedNumber, null,
                "I'm in trouble! Here's my location:\n$myLocation",
                null, null
            )
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action.equals("STOP", true)) {
            stopForeground(true)
            stopSelf()
        } else {

            startForegroundServiceWithNotification()
        }
        return START_NOT_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notificationIntent = Intent(this, SOSServiceActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "MYID"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Foreground Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Women Safety")
            .setContentText("Shake device to send SOS")
            .setSmallIcon(R.drawable.girl_vector)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        ShakeDetector.stop()
    }
}

//class ServiceMine : Service() {
//    private var isRunning: Boolean = false
//    private var fusedLocationClient: FusedLocationProviderClient? = null
//    private var manager: SmsManager = SmsManager.getDefault()
//    private var myLocation: String? = null
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//
//
//    override fun onCreate() {
//        super.onCreate()
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
//                if (location != null) {
//                    // Logic to handle location object
//                    location.altitude
//                    location.longitude
//                    myLocation =
//                        "http://maps.google.com/maps?q=loc:" + location.latitude + "," + location.longitude
//                } else {
//                    myLocation = "Unable to Find Location :("
//                }
//            }
//
//
//        ShakeDetector.create(this) {
//            //if you want to play siren sound you can do it here
//            //just create music player and play here
//            //before playing sound please set volume to max
//            val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
//            val ENUM = sharedPreferences.getString("ENUM", "NONE")
//            if (!ENUM.equals("NONE", ignoreCase = true)) {
//                manager.sendTextMessage(
//                    ENUM,
//                    null,
//                    "Im in Trouble!\nSending My Location :\n$myLocation",
//                    null,
//                    null
//                )
//            }
//        }
//    }
//
//    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        if (intent.action.equals("STOP", ignoreCase = true)) {
//            if (isRunning) {
//                this.stopForeground(true)
//                this.stopSelf()
//            }
//        } else {
//            val notificationIntent = Intent(this, MainActivity::class.java)
//            val pendingIntent = PendingIntent.getActivity(
//                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(
//                    "MYID",
//                    "CHANNELFOREGROUND",
//                    NotificationManager.IMPORTANCE_DEFAULT
//                )
//
//                val m = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//                m.createNotificationChannel(channel)
//
//                val notification = Notification.Builder(this, "MYID")
//                    .setContentTitle("Women Safety")
//                    .setContentText("Shake Device to Send SOS")
//                    .setSmallIcon(R.drawable.girl_vector)
//                    .setContentIntent(pendingIntent)
//                    .build()
//                this.startForeground(115, notification)
//                isRunning = true
//                return START_NOT_STICKY
//            }
//        }
//
//        return super.onStartCommand(intent, flags, startId)
//    }
//
//    private fun hasLocationPermissions(): Boolean {
//        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//    }
//}
