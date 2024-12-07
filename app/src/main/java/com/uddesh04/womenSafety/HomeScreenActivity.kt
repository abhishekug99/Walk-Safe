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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeScreenActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationDrawer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        findViewById<ImageView>(R.id.btnHamburger).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        setupNavigationHeader()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)

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


        findViewById<LinearLayout>(R.id.startSOSServiceTile).setOnClickListener {
            startActivity(Intent(this, SOSServiceActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cameraDetectorTile).setOnClickListener {
            startActivity(Intent(this, MagnetometerActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.liveLocationTile).setOnClickListener {
            startActivity(Intent(this, LiveLocationActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.selfDefenceTile).setOnClickListener {
            startActivity(Intent(this, SelfDefenceActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.historyTile).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.sirenTile).setOnClickListener {
            startActivity(Intent(this, SirenActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_home).isChecked = true
    }

    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val headerUserName = headerView.findViewById<TextView>(R.id.headerUserName)
        val headerProfileIcon = headerView.findViewById<ImageView>(R.id.headerProfileIcon)

        if (currentUser != null) {
            if (!currentUser?.displayName.isNullOrEmpty() || currentUser?.photoUrl != null) {
                headerUserName.text = currentUser?.displayName ?: "Unknown User"
                currentUser?.photoUrl?.let { photoUrl ->
                    Glide.with(this).load(photoUrl).placeholder(R.drawable.default_profile_picture).into(headerProfileIcon)
                }
            } else {
                val userId = currentUser?.uid
                val database = FirebaseDatabase.getInstance().reference.child("users").child(userId!!)
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown User"
                            val photoUrl = snapshot.child("profile_image").getValue(String::class.java)

                            headerUserName.text = name
                            if (!photoUrl.isNullOrEmpty()) {
                                // Load the latest stored image from Firebase Storage
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
        auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}