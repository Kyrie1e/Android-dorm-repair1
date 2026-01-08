package com.example.repairapp.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.repairapp.data.DBHelper
import com.example.repairapp.data.User

class UserDao(private val dbHelper: DBHelper) {

    fun registerUser(username: String, password: String, role: String): Long {
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("role", role)
            put("create_time", System.currentTimeMillis())
        }
        val db = dbHelper.writableDatabase
        return db.insert(DBHelper.TABLE_USER, null, values)
    }

    fun login(username: String, password: String): User? {
        val db = dbHelper.readableDatabase
        val selection = "username = ? AND password = ?"
        val args = arrayOf(username, password)
        db.query(
            DBHelper.TABLE_USER,
            null,
            selection,
            args,
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                userFromCursor(cursor)
            } else {
                null
            }
        }
    }

    private fun userFromCursor(cursor: Cursor): User {
        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
            username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
            password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
            role = cursor.getString(cursor.getColumnIndexOrThrow("role")),
            createTime = cursor.getLong(cursor.getColumnIndexOrThrow("create_time"))
        )
    }
}
