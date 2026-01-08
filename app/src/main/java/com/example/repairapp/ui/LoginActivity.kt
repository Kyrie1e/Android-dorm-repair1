package com.example.repairapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.repairapp.R
import com.example.repairapp.data.DBHelper
import com.example.repairapp.data.dao.UserDao

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.input_username)
        val passwordInput = findViewById<EditText>(R.id.input_password)
        val loginButton = findViewById<Button>(R.id.button_login)
        val registerButton = findViewById<Button>(R.id.button_register)

        val userDao = UserDao(DBHelper(this))

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = userDao.login(username, password)
            if (user == null) {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putLong(KEY_USER_ID, user.id)
                .putString(KEY_USERNAME, user.username)
                .putString(KEY_ROLE, user.role)
                .apply()

            startActivity(Intent(this, RepairListActivity::class.java))
            finish()
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    companion object {
        const val PREFS_NAME = "repair_prefs"
        const val KEY_USER_ID = "user_id"
        const val KEY_USERNAME = "username"
        const val KEY_ROLE = "role"
    }
}
