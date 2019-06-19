package com.myproject.myapplication.dailyCalendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.myproject.myapplication.R
import com.myproject.myapplication.Repository.CalendarData
import com.myproject.myapplication.Repository.DataBaseOpenHelper
import com.myproject.myapplication.Repository.DateCreator
import com.myproject.myapplication.databinding.FragmentDailyCalendarBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    val disposables = CompositeDisposable()
    lateinit var dbHelper: DataBaseOpenHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dbHelper = DataBaseOpenHelper(context!!)

        val binding: FragmentDailyCalendarBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_daily_calendar, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_daily.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                context,
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )


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
                val todoList = viewModel.getDataFlowable().toList().blockingGet()
                if (it == 0) addItemInFront(todoList)
                else addItemToBack(todoList)
            }
            .apply { disposables.add(this) }

    }

    private fun addItemToBack(todoList: MutableList<CalendarData>) {
        DateCreator.createDateFlowable(
            10,
            1,
            todoList,
            viewModel.adapter!!.dataList.findLast { it.type == DailyAdapter.DATE }!!.content as Date
        ).observeOn(AndroidSchedulers.mainThread())
            .subscribe { viewModel.adapter!!.addItem(it, viewModel.adapter!!.itemCount) }
            .apply { disposables.add(this) }
    }

    private fun addItemInFront(todoList: MutableList<CalendarData>) {
        var count = 0
        DateCreator.createDateFlowable(10, -10, todoList, viewModel.adapter!!.dataList[0].content as Date)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { viewModel.adapter!!.addItem(it, count++) }
            .apply { disposables.add(this) }
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
        val dataList = viewModel.adapter!!.dataList
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
        val dataList = viewModel.adapter!!.dataList
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
