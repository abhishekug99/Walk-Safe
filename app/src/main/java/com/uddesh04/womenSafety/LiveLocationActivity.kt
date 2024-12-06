package com.uddesh04.womenSafety

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView


class LiveLocationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_location)

        // Set up Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_live_location).isChecked = true
    }
}