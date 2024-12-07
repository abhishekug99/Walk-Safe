package com.uddesh04.womenSafety.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uddesh04.womenSafety.R
import com.uddesh04.womenSafety.SOSRecord

class HistoryAdapter(private val records: List<SOSRecord>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val startTimeTextView: TextView = view.findViewById(R.id.textStartTime)
        val stopTimeTextView: TextView = view.findViewById(R.id.textStopTime)
        val durationTextView: TextView = view.findViewById(R.id.textDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sos_record, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val record = records[position]
        holder.startTimeTextView.text = "Start Time: ${record.startTime}"
        holder.stopTimeTextView.text = "Stop Time: ${record.stopTime}"
        holder.durationTextView.text = "Duration: ${record.duration} minutes"
    }

    override fun getItemCount(): Int = records.size
}
