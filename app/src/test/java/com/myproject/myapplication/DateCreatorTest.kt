package com.myproject.myapplication

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.GregorianCalendar
import java.util.Calendar

class DateCreatorTest {

    lateinit var dateCreator: DateCreator
    lateinit var gc: GregorianCalendar

    @Before
    fun setUp() {
        dateCreator = DateCreator()
        gc = GregorianCalendar()
        gc.add(Calendar.HOUR_OF_DAY, -gc.get(Calendar.HOUR_OF_DAY))
        gc.add(Calendar.MINUTE, -gc.get(Calendar.MINUTE))
        gc.add(Calendar.SECOND, -gc.get(Calendar.SECOND))
        gc.add(Calendar.MILLISECOND, -gc.get(Calendar.MILLISECOND))
    }

    @Test
    fun `test current date`() {
        val dataList = dateCreator.createDate(1, 0).toList().blockingGet()
        val result = dataList[0].content as Date
        assertEquals(result, gc.time)
    }

    @Test
    fun `test midnight`() {
        val dataList = dateCreator.createDate(1, 0).toList().blockingGet()
        val result = dataList[0].content as Date
        assertEquals(result, gc.time)
    }

    @Test
    fun `test order`() {
        val testSize = 200
        val dataList = dateCreator.createDate(testSize, 0).toList().blockingGet()
        gc.add(Calendar.DATE, -1)
        val expected: List<Date> = List<Date>(testSize) {
            gc.add(Calendar.DATE, 1)
            Date(gc.time.time)
        }

        for (i in 0 until testSize) {
            assertEquals(dataList[i].content as Date, expected[i])
        }
    }

    @Test
    fun `test plus offset`() {
        val offset = 5
        val data = dateCreator.createDate(1, offset).toList().blockingGet()
        val result = data[0].content as Date
        gc.add(Calendar.DATE, offset)
        assertEquals(result, gc.time)
    }

    @Test
    fun `test minus offset`() {
        val offset = -4
        val data = dateCreator.createDate(1, offset).toList().blockingGet()
        val result = data[0].content as Date
        gc.add(Calendar.DATE, offset)
        assertEquals(result, gc.time)
    }

}