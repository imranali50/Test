package com.findmykids.tracker.panda.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AnalysisDatabase(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ( "
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + APP + " TEXT, "
                + RESTRICTED + " TEXT, "
                + UNRESTRICTED + " TEXT, "
                + LIMITED + " TEXT, "
                + UNLIMITED + " TEXT, "
                + BLOCKED + " TEXT, "
                + NOTIBLOCKED + " TEXT, "
                + WEB + " TEXT, "
                + WEBBLOCKED + " TEXT, "
                + WEBUNBLOCKED + " TEXT, "
                + ACCESSDENIED + " TEXT )")
        db.execSQL(query)
    }

    fun addApp(packageName: String?) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(APP, packageName)
        values.put(RESTRICTED, "1")
        values.put(UNRESTRICTED, "0")
        values.put(LIMITED, "1")
        values.put(UNLIMITED, "0")
        values.put(BLOCKED, "0")
        values.put(NOTIBLOCKED, "0")
        db.insert(TABLE_NAME, null, values)
    }

    fun deleteApp(value: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + APP + "='" + value + "'")
    }

    fun addWeb(packageName: String?) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(WEB, packageName)
        values.put(WEBBLOCKED, "1")
        values.put(WEBUNBLOCKED, "0")
        values.put(ACCESSDENIED, "0")
        db.insert(TABLE_NAME, null, values)
    }

    fun deleteWeb(value: String) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + WEB + "='" + value + "'")
    }

    fun readAllApps(): ArrayList<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT " + APP + " FROM " + TABLE_NAME, null)
        val packs = ArrayList<String>()
        if (cursor.moveToFirst()) {
            do {
                packs.add(cursor.getString(cursor.getColumnIndexOrThrow(APP)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return packs
    }

    fun readWebBlocked(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + WEBBLOCKED + " FROM " + TABLE_NAME + " WHERE " + WEB + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(WEBBLOCKED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    fun readWebUnlocked(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + WEBUNBLOCKED + " FROM " + TABLE_NAME + " WHERE " + WEB + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(WEBUNBLOCKED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    fun readAccessDenied(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + ACCESSDENIED + " FROM " + TABLE_NAME + " WHERE " + WEB + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(ACCESSDENIED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    fun readAllAWeb(): ArrayList<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT " + WEB + " FROM " + TABLE_NAME, null)
        val packs = ArrayList<String>()
        if (cursor.moveToFirst()) {
            do {
                packs.add(cursor.getString(cursor.getColumnIndexOrThrow(WEB)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return packs
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun inAppRestrict(pkgName: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + RESTRICTED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + pkgName + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(RESTRICTED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + RESTRICTED + " = " + value + " WHERE " + APP + "='" + pkgName + "'")
        cursor.close()
    }

    fun inAppLimit(pkgName: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + LIMITED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + pkgName + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(LIMITED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + LIMITED + " = " + value + " WHERE " + APP + "='" + pkgName + "'")
        cursor.close()
    }

    fun inAppUnrestrict(pkgName: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + UNRESTRICTED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + pkgName + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(UNRESTRICTED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + UNRESTRICTED + " = " + value + " WHERE " + APP + "='" + pkgName + "'")
        cursor.close()
    }

    fun inAppUnlimit(pkgName: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + UNLIMITED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + pkgName + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(UNLIMITED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + UNLIMITED + " = " + value + " WHERE " + APP + "='" + pkgName + "'")
        cursor.close()
    }

    fun inAppBlocked(pkgName: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + BLOCKED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + pkgName + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(BLOCKED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + BLOCKED + " = " + value + " WHERE " + APP + "='" + pkgName + "'")
        cursor.close()
    }

    fun inAppNotiblocked(pkgName: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + NOTIBLOCKED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + pkgName + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(NOTIBLOCKED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + NOTIBLOCKED + " = " + value + " WHERE " + APP + "='" + pkgName + "'")
        cursor.close()
    }

    fun inWebBlocked(web: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + WEBBLOCKED + " FROM " + TABLE_NAME + " WHERE " + WEB + "='" + web + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(WEBBLOCKED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + WEBBLOCKED + " = " + value + " WHERE " + WEB + "='" + web + "'")
        cursor.close()
    }

    fun inWebUnblocked(web: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + WEBUNBLOCKED + " FROM " + TABLE_NAME + " WHERE " + WEB + "='" + web + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(WEBUNBLOCKED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + WEBUNBLOCKED + " = " + value + " WHERE " + WEB + "='" + web + "'")
        cursor.close()
    }

    fun inWebAccessDenied(web: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT " + ACCESSDENIED + " FROM " + TABLE_NAME + " WHERE " + WEB + "='" + web + "'",
            null
        )
        var value = ""
        if (cursor.moveToFirst()) {
            do {
                value = cursor.getString(cursor.getColumnIndexOrThrow(ACCESSDENIED))
            } while (cursor.moveToNext())
        }
        var temp = value.toInt()
        temp++
        value = temp.toString()
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + ACCESSDENIED + " = " + value + " WHERE " + WEB + "='" + web + "'")
        cursor.close()
    }

    fun readAppRestricted(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + RESTRICTED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(RESTRICTED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    fun readAppUnrestricted(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + UNRESTRICTED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(UNRESTRICTED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    fun readAppLimited(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + LIMITED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(LIMITED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    fun readAppUnlimited(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + UNLIMITED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(UNLIMITED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    fun readAppBlocked(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + BLOCKED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(BLOCKED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    fun readAppNotiBlocked(webName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT " + NOTIBLOCKED + " FROM " + TABLE_NAME + " WHERE " + APP + "='" + webName + "'",
            null
        )
        var temp = ""
        if (cursor.moveToFirst()) {
            do {
                temp = cursor.getString(cursor.getColumnIndexOrThrow(NOTIBLOCKED))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return temp
    }

    companion object {
        private const val DB_NAME = "AnalysisDB"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "analysisCheck"
        private const val ID_COL = "id4"
        private const val APP = "app"
        private const val RESTRICTED = "restricted"
        private const val UNRESTRICTED = "unrestricted"
        private const val LIMITED = "limited"
        private const val UNLIMITED = "unlimited"
        private const val BLOCKED = "blocked"
        private const val NOTIBLOCKED = "notiblocked"
        private const val WEB = "web"
        private const val WEBBLOCKED = "webblocked"
        private const val WEBUNBLOCKED = "webunblocked"
        private const val ACCESSDENIED = "accessdenied"
    }
}