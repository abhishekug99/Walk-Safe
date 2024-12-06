package com.uddesh04.womenSafety

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class DrawerItem(val icon: Int, val title: String, val action: () -> Unit)

class DrawerAdapter(private val items: List<DrawerItem>) : RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder>() {

    inner class DrawerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.drawerIcon)
        val title: TextView = view.findViewById(R.id.drawerTitle)

        fun bind(item: DrawerItem) {
            icon.setImageResource(item.icon)
            title.text = item.title
            itemView.setOnClickListener { item.action() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.drawer_item, parent, false)
        return DrawerViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrawerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
