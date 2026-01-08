package com.example.repairapp.ui

import android.os.Bundle
import android.content.ContentValues
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.repairapp.R
import com.example.repairapp.data.DBHelper
import com.example.repairapp.data.dao.LogDao
import com.example.repairapp.provider.RepairProvider

class SubmitRepairActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_repair)

        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        val username = prefs.getString(LoginActivity.KEY_USERNAME, null)
        val role = prefs.getString(LoginActivity.KEY_ROLE, null)

        if (userId == -1L || username == null || role != "student") {
            finish()
            return
        }

        val typeInput = findViewById<EditText>(R.id.input_type)
        val locationInput = findViewById<EditText>(R.id.input_location)
        val levelGroup = findViewById<RadioGroup>(R.id.radio_group_level)
        val descriptionInput = findViewById<EditText>(R.id.input_description)
        val submitButton = findViewById<Button>(R.id.button_submit)

        val logDao = LogDao(DBHelper(this))

        submitButton.setOnClickListener {
            val type = typeInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val level = when (levelGroup.checkedRadioButtonId) {
                R.id.radio_level_low -> 1
                R.id.radio_level_medium -> 2
                R.id.radio_level_high -> 3
                else -> 0
            }

            if (type.isEmpty() || location.isEmpty() || description.isEmpty() || level == 0) {
                Toast.makeText(this, "请完整填写报修信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                put("student_id", userId)
                put("student_name", username)
                put("type", type)
                put("location", location)
                put("level", level)
                put("description", description)
                put("status", 0)
                putNull("handler_id")
                putNull("handler_name")
                putNull("claim_time")
                put("create_time", now)
                put("update_time", now)
            }
            val uri = contentResolver.insert(RepairProvider.CONTENT_URI, values)
            val orderId = uri?.lastPathSegment?.toLongOrNull() ?: -1L
            if (orderId == -1L) {
                Toast.makeText(this, "提交失败", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            logDao.insertLog(orderId, userId, username, "create", null)
            Toast.makeText(this, "报修提交成功", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
