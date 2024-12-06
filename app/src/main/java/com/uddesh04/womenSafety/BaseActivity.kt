package com.uddesh04.womenSafety

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    protected fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeScreenActivity::class.java))
                    true
                }
                R.id.nav_contacts -> {
                    startActivity(Intent(this, RegisterNumberActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, MyProfileActivity::class.java))
                    true
                }
                R.id.nav_live_location -> {
                    startActivity(Intent(this, LiveLocationActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
