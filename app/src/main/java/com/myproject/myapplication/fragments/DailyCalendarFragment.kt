package com.myproject.myapplication.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.myproject.myapplication.*
import com.myproject.myapplication.myrecyclerview.DailyAdapter
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_daily_calendar.*
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList


class DailyCalendarFragment : Fragment() {

    val dataList = ArrayList<DailyAdapter.Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_daily_calendar, container, false) as RelativeLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val todoList = getData() as ArrayList<CalendarData>
        val gregorianCalendar = GregorianCalendar(TimeZone.getTimeZone("Asia/Seoul"))
        gregorianCalendar.add(GregorianCalendar.DATE, -3)
        val disposable = Observable.range(0, 50)
            .map {
                gregorianCalendar.add(GregorianCalendar.DATE, 1)
                val date = Date(gregorianCalendar.time.time)
                DailyAdapter.Item(DailyAdapter.DATE, date,
                    todoList.filter {
                        (it.startDate.time <= date.time) && (date.time < it.endDate.time + 86400000L)
                    } as ArrayList<CalendarData>)
            }.subscribeOn(Schedulers.computation())
            .subscribe(
                { dataList.add(it) },
                { it.printStackTrace() },
                { Log.d("DailyCalendarFragment", "create list completed") }
            )

        recycler_daily.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recycler_daily.layoutManager = LinearLayoutManager(activity)
        val adapter = DailyAdapter(dataList, this)
        recycler_daily.adapter = adapter
        recycler_daily.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val todoList = getData()
                if (!recyclerView.canScrollVertically(-1)) {
                    gregorianCalendar.time = dataList.first().content as Date
                    val tempDisposable = Observable.range(0, 10)
                        .map {
                            gregorianCalendar.add(GregorianCalendar.DATE, -1)
                            val date = Date(gregorianCalendar.time.time)
                            DailyAdapter.Item(DailyAdapter.DATE, date,
                                todoList.filter {
                                    (it.startDate.time <= date.time) && (date.time < it.endDate.time + 86400000L)
                                } as ArrayList<CalendarData>)
                        }.subscribeOn(Schedulers.computation())
                        .subscribe { dataList.add(0, it) }
                    recyclerView.adapter?.notifyDataSetChanged()
                    tempDisposable.dispose()
                } else if (!recyclerView.canScrollVertically(1)) {
                    gregorianCalendar.time = dataList.findLast { it.type == DailyAdapter.DATE }!!.content as Date
                    val tempDisposable = Observable.range(0, 10)
                        .map {
                            gregorianCalendar.add(GregorianCalendar.DATE, 1)
                            val date = Date(gregorianCalendar.time.time)
                            DailyAdapter.Item(DailyAdapter.DATE, date,
                                todoList.filter {
                                    (it.startDate.time <= date.time) && (date.time < it.endDate.time + 86400000L)
                                } as ArrayList<CalendarData>)
                        }.subscribe { dataList.add(it) }
                    recyclerView.adapter?.notifyDataSetChanged()
                    tempDisposable.dispose()
                }
            }
        })

//        disposable.dispose()
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
                cursor.getLong(0),
                Date(cursor.getLong(1)),
                Date(cursor.getLong(2)),
                cursor.getString(3)
            )
            arrayList.add(temp)
        }
        cursor.close()
        return arrayList.toMutableList()
    }

    fun deleteData(id: Long): Int {
        val db = (activity as MainActivity).dbHelper.writableDatabase
        val selection = "${CalendarDBContract.COLUMN_ID} = ?"
        val selectionArgs = arrayOf("$id")
        return db.delete(CalendarDBContract.TABLE_NAME, selection, selectionArgs)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == resultCode) {
            val calendarData = CalendarData(
                data!!.getLongExtra("id", 0),
                data.getSerializableExtra("startDate") as Date,
                data.getSerializableExtra("endDate") as Date,
                data.getStringExtra("content")
            )
            insertList(calendarData)
        }
    }


    fun insertList(calendarData: CalendarData) {
        val indexes =
            dataList.filter { it.type == DailyAdapter.DATE && (it.content as Date).time >= calendarData.startDate.time && (it.content).time < calendarData.endDate.time + 86400000L }
                .map { dataList.indexOf(it) }
        var count = 0
        indexes.forEach {
            if (dataList[it + count].invisibleChildren!!.size == 0) {
                var index = it + count + 1
                while (dataList[index].type == DailyAdapter.TODO) {
                    index++
                }
                dataList.add(index, DailyAdapter.Item(DailyAdapter.TODO, calendarData))
                recycler_daily.adapter!!.notifyItemInserted(index)
                count++
            } else {
                dataList[it + count].invisibleChildren!!.add(calendarData)
            }
        }
    }

    fun deleteList(id: Long) {
        dataList.forEach {
            if (it.type == DailyAdapter.DATE) {
                val rm = it.invisibleChildren!!.find {calendarData -> calendarData.id == id }
                if(rm != null) it.invisibleChildren.remove(rm)
            }
        }
        // TODO  리스트 삭제
//        if ((it.content as CalendarData).id == id) {
//            return true
//            dataList.remove(it)
//        }

        recycler_daily.adapter!!.notifyDataSetChanged()
    }

}
