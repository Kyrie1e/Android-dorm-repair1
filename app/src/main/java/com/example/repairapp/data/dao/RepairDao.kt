package com.example.repairapp.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.repairapp.data.DBHelper
import com.example.repairapp.data.RepairOrder

class RepairDao(private val dbHelper: DBHelper) {

    fun createOrder(
        studentId: Long,
        studentName: String,
        type: String,
        location: String,
        level: Int,
        description: String
    ): Long {
        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put("student_id", studentId)
            put("student_name", studentName)
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
        val db = dbHelper.writableDatabase
        return db.insert(DBHelper.TABLE_REPAIR_ORDER, null, values)
    }

    fun queryOrdersForStudent(studentId: Long): List<RepairOrder> {
        val db = dbHelper.readableDatabase
        val selection = "student_id = ?"
        val args = arrayOf(studentId.toString())
        return queryOrders(db, selection, args)
    }

    fun queryOrdersForWorker(): List<RepairOrder> {
        val db = dbHelper.readableDatabase
        return queryOrders(db, null, null)
    }

    fun getOrderById(orderId: Long): RepairOrder? {
        val db = dbHelper.readableDatabase
        val selection = "_id = ?"
        val args = arrayOf(orderId.toString())
        db.query(
            DBHelper.TABLE_REPAIR_ORDER,
            null,
            selection,
            args,
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                orderFromCursor(cursor)
            } else {
                null
            }
        }
    }

    fun claimOrder(orderId: Long, workerId: Long, workerName: String): Boolean {
        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put("handler_id", workerId)
            put("handler_name", workerName)
            put("status", 1)
            put("claim_time", now)
            put("update_time", now)
        }
        val db = dbHelper.writableDatabase
        val whereClause = "_id = ? AND status = 0 AND handler_id IS NULL"
        val args = arrayOf(orderId.toString())
        val rowsAffected = db.update(DBHelper.TABLE_REPAIR_ORDER, values, whereClause, args)
        return rowsAffected == 1
    }

    fun markDone(orderId: Long, workerId: Long): Boolean {
        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put("status", 2)
            put("update_time", now)
        }
        val db = dbHelper.writableDatabase
        val whereClause = "_id = ? AND handler_id = ?"
        val args = arrayOf(orderId.toString(), workerId.toString())
        val rowsAffected = db.update(DBHelper.TABLE_REPAIR_ORDER, values, whereClause, args)
        return rowsAffected == 1
    }

    private fun queryOrders(
        db: SQLiteDatabase,
        selection: String?,
        selectionArgs: Array<String>?
    ): List<RepairOrder> {
        val orders = mutableListOf<RepairOrder>()
        db.query(
            DBHelper.TABLE_REPAIR_ORDER,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "create_time DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                orders.add(orderFromCursor(cursor))
            }
        }
        return orders
    }

    private fun orderFromCursor(cursor: Cursor): RepairOrder {
        val handlerIdIndex = cursor.getColumnIndexOrThrow("handler_id")
        val handlerNameIndex = cursor.getColumnIndexOrThrow("handler_name")
        val claimTimeIndex = cursor.getColumnIndexOrThrow("claim_time")
        return RepairOrder(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
            studentId = cursor.getLong(cursor.getColumnIndexOrThrow("student_id")),
            studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name")),
            type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
            location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
            level = cursor.getInt(cursor.getColumnIndexOrThrow("level")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
            status = cursor.getInt(cursor.getColumnIndexOrThrow("status")),
            handlerId = if (cursor.isNull(handlerIdIndex)) null else cursor.getLong(handlerIdIndex),
            handlerName = if (cursor.isNull(handlerNameIndex)) null else cursor.getString(handlerNameIndex),
            claimTime = if (cursor.isNull(claimTimeIndex)) null else cursor.getLong(claimTimeIndex),
            createTime = cursor.getLong(cursor.getColumnIndexOrThrow("create_time")),
            updateTime = cursor.getLong(cursor.getColumnIndexOrThrow("update_time"))
        )
    }
}
