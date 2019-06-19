package com.myproject.myapplication.Repository

import com.myproject.myapplication.dailyCalendar.DailyAdapter
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.sql.Date
import java.util.*

class DateCreator {

    companion object {


        fun createDateFlowable(
            size: Int,
            offset: Int,
            todoList: List<CalendarData> = List(0) {
                CalendarData(
                    0,
                    Date(0),
                    Date(0),
                    ""
                )
            },
            startDate: Date? = null
        ): Flowable<DailyAdapter.Item> {
            val gregorianCalendar = GregorianCalendar()
            if (startDate == null) {
                setTimeMidnight(gregorianCalendar)
            } else {
                gregorianCalendar.time = startDate
            }
            gregorianCalendar.add(Calendar.DATE, offset - 1)
            return Flowable.range(0, size)
                .map {
                    gregorianCalendar.add(GregorianCalendar.DATE, 1)
                    val date = Date(gregorianCalendar.time.time)
                    DailyAdapter.Item(
                        DailyAdapter.DATE,
                        date,
                        todoList.filter {
                            date.time + 86400000L > it.startDate.time
                                    && date.time <= it.endDate.time
                        } as ArrayList<CalendarData>
                    )
                }.subscribeOn(Schedulers.computation())
        }


        private fun setTimeMidnight(gregorianCalendar: GregorianCalendar) {
            gregorianCalendar.add(Calendar.HOUR_OF_DAY, -gregorianCalendar.get(Calendar.HOUR_OF_DAY))
            gregorianCalendar.add(Calendar.MINUTE, -gregorianCalendar.get(Calendar.MINUTE))
            gregorianCalendar.add(Calendar.SECOND, -gregorianCalendar.get(Calendar.SECOND))
            gregorianCalendar.add(Calendar.MILLISECOND, -gregorianCalendar.get(Calendar.MILLISECOND))
        }

    }


}