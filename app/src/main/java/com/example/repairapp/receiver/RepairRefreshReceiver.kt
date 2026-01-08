package com.example.repairapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RepairRefreshReceiver(private val onRefresh: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        onRefresh()
    }
}
