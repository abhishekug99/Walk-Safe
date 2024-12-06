package com.uddesh04.womenSafety

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

class SelfDefenceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_defence)

        // Set up Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)
    }
}