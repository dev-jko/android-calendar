package com.myproject.myapplication

import java.sql.Date


data class CalendarData(
    val id: Int,
    val startDate: Date,
    val endDate: Date,
    val content: String
)