package com.findmykids.tracker.panda.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//DONE
class Packages(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ( "
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PACKAGE_COL_RESTRICT + " TEXT ) ")
        db.execSQL(query)
    }

    fun addPackage(packageName: String?) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(PACKAGE_COL_RESTRICT, packageName)
        db.insert(TABLE_NAME, null, values)
    }

    fun deletePackage(value: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + PACKAGE_COL_RESTRICT + "='" + value + "'")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun readPacks(): ArrayList<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT " + PACKAGE_COL_RESTRICT + " FROM " + TABLE_NAME, null)
        val packs = ArrayList<String>()
        if (cursor.moveToFirst()) {
            do {
                packs.add(cursor.getString(cursor.getColumnIndexOrThrow(PACKAGE_COL_RESTRICT)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return packs
    }

    companion object {
        private const val DB_NAME = "PackageCheckDB"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "packageCheck"
        private const val ID_COL = "id3"
        private const val PACKAGE_COL_RESTRICT = "packageName3"
    }
}