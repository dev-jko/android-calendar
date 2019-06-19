package com.myproject.myapplication.dailyCalendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myproject.myapplication.IterableCursor
import com.myproject.myapplication.Repository.CalendarDBContract
import com.myproject.myapplication.Repository.CalendarData
import com.myproject.myapplication.Repository.DataBaseOpenHelper
import com.myproject.myapplication.Repository.DateCreator
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.sql.Date

class DailyCalendarViewModel(application: Application) : AndroidViewModel(application) {

    var adapter = DailyAdapter()
    private val compositeDisposable = CompositeDisposable()
    val dbHelper: DataBaseOpenHelper = DataBaseOpenHelper(application)
    val liveDailyItems = MutableLiveData<List<DailyAdapter.Item>>()

    init {
        val todoList = getDataFlowable()
            .toList()
            .blockingGet()
        DateCreator.createDateFlowable(15, -3, todoList)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.addItem(it, adapter.itemCount) }
            .apply { compositeDisposable.add(this) }
    }

    fun getDataFlowable(): Flowable<CalendarData> {
        val db = dbHelper.readableDatabase
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
        val db = dbHelper.writableDatabase
        val selection = "${CalendarDBContract.COLUMN_ID} = ?"
        val selectionArgs = arrayOf("$id")
        return db.delete(CalendarDBContract.TABLE_NAME, selection, selectionArgs)
    }

}