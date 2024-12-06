package com.uddesh04.womenSafety

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TrustedContactsAdapter(
    private val context: Context,
    private val contacts: MutableList<String>,
    private val onDeleteContact: (String) -> Unit
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = contacts.size

    override fun getItem(position: Int): Any = contacts[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.item_trusted_contact, parent, false)

        val contactText = view.findViewById<TextView>(R.id.contactText)
        val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)

        val contact = contacts[position]
        contactText.text = contact

        deleteIcon.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete") { _, _ ->
                    onDeleteContact(contact)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return view
    }
}