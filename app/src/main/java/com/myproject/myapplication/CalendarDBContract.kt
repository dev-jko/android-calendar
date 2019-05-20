package com.myproject.myapplication

object CalendarDBContract {
    const val TABLE_NAME = "Calendar"
    const val COLUMN_ID = "ID"
    const val COLUMN_START_DATE = "StartDate"
    const val COLUMN_END_DATE = "EndDate"
    const val COLUMN_CONTENT = "Content"

    const val SQL_CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_START_DATE INTEGER NOT NULL," +
                "$COLUMN_END_DATE INTEGER NOT NULL," +
                "$COLUMN_CONTENT TEXT NOT NULL)"

    const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}
