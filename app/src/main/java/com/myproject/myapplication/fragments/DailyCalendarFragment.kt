package com.myproject.myapplication.fragments

import android.app.Activity
import android.content.Intent
import android.database.Cursor
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
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_daily_calendar.*
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList


class DailyCalendarFragment : Fragment() {

    val dataList = ArrayList<DailyAdapter.Item>()
    val disposables = CompositeDisposable()

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
        Observable.range(0, 20)
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
            .subscribe(
                { dataList.add(it) },
                { it.printStackTrace() },
                { Log.d("DailyCalendarFragment", "create list completed") }
            ).apply { disposables.add(this) }

        recycler_daily.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recycler_daily.layoutManager = LinearLayoutManager(activity)
        val adapter = DailyAdapter(dataList, this)
        recycler_daily.adapter = adapter
        recycler_daily.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val todoList = getData()
                if (!recyclerView.canScrollVertically(-1)) {
                    gregorianCalendar.time = dataList[0].content as Date
                    Observable.range(0, 10)
                        .map {
                            gregorianCalendar.add(GregorianCalendar.DATE, -1)
                            val date = Date(gregorianCalendar.time.time)
                            DailyAdapter.Item(DailyAdapter.DATE, date,
                                todoList.filter {
                                    (it.startDate.time <= date.time) && (date.time < it.endDate.time + 86400000L)
                                } as ArrayList<CalendarData>)
                        }.subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            dataList.add(0, it)
                            recyclerView.adapter!!.notifyItemInserted(0)
                        }.apply { disposables.add(this) }
                } else if (!recyclerView.canScrollVertically(1)) {
                    gregorianCalendar.time = dataList.findLast { it.type == DailyAdapter.DATE }!!.content as Date
                    Observable.range(0, 10)
                        .map {
                            gregorianCalendar.add(GregorianCalendar.DATE, 1)
                            val date = Date(gregorianCalendar.time.time)
                            DailyAdapter.Item(DailyAdapter.DATE, date,
                                todoList.filter {
                                    (it.startDate.time <= date.time) && (date.time < it.endDate.time + 86400000L)
                                } as ArrayList<CalendarData>)
                        }.subscribe {
                            dataList.add(it)
                            recyclerView.adapter!!.notifyItemInserted(dataList.size - 1)
                        }.apply { disposables.add(this) }
                }
            }
        })

    }

    fun getData(): List<CalendarData> {
        val arrayList = ArrayList<CalendarData>()
        val db = (activity as MainActivity).dbHelper.readableDatabase
        var cursor : Cursor? = null
        Flowable.just(
            db.query(
                CalendarDBContract.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            )
        ).map{
            cursor = it
            it
        }.filter { it.moveToNext() }
            .map {
                CalendarData(
                    it.getLong(0),
                    Date(it.getLong(1)),
                    Date(it.getLong(2)),
                    it.getString(3)
                )
            }.doFinally { cursor?.close() }
            .subscribeOn(Schedulers.io())
            .subscribe(
                { arrayList.add(it) },
                { it.printStackTrace() },
                { }
            ).apply { disposables.add(this) }
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
        val indices =
            dataList.filter { it.type == DailyAdapter.DATE && (it.content as Date).time >= calendarData.startDate.time && (it.content).time < calendarData.endDate.time + 86400000L }
                .map { dataList.indexOf(it) }
        var count = 0
        indices.forEach {
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
                val rm = it.invisibleChildren!!.find { calendarData -> calendarData.id == id }
                if (rm != null) it.invisibleChildren.remove(rm)
            }
        }
        val indices = dataList.filter {
            it.type == DailyAdapter.TODO && (it.content as CalendarData).id == id
        }.map { dataList.indexOf(it) }
        var count = 0
        indices.forEach {
            dataList.removeAt(it - count)
            recycler_daily.adapter!!.notifyItemRemoved(it - count)
            count++
        }
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }
}
