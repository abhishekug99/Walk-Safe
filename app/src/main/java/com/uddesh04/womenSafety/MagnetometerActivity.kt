package com.uddesh04.womenSafety

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import kotlin.math.sqrt

class MagnetometerActivity : BaseActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private lateinit var speedometerView: SpeedometerView

    // Baseline calibration variables
    private var baselineMagneticField: Float = 0f
    private var calibrated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magnetometer)

        // Initialize UI components
        speedometerView = findViewById(R.id.speedometer)
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_profile).isChecked = true

        // Initialize sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
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