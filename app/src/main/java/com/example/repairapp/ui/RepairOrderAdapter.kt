package com.example.repairapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.repairapp.R
import com.example.repairapp.data.RepairOrder

class RepairOrderAdapter(
    private val items: MutableList<RepairOrder>,
    private val onItemClick: (RepairOrder) -> Unit
) : RecyclerView.Adapter<RepairOrderAdapter.RepairOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepairOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repair_order, parent, false)
        return RepairOrderViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: RepairOrderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<RepairOrder>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class RepairOrderViewHolder(
        itemView: View,
        private val onItemClick: (RepairOrder) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.text_title)
        private val statusText: TextView = itemView.findViewById(R.id.text_status)
        private val handlerText: TextView = itemView.findViewById(R.id.text_handler)

        fun bind(order: RepairOrder) {
            titleText.text = "${order.type} - ${order.location}"
            statusText.text = statusLabel(order.status)
            handlerText.text = order.handlerName ?: "未接单"
            itemView.setOnClickListener { onItemClick(order) }
        }

        private fun statusLabel(status: Int): String {
            return when (status) {
                0 -> "待接单"
                1 -> "处理中"
                2 -> "已完成"
                else -> "未知"
            }
        }
    }
}
