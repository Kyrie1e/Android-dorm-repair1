package com.example.repairapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.repairapp.R
import com.example.repairapp.data.DBHelper
import com.example.repairapp.data.RepairOrder
import com.example.repairapp.data.dao.LogDao
import com.example.repairapp.data.dao.RepairDao

class RepairDetailActivity : AppCompatActivity() {

    private lateinit var repairDao: RepairDao
    private lateinit var logDao: LogDao

    private lateinit var titleText: TextView
    private lateinit var statusText: TextView
    private lateinit var handlerText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var claimButton: Button
    private lateinit var doneButton: Button
    private lateinit var hintText: TextView

    private var orderId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_detail)

        orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L)
        if (orderId == -1L) {
            finish()
            return
        }

        repairDao = RepairDao(DBHelper(this))
        logDao = LogDao(DBHelper(this))

        titleText = findViewById(R.id.text_title)
        statusText = findViewById(R.id.text_status)
        handlerText = findViewById(R.id.text_handler)
        descriptionText = findViewById(R.id.text_description)
        claimButton = findViewById(R.id.button_claim)
        doneButton = findViewById(R.id.button_done)
        hintText = findViewById(R.id.text_hint)

        claimButton.setOnClickListener { claimOrder() }
        doneButton.setOnClickListener { markDone() }
    }

    override fun onResume() {
        super.onResume()
        refreshOrder()
    }

    private fun refreshOrder() {
        val order = repairDao.getOrderById(orderId)
        if (order == null) {
            finish()
            return
        }
        bindOrder(order)
    }

    private fun bindOrder(order: RepairOrder) {
        titleText.text = "${order.type} - ${order.location}"
        statusText.text = statusLabel(order.status)
        handlerText.text = order.handlerName ?: "未接单"
        descriptionText.text = order.description

        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        val username = prefs.getString(LoginActivity.KEY_USERNAME, null)
        val role = prefs.getString(LoginActivity.KEY_ROLE, null)

        claimButton.visibility = View.GONE
        doneButton.visibility = View.GONE
        hintText.visibility = View.GONE

        if (role != "worker" || userId == -1L || username == null) {
            return
        }

        if (order.status == 0 && order.handlerId == null) {
            claimButton.visibility = View.VISIBLE
        } else if (order.handlerId == userId && order.status != 2) {
            doneButton.visibility = View.VISIBLE
        } else if (order.handlerId != null && order.handlerId != userId) {
            hintText.visibility = View.VISIBLE
            hintText.text = "已由${order.handlerName}接单"
        }
    }

    private fun claimOrder() {
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        val username = prefs.getString(LoginActivity.KEY_USERNAME, null)
        if (userId == -1L || username == null) {
            return
        }

        val success = repairDao.claimOrder(orderId, userId, username)
        if (success) {
            logDao.insertLog(orderId, userId, username, "claim", null)
            Toast.makeText(this, "接单成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "该工单已被接单", Toast.LENGTH_SHORT).show()
        }
        refreshOrder()
    }

    private fun markDone() {
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        val username = prefs.getString(LoginActivity.KEY_USERNAME, null)
        if (userId == -1L || username == null) {
            return
        }

        val success = repairDao.markDone(orderId, userId)
        if (success) {
            logDao.insertLog(orderId, userId, username, "update_status", "DONE")
            Toast.makeText(this, "已标记完成", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "非接单师傅不可操作", Toast.LENGTH_SHORT).show()
        }
        refreshOrder()
    }

    private fun statusLabel(status: Int): String {
        return when (status) {
            0 -> "待接单"
            1 -> "处理中"
            2 -> "已完成"
            else -> "未知"
        }
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
    }
}
