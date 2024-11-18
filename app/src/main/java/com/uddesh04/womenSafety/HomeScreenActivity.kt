package com.uddesh04.womenSafety

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationDrawer)

        findViewById<ImageView>(R.id.btnHamburger).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        // Fetch user details for the navigation drawer



//        val headerView = navigationView.getHeaderView(0)
//        val headerUserName = headerView.findViewById<TextView>(R.id.headerUserName)
//        val headerProfileIcon = headerView.findViewById<ImageView>(R.id.headerProfileIcon)
//        val currentUserSnapshot = auth.currentUser
//
//        // Set user details
//        if (currentUserSnapshot != null) {
//            headerUserName.text = currentUserSnapshot.displayName ?: "Unknown User"
//            // Optionally set a profile picture if available
//            currentUserSnapshot.photoUrl?.let { photoUrl ->
//                Glide.with(this).load(photoUrl).into(headerProfileIcon)
//            }
//        }

        setupNavigationHeader()
        // Initialize Navigation Drawer
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, MyProfileActivity::class.java))
                }
                R.id.nav_logout -> {
                    logoutUser()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        // Navigate to SOS Service Activity
        findViewById<LinearLayout>(R.id.startSOSServiceTile).setOnClickListener {
            startActivity(Intent(this, SOSServiceActivity::class.java))
        }

        // "Walk Safe" tile - No action currently specified, keep it for aesthetic purposes
        findViewById<LinearLayout>(R.id.futureTile).setOnClickListener {
            // Optional: Add action if required
        }


//        val profileButton = findViewById<ImageView>(R.id.btnProfile)
//        profileButton.setOnClickListener {
//            startActivity(Intent(this, MyProfileActivity::class.java))
//        }

    }

    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val headerUserName = headerView.findViewById<TextView>(R.id.headerUserName)
        val headerProfileIcon = headerView.findViewById<ImageView>(R.id.headerProfileIcon)

        // Check if the current user has a displayName or photoUrl (Google sign-in)
        if (currentUser != null) {
            if (!currentUser?.displayName.isNullOrEmpty() || currentUser?.photoUrl != null) {
                headerUserName.text = currentUser?.displayName ?: "Unknown User"
                currentUser?.photoUrl?.let { photoUrl ->
                    Glide.with(this).load(photoUrl).placeholder(R.drawable.default_profile_picture).into(headerProfileIcon)
                }
            } else {
                // Fetch user details from Realtime Database for email/password users
                val userId = currentUser?.uid
                val database = FirebaseDatabase.getInstance().reference.child("users").child(userId!!)
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown User"
                            val photoUrl = snapshot.child("photo_url").getValue(String::class.java)

                            headerUserName.text = name
                            if (!photoUrl.isNullOrEmpty()) {
                                Glide.with(this@HomeScreenActivity).load(photoUrl)
                                    .placeholder(R.drawable.default_profile_picture).into(headerProfileIcon)
                            } else {
                                headerProfileIcon.setImageResource(R.drawable.default_profile_picture)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@HomeScreenActivity, "Failed to load user details: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } else {
            headerUserName.text = "Unknown User"
            headerProfileIcon.setImageResource(R.drawable.default_profile_picture)
        }
    }

    private fun setupNavigationDrawer() {
        val drawerItems = listOf(
            DrawerItem(
                icon = R.drawable.ic_profile,
                title = currentUser?.displayName ?: "Unknown User",
                action = {
                    startActivity(Intent(this, MyProfileActivity::class.java))
                    drawerLayout.closeDrawers()
                }

            ),
            DrawerItem(
                icon = R.drawable.ic_logout,
                title = "Logout",
                action = {
                    logoutUser()
                    drawerLayout.closeDrawers()
                }
            )
        )
        val recyclerView = findViewById<RecyclerView>(R.id.navigationDrawer)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DrawerAdapter(drawerItems)
    }

    private fun logoutUser() {
        // Log out from Firebase
        auth.signOut()

        // Log out from Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        // Redirect to Login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}