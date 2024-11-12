package com.uddesh04.womenSafety

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class RegisterNumberActivity : AppCompatActivity() {
    private var number: TextInputEditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_number)

        number = findViewById(R.id.numberEdit)
    }

    fun saveNumber(view: View?) {
        val numberString = number!!.text.toString()
        if (numberString.length == 10) {
            val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("ENUM", numberString)
            myEdit.apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the back stack
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Enter Valid Number!", Toast.LENGTH_SHORT).show()
        }
    }
}
