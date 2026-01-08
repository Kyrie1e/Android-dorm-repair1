package com.example.repairapp.data.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.repairapp.data.DBHelper
import com.example.repairapp.data.RepairLog

class LogDao(private val dbHelper: DBHelper) {

    fun insertLog(
        orderId: Long,
        operatorId: Long,
        operatorName: String,
        action: String,
        note: String?
    ): Long {
        val values = ContentValues().apply {
            put("order_id", orderId)
            put("operator_id", operatorId)
            put("operator_name", operatorName)
            put("action", action)
            if (note == null) {
                putNull("note")
            } else {
                put("note", note)
            }
            put("time", System.currentTimeMillis())
        }
        val db = dbHelper.writableDatabase
        return db.insert(DBHelper.TABLE_REPAIR_LOG, null, values)
    }

    fun queryLogs(orderId: Long): List<RepairLog> {
        val logs = mutableListOf<RepairLog>()
        val db = dbHelper.readableDatabase
        val selection = "order_id = ?"
        val args = arrayOf(orderId.toString())
        db.query(
            DBHelper.TABLE_REPAIR_LOG,
            null,
            selection,
            args,
            null,
            null,
            "time DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                logs.add(logFromCursor(cursor))
            }
        }
        return logs
    }

    private fun logFromCursor(cursor: Cursor): RepairLog {
        val noteIndex = cursor.getColumnIndexOrThrow("note")
        return RepairLog(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
            orderId = cursor.getLong(cursor.getColumnIndexOrThrow("order_id")),
            operatorId = cursor.getLong(cursor.getColumnIndexOrThrow("operator_id")),
            operatorName = cursor.getString(cursor.getColumnIndexOrThrow("operator_name")),
            action = cursor.getString(cursor.getColumnIndexOrThrow("action")),
            note = if (cursor.isNull(noteIndex)) null else cursor.getString(noteIndex),
            time = cursor.getLong(cursor.getColumnIndexOrThrow("time"))
        )
    }
}
