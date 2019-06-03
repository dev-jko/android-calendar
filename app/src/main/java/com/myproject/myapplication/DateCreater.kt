package com.myproject.myapplication

import com.myproject.myapplication.myrecyclerview.DailyAdapter
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList

class DateCreator {

    fun createDate(size: Int, offset: Int, todoList: ArrayList<CalendarData>): Observable<DailyAdapter.Item> {
        val gregorianCalendar = GregorianCalendar()
        gregorianCalendar.add(GregorianCalendar.DATE, offset - 1)
        return Observable.range(0, size)
            .map {
                gregorianCalendar.add(GregorianCalendar.DATE, 1)
                val date = Date(gregorianCalendar.time.time)
                DailyAdapter.Item(
                    DailyAdapter.DATE,
                    date,
                    todoList.filter {
                        (it.startDate.time <= date.time) && (date.time < it.endDate.time + 86400000L)
                    } as ArrayList<CalendarData>
                )
            }.subscribeOn(Schedulers.computation())
    }

}