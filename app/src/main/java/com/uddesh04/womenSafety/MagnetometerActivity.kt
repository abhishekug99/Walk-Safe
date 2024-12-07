package com.uddesh04.womenSafety

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.sqrt

class MagnetometerActivity : BaseActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private lateinit var speedometerView: SpeedometerView
    private lateinit var magneticFieldTextView: TextView

    // Baseline calibration variables
    private var baselineMagneticField: Float = 0f
    private var calibrated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magnetometer)

        // Initialize UI components
        speedometerView = findViewById(R.id.speedometer)
        magneticFieldTextView = findViewById(R.id.value)

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_home).isChecked = true

        // Initialize sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val helpButton = findViewById<FloatingActionButton>(R.id.magnetoInst)
        helpButton.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("How the Hidden Camera Detection Works")
            .setMessage(
                "This tool helps you find hidden cameras using your phone.\n\n" +
                        "Steps to use this feature:\n" +
                        "1. Walk around the room with your phone.\n" +
                        "2. Hold your phone close to suspicious objects like mirrors, clocks, or decorations.\n" +
                        "3. The app will automatically scan the area as you move.\n" +
                        "4. If a hidden camera is detected, you'll hear an alert sound.\n\n" +
                        "Use this tool in places like changing rooms, hotel rooms, or restrooms to stay safe."
            )
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Update the TextViews with the new values
            findViewById<TextView>(R.id.x_cor).text = String.format("%.1f μT", x)
            findViewById<TextView>(R.id.y_cor).text = String.format("%.1f μT", y)
            findViewById<TextView>(R.id.z_cor).text = String.format("%.1f μT", z)

            val magneticFieldStrength = sqrt(
                (event.values[0] * event.values[0] +
                        event.values[1] * event.values[1] +
                        event.values[2] * event.values[2]).toDouble()
            ).toFloat()

            // Baseline calibration
            if (!calibrated) {
                baselineMagneticField = magneticFieldStrength
                calibrated = true
                Toast.makeText(this, "Baseline calibrated", Toast.LENGTH_SHORT).show()
            }

            // Calculate the anomaly from baseline
            val anomaly = magneticFieldStrength - baselineMagneticField

            // Update the speedometer view with the magnetic field strength
            speedometerView.setValue(magneticFieldStrength)

            magneticFieldTextView.text = String.format("%.1f μT", magneticFieldStrength)

            // Detect anomaly and provide feedback
            if (anomaly > 50) { // Threshold for detecting a significant anomaly
                Toast.makeText(this, "Potential hidden camera detected!", Toast.LENGTH_SHORT).show()
                playAlertSound()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    // Function to play an alert sound when anomaly is detected
    private fun playAlertSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { it.release() }
    }
}