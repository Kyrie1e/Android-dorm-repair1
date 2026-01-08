package com.example.repairapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_USER_TABLE)
        db.execSQL(SQL_CREATE_REPAIR_ORDER_TABLE)
        db.execSQL(SQL_CREATE_REPAIR_LOG_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DROP_REPAIR_LOG_TABLE)
        db.execSQL(SQL_DROP_REPAIR_ORDER_TABLE)
        db.execSQL(SQL_DROP_USER_TABLE)
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "repair_app.db"
        const val DATABASE_VERSION = 1

        const val TABLE_USER = "user"
        const val TABLE_REPAIR_ORDER = "repair_order"
        const val TABLE_REPAIR_LOG = "repair_log"

        val SQL_CREATE_USER_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_USER (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('student','worker')),
                create_time INTEGER NOT NULL
            );
        """.trimIndent()

        val SQL_CREATE_REPAIR_ORDER_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_REPAIR_ORDER (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                student_name TEXT NOT NULL,
                type TEXT NOT NULL,
                location TEXT NOT NULL,
                level INTEGER NOT NULL,
                description TEXT NOT NULL,
                status INTEGER NOT NULL,
                handler_id INTEGER,
                handler_name TEXT,
                claim_time INTEGER,
                create_time INTEGER NOT NULL,
                update_time INTEGER NOT NULL
            );
        """.trimIndent()

        val SQL_CREATE_REPAIR_LOG_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_REPAIR_LOG (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                operator_id INTEGER NOT NULL,
                operator_name TEXT NOT NULL,
                action TEXT NOT NULL,
                note TEXT,
                time INTEGER NOT NULL
            );
        """.trimIndent()

        const val SQL_DROP_USER_TABLE = "DROP TABLE IF EXISTS $TABLE_USER"
        const val SQL_DROP_REPAIR_ORDER_TABLE = "DROP TABLE IF EXISTS $TABLE_REPAIR_ORDER"
        const val SQL_DROP_REPAIR_LOG_TABLE = "DROP TABLE IF EXISTS $TABLE_REPAIR_LOG"
    }
}
