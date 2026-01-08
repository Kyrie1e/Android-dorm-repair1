package com.example.repairapp.ui

import android.content.Intent
import android.os.Bundle
import android.content.IntentFilter
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.repairapp.R
import com.example.repairapp.data.DBHelper
import com.example.repairapp.data.dao.RepairDao
import com.example.repairapp.receiver.RepairRefreshReceiver
import com.example.repairapp.service.WorkerPollingService

class RepairListActivity : AppCompatActivity() {

    private lateinit var adapter: RepairOrderAdapter
    private lateinit var repairDao: RepairDao
    private lateinit var refreshReceiver: RepairRefreshReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_list)

        val userInfoText = findViewById<TextView>(R.id.text_user_info)
        val addButton = findViewById<Button>(R.id.button_add)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_orders)

        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        val username = prefs.getString(LoginActivity.KEY_USERNAME, null)
        val role = prefs.getString(LoginActivity.KEY_ROLE, null)

        if (userId == -1L || username == null || role == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        userInfoText.text = "$username ($role)"

        addButton.setOnClickListener {
            startActivity(Intent(this, SubmitRepairActivity::class.java))
        }

        addButton.visibility = if (role == "student") View.VISIBLE else View.GONE

        adapter = RepairOrderAdapter(mutableListOf()) { order ->
            val intent = Intent(this, RepairDetailActivity::class.java)
            intent.putExtra(RepairDetailActivity.EXTRA_ORDER_ID, order.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        repairDao = RepairDao(DBHelper(this))

        refreshReceiver = RepairRefreshReceiver { refreshList() }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(WorkerPollingService.ACTION_REPAIR_REFRESH)
        registerReceiver(refreshReceiver, filter)
    }

    override fun onStop() {
        unregisterReceiver(refreshReceiver)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
        maybeStartWorkerService()
    }

    private fun refreshList() {
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        val role = prefs.getString(LoginActivity.KEY_ROLE, null)
        if (userId == -1L || role == null) {
            return
        }

        val orders = if (role == "student") {
            repairDao.queryOrdersForStudent(userId)
        } else {
            repairDao.queryOrdersForWorker()
        }
        adapter.updateData(orders)
    }

    private fun maybeStartWorkerService() {
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        val role = prefs.getString(LoginActivity.KEY_ROLE, null)
        if (role == "worker") {
            startService(Intent(this, WorkerPollingService::class.java))
        }
    }
}
