package com.myproject.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.myproject.myapplication.*
import com.myproject.myapplication.myrecyclerview.DailyAdapter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_daily_calendar.*
import java.sql.Date
import java.util.concurrent.TimeUnit


class DailyCalendarFragment : Fragment() {

    companion object {
        private val TAG: String? = DailyCalendarFragment::class.simpleName
    }

    lateinit var adapter: DailyAdapter
    val disposables = CompositeDisposable()
    private val dateCreator = DateCreator()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_daily_calendar, container, false) as RelativeLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val todoList = getDataFlowable().toList().blockingGet()
        val dataList = ArrayList<DailyAdapter.Item>()
        dateCreator.createDateFlowable(15, -3, todoList)
            .subscribe { dataList.add(it) }
            .apply { disposables.add(this) }

        recycler_daily.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recycler_daily.layoutManager = LinearLayoutManager(activity)
        adapter = DailyAdapter(dataList, this)
        recycler_daily.adapter = adapter


        // 스크롤을 빨리할 경우, 이벤트가 여러번 실행되어 중복된 아이템이 나올 수 있음
        // TODO 수정함 테스트 필요
        RxRecyclerView.scrollStateChanges(recycler_daily)
            .map {
                when {
                    !recycler_daily.canScrollVertically(-1) -> 0
                    !recycler_daily.canScrollVertically(1) -> 1
                    else -> 2
                }
            }
            .filter { it != 2 }
            .debounce(300L, TimeUnit.MILLISECONDS)
            .subscribe {
                val todoList = getDataFlowable().toList().blockingGet()
                if (it == 0) addItemInFront(todoList)
                else addItemToBack(todoList)
            }
            .apply { disposables.add(this) }

    }

    private fun addItemToBack(todoList: MutableList<CalendarData>) {
        dateCreator.createDateFlowable(
            10,
            1,
            todoList,
            adapter.dataList.findLast { it.type == DailyAdapter.DATE }!!.content as Date
        ).observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.addItem(it, adapter.itemCount) }
            .apply { disposables.add(this) }
    }

    private fun addItemInFront(todoList: MutableList<CalendarData>) {
        var count = 0
        dateCreator.createDateFlowable(10, -10, todoList, adapter.dataList[0].content as Date)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.addItem(it, count++) }
            .apply { disposables.add(this) }
    }

    fun getDataFlowable(): Flowable<CalendarData> {
        val db = (activity as MainActivity).dbHelper.readableDatabase
        return Flowable.just(
            db.query(
                CalendarDBContract.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            )
        ).filter {
            Log.d(TAG, "${it.count}")
            it.moveToNext()
        }.map {
            CalendarData(
                it.getLong(0),
                Date(it.getLong(1)),
                Date(it.getLong(2)),
                it.getString(3)
            )
        }.subscribeOn(Schedulers.io())
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
        val dataList = adapter.dataList
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
        val dataList = adapter.dataList
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
