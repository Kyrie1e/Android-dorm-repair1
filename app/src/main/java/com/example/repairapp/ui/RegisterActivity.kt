package com.example.repairapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.repairapp.R
import com.example.repairapp.data.DBHelper
import com.example.repairapp.data.dao.UserDao

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameInput = findViewById<EditText>(R.id.input_username)
        val passwordInput = findViewById<EditText>(R.id.input_password)
        val confirmInput = findViewById<EditText>(R.id.input_confirm_password)
        val roleGroup = findViewById<RadioGroup>(R.id.radio_group_role)
        val registerButton = findViewById<Button>(R.id.button_register)

        val userDao = UserDao(DBHelper(this))

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirm = confirmInput.text.toString().trim()
            val role = when (roleGroup.checkedRadioButtonId) {
                R.id.radio_student -> "student"
                R.id.radio_worker -> "worker"
                else -> ""
            }

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty() || role.isEmpty()) {
                Toast.makeText(this, "请完整填写信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rowId = userDao.registerUser(username, password, role)
            if (rowId == -1L) {
                Toast.makeText(this, "注册失败，用户名可能已存在", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
