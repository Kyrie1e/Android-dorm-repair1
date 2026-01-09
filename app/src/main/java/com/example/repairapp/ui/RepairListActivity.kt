package com.example.repairapp.ui

import android.content.Intent
import android.os.Bundle
import android.content.IntentFilter
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.TextView
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
    private var isReceiverRegistered: Boolean = false

    private var currentUserId: Long = -1L
    private var currentUsername: String? = null
    private var currentRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_list)

        val userInfoText = findViewById<TextView>(R.id.text_user_info)
        val addButton = findViewById<Button>(R.id.button_add)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_orders)

        // 1) 读取登录态
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        currentUserId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        currentUsername = prefs.getString(LoginActivity.KEY_USERNAME, null)
        currentRole = prefs.getString(LoginActivity.KEY_ROLE, null)

        // 2) 没登录就回登录页
        if (currentUserId == -1L || currentUsername == null || currentRole == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 3) UI：用户信息 + 角色按钮可见性
        userInfoText.text = "${currentUsername} (${currentRole})"
        addButton.visibility = if (currentRole == "student") View.VISIBLE else View.GONE
        addButton.setOnClickListener {
            startActivity(Intent(this, SubmitRepairActivity::class.java))
        }

        // 4) 列表
        adapter = RepairOrderAdapter(mutableListOf()) { order ->
            val intent = Intent(this, RepairDetailActivity::class.java)
            intent.putExtra(RepairDetailActivity.EXTRA_ORDER_ID, order.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 5) DAO
        repairDao = RepairDao(DBHelper(this))

        // 6) Receiver（收到刷新广播就更新列表）
        refreshReceiver = RepairRefreshReceiver { refreshList() }
    }

    override fun onStart() {
        super.onStart()
        registerRefreshReceiverIfNeeded()
    }

    override fun onStop() {
        unregisterRefreshReceiverIfNeeded()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
        maybeStartWorkerService()
    }

    private fun registerRefreshReceiverIfNeeded() {
        if (isReceiverRegistered) return

        val filter = IntentFilter(WorkerPollingService.ACTION_REPAIR_REFRESH)

        // ✅ Android 13+ 必须指定 RECEIVER_EXPORTED / RECEIVER_NOT_EXPORTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(refreshReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(refreshReceiver, filter)
        }

        isReceiverRegistered = true
    }

    private fun unregisterRefreshReceiverIfNeeded() {
        if (!isReceiverRegistered) return
        try {
            unregisterReceiver(refreshReceiver)
        } catch (_: IllegalArgumentException) {
            // 防止某些异常情况下重复注销导致崩溃
        } finally {
            isReceiverRegistered = false
        }
    }

    private fun refreshList() {
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        val role = prefs.getString(LoginActivity.KEY_ROLE, null)

        if (userId == -1L || role.isNullOrBlank()) return

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

        // 只有师傅启动轮询服务
        if (role == "worker") {
            // Android 8+ 后台限制：为了简单可先用 startService，
            // 若后续遇到限制再改成 startForegroundService + 通知
            startService(Intent(this, WorkerPollingService::class.java))
        }
    }
}
