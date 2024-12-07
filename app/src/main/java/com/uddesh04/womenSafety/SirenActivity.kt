package com.uddesh04.womenSafety

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SirenActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var countdownTimer: CountDownTimer? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show countdown dialog immediately
        showCountdownDialog()
    }

    private fun showCountdownDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_siren_countdown, null)

        val countdownText = dialogView.findViewById<TextView>(R.id.textCountdown)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancelCountdown)

        alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        countdownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = "Siren will start in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                alertDialog?.dismiss()
                fetchAndPlaySiren()
                countdownTimer = null
            }
        }

        cancelButton.setOnClickListener {
            countdownTimer?.cancel()
            countdownTimer = null
            alertDialog?.dismiss()
            finish()  // Close the transparent activity
            Toast.makeText(this, "Siren cancelled.", Toast.LENGTH_SHORT).show()
        }

        alertDialog?.show()
        countdownTimer?.start()
    }

    private fun fetchAndPlaySiren() {
        val storageRef = Firebase.storage.reference.child("siren/siren_sound.mp3")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            playSiren(uri.toString())
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load siren sound.", Toast.LENGTH_SHORT).show()
            finish()  // Close the transparent activity if the siren fails to load
        }
    }

    private fun playSiren(url: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepare()
                start()
            }
            showStopSirenDialog()
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing siren sound.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showStopSirenDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Siren is Active")
        builder.setMessage("Siren is playing. Do you want to stop it?")
        builder.setCancelable(false)
        builder.setPositiveButton("Stop Siren") { dialog, _ ->
            stopSiren()
            dialog.dismiss()
            finish()  // Close the transparent activity
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun stopSiren() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        Toast.makeText(this, "Siren stopped.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSiren()
    }
}