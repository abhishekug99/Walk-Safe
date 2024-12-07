package com.uddesh04.womenSafety

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView

class SelfDefenceActivity : BaseActivity() {

    private lateinit var searchButton: Button
    private lateinit var searchInput: EditText
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_defence)

        // Set up Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupBottomNavigation(bottomNavigationView)

        // UI Elements
        searchButton = findViewById(R.id.searchButton)
        searchInput = findViewById(R.id.searchInput)
        webView = findViewById(R.id.webView)

        // WebView Settings
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        searchButton.setOnClickListener {
            val query = searchInput.text.toString()
            if (query.isNotEmpty()) {
                searchYouTube(query)
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchYouTube(query: String) {
        // Construct YouTube search URL
        val searchUrl = "https://www.youtube.com/results?search_query=${query.replace(" ", "+")}"
        webView.loadUrl(searchUrl)
    }
}