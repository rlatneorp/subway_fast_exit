package com.rlatneorp.fast_subway_exit.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rlatneorp.fast_subway_exit.R
import com.rlatneorp.fast_subway_exit.model.ElevatorRow

private fun getStatusColor(status: String): Int {
    if (status.contains("불가") || status.contains("수리") || status.contains("점검")) {
        return Color.RED
    }
    if (status.contains("가능")) {
        return Color.parseColor("#4CAF50")
    }
    return Color.BLACK
}

class ElevatorAdapter : ListAdapter<ElevatorRow, ElevatorAdapter.ElevatorViewHolder>(ElevatorDiffCallback()) {

    class ElevatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val facilityName: TextView = itemView.findViewById(R.id.tvFacilityName)
        private val location: TextView = itemView.findViewById(R.id.tvLocation)
        private val runStatus: TextView = itemView.findViewById(R.id.tvRunStatus)

        fun bind(item: ElevatorRow) {
            facilityName.text = "승강기명: ${item.facilityName}"
            location.text = "설치위치: ${item.location}"
            runStatus.text = "운행상태: ${item.runStatus}"

            runStatus.setTextColor(getStatusColor(item.runStatus))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElevatorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_elevator, parent, false)
        return ElevatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ElevatorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ElevatorDiffCallback : DiffUtil.ItemCallback<ElevatorRow>() {
    override fun areItemsTheSame(oldItem: ElevatorRow, newItem: ElevatorRow): Boolean {
        return oldItem.facilityName == newItem.facilityName && oldItem.location == newItem.location
    }

    override fun areContentsTheSame(oldItem: ElevatorRow, newItem: ElevatorRow): Boolean {
        return oldItem == newItem
    }
}