package com.myproject.myapplication.Repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseOpenHelper(
    context: Context,
    DATABASE_NAME: String = "Calendar.db",
    DATABASE_VERSION: Int = 1
) : SQLiteOpenHelper(
    context, DATABASE_NAME,
    null, DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(CalendarDBContract.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }
}