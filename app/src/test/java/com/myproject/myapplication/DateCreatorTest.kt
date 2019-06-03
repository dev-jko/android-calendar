package com.myproject.myapplication

import com.myproject.myapplication.myrecyclerview.DailyAdapter
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList


class DateCreatorTest {

    lateinit var dateCreator: DateCreator
    lateinit var todoList: ArrayList<CalendarData>

    @Before
    fun setUp() {
        dateCreator = DateCreator()
        todoList = ArrayList(0)
    }

    @Test
    fun `test current date`() {
        val gc = GregorianCalendar()
        val dataList = dateCreator.createDate(1, 0, todoList).toList().blockingGet()
        val time = GregorianCalendar()
        time.time = dataList[0].content as Date
        assertEquals(time.get(Calendar.YEAR), gc.get(Calendar.YEAR))
        assertEquals(time.get(Calendar.MONTH), gc.get(Calendar.MONTH))
        assertEquals(time.get(Calendar.DATE), gc.get(Calendar.DATE))

    }


}