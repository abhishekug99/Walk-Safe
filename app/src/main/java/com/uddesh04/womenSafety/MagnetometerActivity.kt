package com.uddesh04.womenSafety

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MagnetometerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magnetometer)
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_profile).isChecked = true

        // Optional: Set up any UI logic for the entry screen
    }
}