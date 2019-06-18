package com.myproject.myapplication.dailyCalendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.myproject.myapplication.*
import com.myproject.myapplication.databinding.FragmentDailyCalendarBinding
import com.myproject.myapplication.main.MainActivity
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_daily_calendar.*
import java.sql.Date
import java.util.concurrent.TimeUnit


class DailyCalendarFragment : androidx.fragment.app.Fragment() {

    companion object {
        private val TAG: String? = DailyCalendarFragment::class.simpleName
    }

    private val viewModel: DailyCalendarViewModel by lazy {
        ViewModelProviders.of(this).get(DailyCalendarViewModel::class.java)
    }

    lateinit var adapter: DailyAdapter
    val disposables = CompositeDisposable()
    private val dateCreator = DateCreator()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDailyCalendarBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_daily_calendar, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        return binding.root
//        return inflater.inflate(R.layout.fragment_daily_calendar, container, false) as RelativeLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_daily.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                context,
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )
        recycler_daily.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        adapter = DailyAdapter(this)
        recycler_daily.adapter = adapter
        val todoList = getDataFlowable()
            .toList()
            .blockingGet()
        dateCreator.createDateFlowable(15, -3, todoList)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.addItem(it, adapter.itemCount) }
            .apply { disposables.add(this) }

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
        return Flowable.fromIterable(
            IterableCursor(
                db.query(
                    CalendarDBContract.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        )
            .map {
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
            dataList.filter {
                (it.type == DailyAdapter.DATE)
                        && (it.content as Date).time + 86400000L > calendarData.startDate.time
                        && (it.content).time <= calendarData.endDate.time
            }
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
