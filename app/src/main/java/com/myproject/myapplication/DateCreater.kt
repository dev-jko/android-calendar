package com.myproject.myapplication

import android.util.Log
import com.myproject.myapplication.myrecyclerview.DailyAdapter
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList

class DateCreator {

    fun createDate(
        size: Int,
        offset: Int,
        todoList: List<CalendarData> = List(0) { CalendarData(0, Date(0), Date(0), "") },
        startDate: Date? = null
    ): Observable<DailyAdapter.Item> {
        val gregorianCalendar = GregorianCalendar()
        if (startDate == null) {
            gregorianCalendar.add(Calendar.HOUR_OF_DAY, -gregorianCalendar.get(Calendar.HOUR_OF_DAY))
            gregorianCalendar.add(Calendar.MINUTE, -gregorianCalendar.get(Calendar.MINUTE))
            gregorianCalendar.add(Calendar.SECOND, -gregorianCalendar.get(Calendar.SECOND))
            gregorianCalendar.add(Calendar.MILLISECOND, -gregorianCalendar.get(Calendar.MILLISECOND))
        } else {
            gregorianCalendar.time = startDate
        }
        gregorianCalendar.add(Calendar.DATE, offset - 1)
        return Observable.range(0, size)
            .map {
                gregorianCalendar.add(GregorianCalendar.DATE, 1)
                val date = Date(gregorianCalendar.time.time)
                DailyAdapter.Item(
                    DailyAdapter.DATE,
                    date,

                    // TODO 날짜 시간 맞추기
                    todoList.filter {
                        (it.startDate.time >= date.time) && (date.time + 86400000L > it.endDate.time)
                    } as ArrayList<CalendarData>
                )
            }.subscribeOn(Schedulers.computation())
    }

}