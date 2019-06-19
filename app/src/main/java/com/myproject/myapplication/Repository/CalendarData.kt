package com.myproject.myapplication.Repository

import java.sql.Date


data class CalendarData(
    val id: Long,
    val startDate: Date,
    val endDate: Date,
    val content: String
)