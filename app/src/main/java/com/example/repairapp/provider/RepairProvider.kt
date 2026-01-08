package com.example.repairapp.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.repairapp.data.DBHelper

class RepairProvider : ContentProvider() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(): Boolean {
        val context = context ?: return false
        dbHelper = DBHelper(context)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return when (uriMatcher.match(uri)) {
            MATCH_REPAIR_ORDER -> {
                db.query(DBHelper.TABLE_REPAIR_ORDER, projection, selection, selectionArgs, null, null, sortOrder)
            }
            MATCH_REPAIR_ORDER_ID -> {
                val id = ContentUris.parseId(uri)
                val where = appendIdSelection(selection)
                val args = appendIdArgs(selectionArgs, id)
                db.query(DBHelper.TABLE_REPAIR_ORDER, projection, where, args, null, null, sortOrder)
            }
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            MATCH_REPAIR_ORDER -> {
                val rowId = db.insert(DBHelper.TABLE_REPAIR_ORDER, null, values)
                if (rowId == -1L) null else ContentUris.withAppendedId(CONTENT_URI, rowId)
            }
            else -> null
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            MATCH_REPAIR_ORDER -> {
                db.update(DBHelper.TABLE_REPAIR_ORDER, values, selection, selectionArgs)
            }
            MATCH_REPAIR_ORDER_ID -> {
                val id = ContentUris.parseId(uri)
                val where = appendIdSelection(selection)
                val args = appendIdArgs(selectionArgs, id)
                db.update(DBHelper.TABLE_REPAIR_ORDER, values, where, args)
            }
            else -> 0
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    private fun appendIdSelection(selection: String?): String {
        return if (selection.isNullOrBlank()) {
            "_id = ?"
        } else {
            "($selection) AND _id = ?"
        }
    }

    private fun appendIdArgs(selectionArgs: Array<String>?, id: Long): Array<String> {
        val base = selectionArgs ?: emptyArray()
        return base + id.toString()
    }

    companion object {
        const val AUTHORITY = "com.example.repairapp.repair.provider"
        private const val PATH_REPAIR_ORDER = "repair_order"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_REPAIR_ORDER")

        private const val MATCH_REPAIR_ORDER = 1
        private const val MATCH_REPAIR_ORDER_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_REPAIR_ORDER, MATCH_REPAIR_ORDER)
            addURI(AUTHORITY, "$PATH_REPAIR_ORDER/#", MATCH_REPAIR_ORDER_ID)
        }
    }
}
