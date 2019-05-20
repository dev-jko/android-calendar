package com.myproject.myapplication

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.myproject.myapplication.myrecyclerview.DailyAdapter
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_daily_calendar.*
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList


class DailyCalendarFragment : Fragment() {

    lateinit var dateList: ArrayList<Date>
    lateinit var todoList: ArrayList<CalendarData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_daily_calendar, container, false) as RelativeLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        todoList = getData() as ArrayList<CalendarData>
        val gregorianCalendar = GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul"))
        gregorianCalendar.add(GregorianCalendar.DATE, -3)
        dateList = Observable.range(0, 50)
            .map {
                gregorianCalendar.add(GregorianCalendar.DATE, 1)
                Date(gregorianCalendar.time.time)
            }
            .toList().blockingGet() as ArrayList<Date>

        recycler_daily.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recycler_daily.layoutManager = LinearLayoutManager(activity)
        val adapter = DailyAdapter(dateList, todoList, context!!)
        recycler_daily.adapter = adapter
        recycler_daily.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    gregorianCalendar.time = dateList.first()
                    val disposable = Observable.range(0, 10)
                        .observeOn(Schedulers.computation())
                        .map {
                            gregorianCalendar.add(GregorianCalendar.DATE, -1)
                            Date(gregorianCalendar.time.time)
                        }.subscribe { dateList.add(0, it) }
                    recyclerView.adapter?.notifyDataSetChanged()
                    disposable.dispose()

                } else if (!recyclerView.canScrollVertically(1)) {
                    gregorianCalendar.time = dateList.last()
                    val disposable = Observable.range(0, 10)
                        .observeOn(Schedulers.computation())
                        .map {
                            gregorianCalendar.add(GregorianCalendar.DATE, 1)
                            Date(gregorianCalendar.time.time)
                        }.subscribe { dateList.add(it) }
                    recyclerView.adapter?.notifyDataSetChanged()
                    disposable.dispose()
                }
            }
        })


    }

    fun getData(): List<CalendarData> {
        val arrayList = ArrayList<CalendarData>()
        val db = (activity as MainActivity).dbHelper.readableDatabase
        val cursor = db.query(
            CalendarDBContract.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        while (cursor.moveToNext()) {
            val temp = CalendarData(
                cursor.getInt(0),
                Date(cursor.getLong(1)),
                Date(cursor.getLong(2)),
                cursor.getString(3)
            )
            arrayList.add(temp)
        }
        cursor.close()
        return arrayList.toMutableList()
    }

    fun updateRecycler(calendarData: CalendarData) {
        todoList.add(calendarData)
        recycler_daily.adapter?.notifyDataSetChanged()

    }
}
