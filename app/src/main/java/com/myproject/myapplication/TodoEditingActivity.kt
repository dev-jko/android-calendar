package com.myproject.myapplication

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.DatePicker
import android.widget.Toast
import com.jakewharton.rxbinding2.view.RxView
import com.myproject.myapplication.fragments.DatePickerDialogFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_todo_editing.*
import java.sql.Date
import java.util.*
import java.util.concurrent.TimeUnit


class TodoEditingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    lateinit var startDate: Date
    lateinit var endDate: Date
    var datePickerFlag = 0
    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_editing)

        startDate = intent.getSerializableExtra("startDate") as Date
        endDate = intent.getSerializableExtra("endDate") as Date

        text_view_todo_editing_start_date.text = startDate.toString()
        text_view_todo_editing_end_date.text = endDate.toString()

        RxView.clicks(linear_layout_todo_editing_start)
            .map { Bundle().apply { putSerializable("date", startDate) } }
            .map { DatePickerDialogFragment().apply { arguments = it } }
            .subscribe {
                datePickerFlag = 0
                it.show(this.supportFragmentManager, "startDatePicker")
            }
            .apply { disposables.add(this) }

        RxView.clicks(linear_layout_todo_editing_end)
            .map { Bundle().apply { putSerializable("date", endDate) } }
            .map { DatePickerDialogFragment().apply { arguments = it } }
            .subscribe {
                datePickerFlag = 1
                it.show(this.supportFragmentManager, "endDatePicker")
            }
            .apply { disposables.add(this) }

        btn_todo_editing_save.setOnClickListener {
            val db = DataBaseOpenHelper(this).writableDatabase
            val values = ContentValues().apply {
                put(CalendarDBContract.COLUMN_START_DATE, startDate.time)
                put(CalendarDBContract.COLUMN_END_DATE, endDate.time)
                put(CalendarDBContract.COLUMN_CONTENT, edit_text_todo_editing_content.text.toString())
            }
            val result = db.insert(CalendarDBContract.TABLE_NAME, null, values)
            db.close()
            if (result == -1L) {
                Toast.makeText(this, "오류가 발생했습니다. 다시 저장해주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "일정이 저장되었습니다. $result", Toast.LENGTH_LONG).show()
            val intent = Intent()
            intent.putExtra("id", result)
            intent.putExtra("startDate", startDate)
            intent.putExtra("endDate", endDate)
            intent.putExtra("content", edit_text_todo_editing_content.text.toString())
            setResult(50, intent)
            finish()
        }

        RxView.clicks(btn_todo_editing_cancel)
            .debounce(400, TimeUnit.MILLISECONDS)
            .subscribe { onBackPressed() }
            .apply { disposables.add(this) }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c = GregorianCalendar.getInstance()
        c.set(year, month, dayOfMonth)
        if (datePickerFlag == 0) {
            startDate = Date(c.time.time)
            text_view_todo_editing_start_date.text = startDate.toString()
            return
        }
        endDate = Date(c.time.time)
        text_view_todo_editing_end_date.text = endDate.toString()
    }


}
